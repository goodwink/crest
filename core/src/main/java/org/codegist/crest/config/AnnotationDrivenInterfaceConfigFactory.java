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

package org.codegist.crest.config;

import org.codegist.common.reflect.Methods;
import org.codegist.crest.CRestContext;
import org.codegist.crest.annotate.*;
import org.codegist.crest.annotate.Destination;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>Annotation based config factory of any possible interfaces given to the factory.
 * <p>The factory will lookup any annotation in package {@link org.codegist.crest.annotate} on to the given interface.
 * <p/>
 * <p>- Each config fallback from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link PropertiesDrivenInterfaceConfigFactory}).
 * @see org.codegist.crest.config.InterfaceConfig
 * @see org.codegist.crest.annotate
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class AnnotationDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        try {
            /* Interface specifics */
            EndPoint endPoint = interfaze.getAnnotation(EndPoint.class);
            if(endPoint == null) {
                throw new IllegalArgumentException(EndPoint.class + " annotation not fould on + " + interfaze);
            }
            ContextPath contextPath = interfaze.getAnnotation(ContextPath.class);
            Encoding encoding = interfaze.getAnnotation(Encoding.class);
            GlobalInterceptor globalInterceptor = interfaze.getAnnotation(GlobalInterceptor.class);

            /* Methods defaults */
            Path path = interfaze.getAnnotation(Path.class);
            org.codegist.crest.annotate.Param param = interfaze.getAnnotation(org.codegist.crest.annotate.Param.class);
            org.codegist.crest.annotate.Params params = interfaze.getAnnotation(org.codegist.crest.annotate.Params.class);
            SocketTimeout socketTimeout = interfaze.getAnnotation(SocketTimeout.class);
            ConnectionTimeout connectionTimeout = interfaze.getAnnotation(ConnectionTimeout.class);
            RequestInterceptor interceptor = interfaze.getAnnotation(RequestInterceptor.class);
            ResponseHandler responseHandler = interfaze.getAnnotation(ResponseHandler.class);
            ErrorHandler errorHandler = interfaze.getAnnotation(ErrorHandler.class);
            RetryHandler retryHandler = interfaze.getAnnotation(RetryHandler.class);
            HttpMethod httpMethod = interfaze.getAnnotation(HttpMethod.class);

            /* Params defaults */
            Serializer serializer = interfaze.getAnnotation(Serializer.class);
            Name name = interfaze.getAnnotation(Name.class);
            Destination destination = interfaze.getAnnotation(Destination.class);
            Injector injector = interfaze.getAnnotation(Injector.class);

            ConfigBuilders.InterfaceConfigBuilder config = new ConfigBuilders.InterfaceConfigBuilder(interfaze, endPoint.value(), context.getProperties());
            if(contextPath != null) config.setContextPath(contextPath.value());
            if(encoding != null) config.setEncoding(encoding.value());
            if(globalInterceptor != null) config.setGlobalInterceptor(globalInterceptor.value());

            if(param != null) {
                config.addMethodsStaticParam(param.name(), param.value(), param.dest());
            }
            if(params != null) {
                for(org.codegist.crest.annotate.Param p : params.value()){
                    config.addMethodsStaticParam(p.name(), p.value(), p.dest());
                }
            }
            
            if(path != null) config.setMethodsPath(path.value());
            if(socketTimeout != null) config.setMethodsSocketTimeout(socketTimeout.value());
            if(connectionTimeout != null) config.setMethodsConnectionTimeout(connectionTimeout.value());
            if(interceptor != null) config.setMethodsRequestInterceptor(interceptor.value());
            if(responseHandler != null) config.setMethodsResponseHandler(responseHandler.value());
            if(errorHandler != null) config.setMethodsErrorHandler(errorHandler.value());
            if(retryHandler != null) config.setMethodsRetryHandler(retryHandler.value());
            if(httpMethod != null) config.setMethodsHttpMethod(httpMethod.value());

            if(serializer != null) config.setParamsSerializer(serializer.value());
            if(name != null) config.setParamsName(name.value());
            if(destination != null) config.setParamsDestination(destination.value());
            if(injector != null) config.setParamsInjector(injector.value());


            for (Method meth : interfaze.getDeclaredMethods()) {
                /* Methods specifics */
                path = meth.getAnnotation(Path.class);
                param = meth.getAnnotation(org.codegist.crest.annotate.Param.class);
                params = meth.getAnnotation(org.codegist.crest.annotate.Params.class);
                socketTimeout = meth.getAnnotation(SocketTimeout.class);
                connectionTimeout = meth.getAnnotation(ConnectionTimeout.class);
                interceptor = meth.getAnnotation(RequestInterceptor.class);
                responseHandler = meth.getAnnotation(ResponseHandler.class);
                errorHandler = meth.getAnnotation(ErrorHandler.class);
                retryHandler = meth.getAnnotation(RetryHandler.class);
                httpMethod = meth.getAnnotation(HttpMethod.class);

                /* Params defaults */
                serializer = meth.getAnnotation(Serializer.class);
                name = meth.getAnnotation(Name.class);
                destination = meth.getAnnotation(Destination.class);
                injector = meth.getAnnotation(Injector.class);

                ConfigBuilders.MethodConfigBuilder methodConfigBuilder = config.startMethodConfig(meth);

                if(param != null) {
                    methodConfigBuilder.addStaticParam(param.name(), param.value(), param.dest());
                }
                if(params != null) {
                    for(org.codegist.crest.annotate.Param p : params.value()){
                        methodConfigBuilder.addStaticParam(p.name(), p.value(), p.dest());
                    }
                }

                if(path != null) methodConfigBuilder.setPath(path.value());
                if(socketTimeout != null) methodConfigBuilder.setSocketTimeout(socketTimeout.value());
                if(connectionTimeout != null) methodConfigBuilder.setConnectionTimeout(connectionTimeout.value());
                if(interceptor != null) methodConfigBuilder.setRequestInterceptor(interceptor.value());
                if(responseHandler != null) methodConfigBuilder.setResponseHandler(responseHandler.value());
                if(errorHandler != null) methodConfigBuilder.setErrorHandler(errorHandler.value());
                if(retryHandler != null) methodConfigBuilder.setRetryHandler(retryHandler.value());
                if(httpMethod != null) methodConfigBuilder.setHttpMethod(httpMethod.value());

                if(name != null) methodConfigBuilder.setParamsName(name.value());
                if(destination != null) methodConfigBuilder.setParamsDestination(destination.value());
                if(serializer != null) methodConfigBuilder.setParamsSerializer(serializer.value());
                if(injector != null) methodConfigBuilder.setParamsInjector(injector.value());

                for(int i = 0, max = meth.getParameterTypes().length; i < max ; i++){
                    Map<Class<? extends Annotation>,Annotation> paramAnnotations = Methods.getParamsAnnotation(meth, i);
                    //Class<? extends RequestInjector> typeInjector = RequestInjectors.getAnnotatedInjectorFor(meth.getParameterTypes()[i]);
                    ConfigBuilders.ParamConfigBuilder paramConfigBuilder = methodConfigBuilder.startParamConfig(i);

                    // Injects user type annotated config.
                    Configs.injectAnnotatedConfig(paramConfigBuilder, meth.getParameterTypes()[i]);

                    /* Params specifics - Override user annotated config */
                    serializer = (Serializer) paramAnnotations.get(Serializer.class);
                    name = (Name) paramAnnotations.get(Name.class);
                    destination = (org.codegist.crest.annotate.Destination) paramAnnotations.get(Destination.class);
                    injector = (Injector) paramAnnotations.get(Injector.class);

                    if(serializer != null) paramConfigBuilder.setSerializer(serializer.value());
                    if(name != null) paramConfigBuilder.setName(name.value());
                    if(destination != null) paramConfigBuilder.setDestination(destination.value());
                    if(injector != null) paramConfigBuilder.setInjector(injector.value());

                    paramConfigBuilder.endParamConfig();
                }

                methodConfigBuilder.endMethodConfig();
            }

            return config.build();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigFactoryException(e);
        }
    }
}
