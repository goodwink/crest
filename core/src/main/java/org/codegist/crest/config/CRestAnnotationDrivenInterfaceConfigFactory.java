/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.common.reflect.Methods;
import org.codegist.crest.CRestContext;
import org.codegist.crest.annotate.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Annotation based config factory of any possible interfaces given to the factory.
 * <p>The factory will lookup any annotation in package {@link org.codegist.crest.annotate} on to the given interface.
 * <p/>
 * <p>- Each config fallback from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link PropertiesDrivenInterfaceConfigFactory}).
 * @see org.codegist.crest.config.InterfaceConfig
 * @see org.codegist.crest.annotate
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class CRestAnnotationDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    private final boolean useDefaults;

    public CRestAnnotationDrivenInterfaceConfigFactory(boolean useDefaults) {
        this.useDefaults = useDefaults;
    }
    public CRestAnnotationDrivenInterfaceConfigFactory() {
        this(true);
    }

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
            RetryHandler retryHandler = interfaze.getAnnotation(RetryHandler.class);
            HttpMethod httpMethod = getHttpMethod(interfaze.getAnnotations(), interfaze.getAnnotation(HttpMethod.class));
            Set<BasicParamConfig> extraParams = getExtraParamConfigs(interfaze.getAnnotations());

            /* Params defaults */
            Serializer serializer = interfaze.getAnnotation(Serializer.class);
            Injector injector = interfaze.getAnnotation(Injector.class);

            ConfigBuilders.InterfaceConfigBuilder config = new ConfigBuilders.InterfaceConfigBuilder(interfaze, context.getProperties());
            if(endPoint != null) config.setEndPoint(endPoint.value());
            if(contextPath != null) config.setContextPath(contextPath.value());
            if(encoding != null) config.setEncoding(encoding.value());
            if(globalInterceptor != null) config.setGlobalInterceptor(globalInterceptor.value());
            if(extraParams != null) {
                for(BasicParamConfig c : extraParams){
                    config.addMethodsExtraParam(c.getName(), c.getDefaultValue(),c.getDestination());
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
            if(injector != null) config.setParamsInjector(injector.value());


            for (Method meth : interfaze.getDeclaredMethods()) {
                /* Methods specifics */
                path = meth.getAnnotation(Path.class);
                extraParams = getExtraParamConfigs(meth.getAnnotations());
                socketTimeout = meth.getAnnotation(SocketTimeout.class);
                connectionTimeout = meth.getAnnotation(ConnectionTimeout.class);
                interceptor = meth.getAnnotation(RequestInterceptor.class);
                responseHandler = meth.getAnnotation(ResponseHandler.class);
                errorHandler = meth.getAnnotation(ErrorHandler.class);
                retryHandler = meth.getAnnotation(RetryHandler.class);
                httpMethod = getHttpMethod(meth.getAnnotations(), meth.getAnnotation(HttpMethod.class));

                /* Params defaults */
                serializer = meth.getAnnotation(Serializer.class);
                injector = meth.getAnnotation(Injector.class);

                ConfigBuilders.MethodConfigBuilder methodConfigBuilder = config.startMethodConfig(meth);

                if(extraParams != null) {
                    for(BasicParamConfig c : extraParams){
                        methodConfigBuilder.startExtraParamConfig(c.getName())
                                .setDefaultValue(c.getDefaultValue())
                                .setDestination(c.getDestination())
                                .endParamConfig();
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
                    injector = (Injector) paramAnnotations.get(Injector.class);

                    if(serializer != null) paramConfigBuilder.setSerializer(serializer.value());
                    if(injector != null) paramConfigBuilder.setInjector(injector.value());

                    BasicParamConfig pconfig = getFirstExtraParamConfig(paramAnnotations.values().toArray(new Annotation[paramAnnotations.size()]));
                    paramConfigBuilder.setName(pconfig.getName());
                    paramConfigBuilder.setDestination(pconfig.getDestination());
                    paramConfigBuilder.setDefaultValue(pconfig.getDefaultValue());

                    paramConfigBuilder.endParamConfig();
                }

                methodConfigBuilder.endMethodConfig();
            }

            return config.build(useDefaults);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigFactoryException(e);
        }
    }

    
    private static BasicParamConfig getFirstExtraParamConfig(Annotation[] annotations){
        Set<BasicParamConfig> config = getExtraParamConfigs(annotations);
        if(config.isEmpty()) return new DefaultBasicParamConfig(null,null,null);
        return config.iterator().next();// get the first
    }
    private static Set<BasicParamConfig> getExtraParamConfigs(Annotation[] annotations){
        Set<BasicParamConfig> params = new LinkedHashSet<BasicParamConfig>();

        for(Annotation a : annotations){
            if(a instanceof FormParam) {
                FormParam p = (FormParam) a;
                params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.FORM));
            }else if(a instanceof FormParams) {
                FormParams ps = (FormParams) a;
                for(FormParam p : ps.value()){
                    params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.FORM));
                }
            }else if(a instanceof PathParam) {
                PathParam p = (PathParam) a;
                params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.PATH));
            }else if(a instanceof PathParams) {
                PathParams ps = (PathParams) a;
                for(PathParam p : ps.value()){
                    params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.PATH));
                }
            }else if(a instanceof QueryParam) {
                QueryParam p = (QueryParam) a;
                params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.QUERY));
            }else if(a instanceof QueryParams) {
                QueryParams ps = (QueryParams) a;
                for(QueryParam p : ps.value()){
                    params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.QUERY));
                }
            }else if(a instanceof HeaderParam) {
                HeaderParam p = (HeaderParam) a;
                params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.HEADER));
            }else if(a instanceof HeaderParams) {
                HeaderParams ps = (HeaderParams) a;
                for(HeaderParam p : ps.value()){
                    params.add(new DefaultBasicParamConfig(p.name(), p.value(), org.codegist.crest.config.Destination.HEADER));
                }
            }
        }
        return params;
    }

    private static HttpMethod getHttpMethod(Annotation[] annotations, HttpMethod def){
        for(Annotation a : annotations){
            HttpMethod meth = a.annotationType().getAnnotation(HttpMethod.class);
            if(meth != null) return meth;
        }
        return def;
    }
}
