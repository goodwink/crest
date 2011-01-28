/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest;

import org.codegist.common.lang.Disposable;
import org.codegist.common.lang.Disposables;
import org.codegist.common.lang.Strings;
import org.codegist.common.reflect.ObjectMethodsAwareInvocationHandler;
import org.codegist.crest.config.BasicParamConfig;
import org.codegist.crest.config.ConfigFactoryException;
import org.codegist.crest.config.InterfaceConfig;
import org.codegist.crest.config.MethodConfig;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

/**
 * Default CRest implementation based on {@link org.codegist.crest.CRestContext} interface data model.
 * <p>On top of the behavior described in {@link org.codegist.crest.CRest}, this implementation adds :
 * <p>- {@link org.codegist.crest.interceptor.RequestInterceptor} to intercept any requests before it gets fired.
 * <p>- {@link org.codegist.crest.serializer.Serializer} to customize the serialization process of any types.
 * <p>- {@link org.codegist.crest.injector.Injector} to inject complexe types that can't be reduced to a String via the serializers.
 * <p>- {@link org.codegist.crest.handler.ResponseHandler} to customize response handling when interface method's response type is not one of raw types.
 * <p>- {@link org.codegist.crest.handler.ErrorHandler} to customize how the created interface behaves when any error occurs during the method call process.
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DefaultCRest implements CRest, Disposable {

    private final CRestContext context;

    /**
     * @param context The CRest configuration holder
     */
    public DefaultCRest(CRestContext context) {
        this.context = context;   
    }

    /**
     * @inheritDoc
     */
    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> interfaze) throws CRestException {
        try {
            return (T) context.getProxyFactory().createProxy(interfaze.getClassLoader(), new RestInterfacer(interfaze), new Class[]{interfaze});
        } catch (Exception e) {
            throw CRestException.wrap(e);
        }
    }

    class RestInterfacer<T> extends ObjectMethodsAwareInvocationHandler {

        private final InterfaceContext interfaceContext;

        private RestInterfacer(Class<T> interfaze) throws ConfigFactoryException {
            InterfaceConfig config = context.getConfigFactory().newConfig(interfaze, context);
            this.interfaceContext = new DefaultInterfaceContext(config, context.getProperties());
        }

        @Override
        protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodConfig methodConfig = interfaceContext.getConfig().getMethodConfig(method);
            RequestContext requestContext = new DefaultRequestContext(interfaceContext, method, args);

            int attemptCount = 0;
            ResponseContext responseContext;
            Exception exception;
            do {
                exception = null;
                // build the request, can throw exception but that should not be part of the retry policy
                HttpRequest request = buildRequest(requestContext);
                try {
                    // doInvoke the request
                    HttpResponse response = context.getRestService().exec(request);
                    // wrap the response in response context
                    responseContext = new DefaultResponseContext(requestContext, response);
                } catch (HttpException e) {
                    responseContext = new DefaultResponseContext(requestContext, e.getResponse());
                    exception = e;
                } catch (RuntimeException e) {
                    responseContext = new DefaultResponseContext(requestContext, null);
                    exception = e;
                }
                // loop until an exception has been thrown and the retry handle ask for retry
            }while(exception != null && methodConfig.getRetryHandler().retry(responseContext, exception, ++attemptCount));

            if (exception != null) {
                // An exception has been thrown during request execution, invoke the error handler and return
                return methodConfig.getErrorHandler().handle(responseContext, exception);
            }else{
                // all good, handle the response
                return handle(responseContext);
            }
        }

        /**
         * Response handling base implementation, returns raw response if InputStream or Reader is the requested return type.
         * <p>Otherwise delegate response handling to the given response handler.
         * @param responseContext current response context
         * @return response
         */
        private Object handle(ResponseContext responseContext) {
            Class<?> returnTypeClass = responseContext.getRequestContext().getMethodConfig().getMethod().getReturnType();
            boolean closeResponse = false;
            try {
                if (returnTypeClass.equals(InputStream.class)) {
                    // If InputStream return type, then return raw response ()
                    return responseContext.getResponse().asStream();
                } else if (returnTypeClass.equals(Reader.class)) {
                    // If Reader return type, then return raw response
                    return responseContext.getResponse().asReader();
                } else {
                    // otherwise, delegate to response handler
                    return responseContext.getRequestContext().getMethodConfig().getResponseHandler().handle(responseContext);
                }
            } catch (CRestException e) {
                closeResponse = true;
                throw e;
            } catch (RuntimeException e) {
                closeResponse = true;
                throw new CRestException(e);
            } finally {
                if (closeResponse && responseContext.getResponse() != null) {
                    responseContext.getResponse().close();
                }
            }
        }

        /**
         *
         * @param requestContext
         * @return
         * @throws URISyntaxException
         */
        private HttpRequest buildRequest(RequestContext requestContext) throws Exception {

            // Build base request
            String fullpath = requestContext.getConfig().getEndPoint() + Strings.defaultIfBlank(requestContext.getConfig().getContextPath(), "") + requestContext.getMethodConfig().getPath();
            HttpRequest.Builder builder = new HttpRequest.Builder(fullpath, interfaceContext.getConfig().getEncoding())
                    .using(requestContext.getMethodConfig().getHttpMethod())
                    .timeoutSocketAfter(requestContext.getMethodConfig().getSocketTimeout())
                    .timeoutConnectionAfter(requestContext.getMethodConfig().getConnectionTimeout());

            // Notify injectors (Global and method) before param injection
            requestContext.getConfig().getGlobalInterceptor().beforeParamsInjectionHandle(builder, requestContext);
            requestContext.getMethodConfig().getRequestInterceptor().beforeParamsInjectionHandle(builder, requestContext);

            // Add default params
            for(BasicParamConfig param : requestContext.getMethodConfig().getExtraParams()){
                switch(param.getDestination()){
                    case HEADER:
                        builder.addHeader(param.getName(), param.getDefaultValue());
                        break;
                    case BODY:
                        builder.addBodyParam(param.getName(), param.getDefaultValue());
                        break;
                    default:
                        builder.addQueryParam(param.getName(), param.getDefaultValue());
                        break;
                }
            }

            int count = requestContext.getMethodConfig().getParamCount();
            for (int i = 0; i < count; i++) {
                // invoke configured parameter injectors
                requestContext.getMethodConfig().getParamConfig(i).getInjector().inject(builder, new DefaultParamContext(requestContext, i));
            }

            // Notify injectors (Global and method after param injection
            requestContext.getMethodConfig().getRequestInterceptor().afterParamsInjectionHandle(builder, requestContext);
            requestContext.getConfig().getGlobalInterceptor().afterParamsInjectionHandle(builder, requestContext);

            return builder.build();
        }
    }


    public void dispose() {
        Disposables.dispose(context.getRestService());
    }
}
