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
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.annotate.RestParam;
import org.codegist.crest.annotate.exploded.*;
import org.codegist.crest.annotate.exploded.Destination;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.injector.RequestInjectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExplodedAnnotationDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

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
            SocketTimeout socketTimeout = interfaze.getAnnotation(SocketTimeout.class);
            ConnectionTimeout connectionTimeout = interfaze.getAnnotation(ConnectionTimeout.class);
            RequestInterceptor interceptor = interfaze.getAnnotation(RequestInterceptor.class);
            ResponseHandler responseHandler = interfaze.getAnnotation(ResponseHandler.class);
            ErrorHandler errorHandler = interfaze.getAnnotation(ErrorHandler.class);
            HttpMethod httpMethod = interfaze.getAnnotation(HttpMethod.class);

            /* Params defaults */
            Serializer serializer = interfaze.getAnnotation(Serializer.class);
            Name name = interfaze.getAnnotation(Name.class);
            Destination destination = interfaze.getAnnotation(Destination.class);
            Injector injector = interfaze.getAnnotation(Injector.class);

            ConfigBuilders.InterfaceConfigBuilder config = new ConfigBuilders.InterfaceConfigBuilder(interfaze, endPoint.value(), context.getProperties());
            if(contextPath != null) config.setPath(contextPath.value());
            if(socketTimeout != null) config.setMethodsSocketTimeout(socketTimeout.value());
            if(connectionTimeout != null) config.setMethodsConnectionTimeout(connectionTimeout.value());
            if(encoding != null) config.setEncoding(encoding.value());
            if(globalInterceptor != null) config.setRequestInterceptor(globalInterceptor.value());
            if(serializer != null) config.setParamsSerializer(serializer.value());
            if(name != null) config.setParamsName(name.value());
            if(destination != null) config.setParamsDestination(destination.value());
            if(injector != null) config.setParamsInjector(injector.value());
            if(responseHandler != null) config.setMethodsResponseHandler(responseHandler.value());
            if(errorHandler != null) config.setMethodsErrorHandler(errorHandler.value());
            if(interceptor != null) config.setMethodsRequestInterceptor(interceptor.value());
            if(path != null) config.setMethodsPath(path.value());
            if(httpMethod != null) config.setMethodsHttpMethod(httpMethod.value());

            for (Method meth : interfaze.getDeclaredMethods()) {
                /* Methods specifics */
                path = meth.getAnnotation(Path.class);
                socketTimeout = meth.getAnnotation(SocketTimeout.class);
                connectionTimeout = meth.getAnnotation(ConnectionTimeout.class);
                interceptor = meth.getAnnotation(RequestInterceptor.class);
                responseHandler = meth.getAnnotation(ResponseHandler.class);
                errorHandler = meth.getAnnotation(ErrorHandler.class);
                httpMethod = meth.getAnnotation(HttpMethod.class);

                /* Params defaults */
                serializer = interfaze.getAnnotation(Serializer.class);
                name = interfaze.getAnnotation(Name.class);
                destination = interfaze.getAnnotation(Destination.class);
                injector = interfaze.getAnnotation(Injector.class);

                ConfigBuilders.MethodConfigBuilder methodConfigBuilder = config.startMethodConfig(meth);

                if(path != null) methodConfigBuilder.setPath(path.value());
                if(httpMethod != null) methodConfigBuilder.setHttpMethod(httpMethod.value());
                if(socketTimeout != null) methodConfigBuilder.setSocketTimeout(socketTimeout.value());
                if(connectionTimeout != null) methodConfigBuilder.setConnectionTimeout(connectionTimeout.value());
                if(name != null) methodConfigBuilder.setParamsName(name.value());
                if(destination != null) methodConfigBuilder.setParamsDestination(destination.value());
                if(serializer != null) methodConfigBuilder.setParamsSerializer(serializer.value());
                if(injector != null) methodConfigBuilder.setParamsInjector(injector.value());
                if(interceptor != null) methodConfigBuilder.setRequestInterceptor(interceptor.value());
                if(responseHandler != null) methodConfigBuilder.setResponseHandler(responseHandler.value());
                if(errorHandler != null) methodConfigBuilder.setErrorHandler(errorHandler.value());

                for(int i = 0, max = meth.getParameterTypes().length; i < max ; i++){
                    Map<Class,Annotation> paramAnnotations = Methods.getParamsAnnotation(meth, i);
                    Class<? extends RequestInjector> typeInjector = RequestInjectors.getAnnotatedInjectorFor(meth.getParameterTypes()[i]);
                    ConfigBuilders.ParamConfigBuilder paramConfigBuilder = methodConfigBuilder.startParamConfig(i);

                    /* Params specifics */
                    serializer = (Serializer) paramAnnotations.get(Serializer.class);
                    name = (Name) paramAnnotations.get(Name.class);
                    destination = (Destination) paramAnnotations.get(Destination.class);
                    injector = (Injector) paramAnnotations.get(Injector.class);

                    if(serializer != null) paramConfigBuilder.setSerializer(serializer.value());
                    if(name != null) paramConfigBuilder.setName(name.value());
                    if(destination != null) paramConfigBuilder.setDestination(destination.value());

                    if(injector != null) {
                        paramConfigBuilder.setInjector(Configs.chooseInjector(typeInjector, injector.value()));
                    }else if(typeInjector != null) {
                        paramConfigBuilder.setInjector(typeInjector);
                    }


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
