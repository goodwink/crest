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

import org.codegist.crest.CRestContext;
import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.annotate.RestParam;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.injector.RequestInjectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * <p>Annotation based config factory of any possible interfaces given to the factory.
 * <p>The factory will lookup the given interface for the following annotation :
 * <p>- {@link org.codegist.crest.annotate.RestApi}
 * <p>- {@link org.codegist.crest.annotate.RestMethod}
 * <p>- {@link org.codegist.crest.annotate.RestParam}
 * <p/>
 * <p>- Each config fallback from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link PropertiesDrivenInterfaceFactory}).
 * <p>NB : the factory looks up all the method argument for the {@link org.codegist.crest.annotate.RestInjector} annotation in order to autoconfigure a param injector if not specified in the properties.
 * <p>- {@link org.codegist.crest.annotate.RestInjector}
 *
 * @see org.codegist.crest.config.InterfaceConfig
 * @see org.codegist.crest.annotate.RestApi
 * @see org.codegist.crest.annotate.RestMethod
 * @see org.codegist.crest.annotate.RestParam
 * @see org.codegist.crest.annotate.RestInjector
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class AnnotationDrivenInterfaceConfigFactory implements InterfaceConfigFactory {


    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        try {
            RestApi restAPI = interfaze.getAnnotation(RestApi.class);
            if (restAPI == null)
                throw new IllegalArgumentException("RestAPI annotation (" + RestApi.class + ") not fould in interface (" + interfaze + ").");

            ConfigBuilders.InterfaceConfigBuilder config = new ConfigBuilders.InterfaceConfigBuilder(interfaze, restAPI.endPoint(), context != null ? context.getProperties() : null)
                    .setPath(restAPI.path())
                    .setMethodsSocketTimeout(restAPI.methodsSocketTimeout() == Fallbacks.FALLBACK_LONG ? null : restAPI.methodsSocketTimeout())
                    .setMethodsConnectionTimeout(restAPI.methodsConnectionTimeout() == Fallbacks.FALLBACK_LONG ? null : restAPI.methodsConnectionTimeout())
                    .setEncoding(restAPI.encoding())
                    .setRequestInterceptor(restAPI.requestInterceptor())
                    .setParamsSerializer(restAPI.paramsSerializer())
                    .setParamsName(restAPI.paramsName())
                    .setParamsDestination(restAPI.paramsDestination())
                    .setParamsInjector(restAPI.paramsInjector())
                    .setMethodsResponseHandler(restAPI.methodsResponseHandler())
                    .setMethodsErrorHandler(restAPI.methodsErrorHandler())
                    .setMethodsRequestInterceptor(restAPI.methodsRequestInterceptor())
                    .setMethodsPath(restAPI.methodsPath())
                    .setMethodsHttpMethod(restAPI.methodsHttpMethod());

            for (Method meth : interfaze.getDeclaredMethods()) {
                RestMethod restAPIMethod = meth.getAnnotation(RestMethod.class);
                ConfigBuilders.MethodConfigBuilder methodConfigBuilder = config.startMethodConfig(meth);

                if (restAPIMethod != null) {
                    methodConfigBuilder.setIgnoreNullOrEmptyValues(true)
                            .setPath(Fallbacks.FALLBACK_STRING.equals(restAPIMethod.path()) ? null : restAPIMethod.path())
                            .setHttpMethod(Fallbacks.FALLBACK_STRING.equals(restAPIMethod.method()) ? null : restAPIMethod.method())
                            .setSocketTimeout(restAPIMethod.socketTimeout() == Fallbacks.FALLBACK_LONG ? null : restAPIMethod.socketTimeout())
                            .setConnectionTimeout(restAPIMethod.connectionTimeout() == Fallbacks.FALLBACK_LONG ? null : restAPIMethod.connectionTimeout())
                            .setParamsName(Fallbacks.FALLBACK_STRING.equals(restAPIMethod.paramsName()) ? null : restAPIMethod.paramsName())
                            .setParamsDestination(Fallbacks.FALLBACK_STRING.equals(restAPIMethod.paramsDestination()) ? null : restAPIMethod.paramsDestination())
                            .setParamsSerializer(Fallbacks.FallbackSerializer.class.equals(restAPIMethod.paramsSerializer()) ? null : restAPIMethod.paramsSerializer())
                            .setParamsInjector(Fallbacks.FallbackRequestParameterInjector.class.equals(restAPIMethod.paramsInjector()) ? null : restAPIMethod.paramsInjector())
                            .setRequestInterceptor(Fallbacks.FallbackMethodInterceptor.class.equals(restAPIMethod.requestInterceptor()) ? null : restAPIMethod.requestInterceptor())
                            .setResponseHandler(Fallbacks.FallbackResponseHandler.class.equals(restAPIMethod.responseHandler()) ? null : restAPIMethod.responseHandler())
                            .setErrorHandler(Fallbacks.FallbackErrorHandler.class.equals(restAPIMethod.errorHandler()) ? null : restAPIMethod.errorHandler());
                }

                Annotation[][] annotations = meth.getParameterAnnotations();
                int i = 0;
                for (Annotation[] anns : annotations) {
                    boolean added = false;
                    Class<? extends RequestInjector> typeInjector = RequestInjectors.getAnnotatedInjectorFor(meth.getParameterTypes()[i]);
                    ConfigBuilders.ParamConfigBuilder paramConfigBuilder = methodConfigBuilder.startParamConfig(i);
                    for (Annotation anno : anns) {
                        if (anno instanceof RestParam) {
                            RestParam restParam = (RestParam) anno;
                            Class<? extends RequestInjector> interfaceInjector = Fallbacks.FallbackRequestParameterInjector.class.equals(restParam.injector()) ? null : restParam.injector();
                            paramConfigBuilder.setIgnoreNullOrEmptyValues(true)
                                    .setName(Fallbacks.FALLBACK_STRING.equals(restParam.name()) ? null : restParam.name())
                                    .setDestination(Fallbacks.FALLBACK_STRING.equals(restParam.destination()) ? null : restParam.destination())
                                    .setInjector(Configs.chooseInjector(typeInjector, interfaceInjector))
                                    .setSerializer(Fallbacks.FallbackSerializer.class.equals(restParam.serializer()) ? null : restParam.serializer());

                            added = true;
                            break;
                        }
                    }
                    if (typeInjector != null && !added) {
                        paramConfigBuilder.setInjector(typeInjector);
                    }
                    paramConfigBuilder.endParamConfig();
                    i++;
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
