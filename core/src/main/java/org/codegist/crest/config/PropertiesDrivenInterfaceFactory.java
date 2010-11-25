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
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.injector.RequestInjectors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.codegist.common.lang.Strings.defaultIfBlank;
import static org.codegist.common.lang.Strings.isBlank;


/**
 * <p>Properties based config factory of any possible interfaces given to the factory.
 * <p>Usefull when the end-point should be read externally instead, eg for profil (dev,integration,prod)
 * <p>Expected format for a single Interface config is of the following :
 * <p>- Any property not specified as mandatory is optional.
 * <p>- The same logic as the annotation config applies here, config fallback from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link PropertiesDrivenInterfaceFactory}).
 * <code><pre>
 * package my.rest.interface;
 * class Interface {
 *     String get();
 *     String get(String s);
 *     void push(String s);
 * }
 * -----------------------------------------------
 * service.test.class=my.rest.interface.Interface  #Mandatory if no global server is configured
 * service.test.end-point=http://localhost:8080    #Mandatory if no global server is configured
 * service.test.path=/my-path
 * service.test.socket-timeout=1
 * service.test.connection-timeout=2
 * service.test.encoding=utf-8
 * service.test.request-interceptor=my.rest.interceptor.MyRequestInterceptor
 * service.test.response-handler=my.rest.MyResponseHandler
 * service.test.error-handler=my.rest.MyErrorHandler
 * service.test.method-path=/hello
 * service.test.http-method=DELETE
 * service.test.param-name=name
 * service.test.param-destination=BODY
 * service.test.param-serializer=my.rest.serializer.MyParamSerializer
 * service.test.param-injector=my.rest.injector.MyRequestInjector
 * <p/>
 * service.test.method.m1.pattern=get\\(.*\\)  #Pattern matching one or more methods. Config will apply to all matched method. Applies to "String get()" amd "String get(String)"
 * service.test.method.m1.path=/get
 * service.test.method.m1.http-method=PUT
 * service.test.method.m1.socket-timeout=3
 * service.test.method.m1.connection-timeout=4
 * service.test.method.m1.param-name=name1
 * service.test.method.m1.param-destination=URL
 * service.test.method.m1.param-injector=my.rest.injector.MyRequestInjector2
 * service.test.method.m1.request-interceptor=my.rest.interceptor.MyRequestInterceptor2
 * service.test.method.m1.response-handler=my.rest.MyResponseHandler2
 * service.test.method.m1.error-handler=my.rest.MyErrorHandler2
 * service.test.method.m1.param-serializer=my.rest.serializer.MyParamSerializer2
 * service.test.method.m1.params.0.name=a     # Param config applies also to all matched method as long as the method as enough param, otherwise is ignored.
 * service.test.method.m1.params.0.destination=URL
 * service.test.method.m1.params.0.serializer=my.rest.serializer.MyParamSerializer3
 * service.test.method.m1.params.0.injector=my.rest.interceptor.MyRequestInterceptor3
 * <p/>
 * service.test.method.m2.pattern=push\\(\\)
 * service.test.method.m2.path=/push
 * service.test.method.m2.http-method=PUT
 * service.test.method.m2.socket-timeout=5
 * service.test.method.m2.connection-timeout=6
 * service.test.method.m2.param-name=name1
 * service.test.method.m2.param-destination=URL
 * service.test.method.m2.param-injector=my.rest.injector.MyRequestInjector2
 * service.test.method.m2.request-interceptor=my.rest.interceptor.MyRequestInterceptor2
 * service.test.method.m2.response-handler=my.rest.MyResponseHandler2
 * service.test.method.m2.error-handler=my.rest.MyErrorHandler2
 * service.test.method.m2.param-serializer=my.rest.serializer.MyParamSerializer2
 * service.test.method.m2.params.0.name=a
 * service.test.method.m2.params.0.destination=URL
 * service.test.method.m2.params.0.serializer=my.rest.serializer.MyParamSerializer3
 * service.test.method.m2.params.0.injector=my.rest.interceptor.MyRequestInterceptor3
 * </pre></code>
 * <p>Can contain as much interface config as needed in a single Properties (or Map) object.
 * <p>A shortcut to configure the server for all interfaces is :
 * <code><pre>
 * service.end-point=My server url
 * </pre></code>
 * <p>The interface specific end-point if specified override the global one.
 * <p>NB : the factory looks up all the method argument for the {@link org.codegist.crest.annotate.RestInjector} annotation in order to autoconfigure a param injector if not specified in the properties.
 *
 * @see org.codegist.crest.config.InterfaceConfig
 * @see org.codegist.crest.annotate.RestInjector
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class PropertiesDrivenInterfaceFactory implements InterfaceConfigFactory {

    private final Map<Object, Object> properties;
    private final boolean useDefaults;

    public PropertiesDrivenInterfaceFactory(Map properties) {
        this(properties, true);
    }

    public PropertiesDrivenInterfaceFactory(Map properties, boolean useDefaults) {
        this.properties = properties;
        this.useDefaults = useDefaults;
    }

    @Override
    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        try {
            String globalServer = getServiceGlobalProp("end-point");
            String serviceAlias = getClassAlias(interfaze);
            String server = defaultIfBlank(getServiceProp(serviceAlias, "end-point"), globalServer);
            if (isBlank(server)) throw new IllegalArgumentException("server not found!");

            ConfigBuilders.InterfaceConfigBuilder ricb = new ConfigBuilders.InterfaceConfigBuilder(interfaze, server, context.getProperties()).setIgnoreNullOrEmptyValues(true);
            ricb.setPath(getServiceProp(serviceAlias, "path"))
                    .setMethodsConnectionTimeout(getServiceProp(serviceAlias, "methods-connection-timeout"))
                    .setMethodsSocketTimeout(getServiceProp(serviceAlias, "methods-socket-timeout"))
                    .setEncoding(getServiceProp(serviceAlias, "encoding"))
                    .setRequestInterceptor(getServiceProp(serviceAlias, "request-interceptor"))
                    .setMethodsResponseHandler(getServiceProp(serviceAlias, "methods-response-handler"))
                    .setMethodsErrorHandler(getServiceProp(serviceAlias, "methods-error-handler"))
                    .setMethodsRequestInterceptor(getServiceProp(serviceAlias, "methods-request-interceptor"))
                    .setMethodsPath(getServiceProp(serviceAlias, "methods-path"))
                    .setMethodsHttpMethod(getServiceProp(serviceAlias, "methods-http-method"))
                    .setParamsName(getServiceProp(serviceAlias, "params-name"))
                    .setParamsDestination(getServiceProp(serviceAlias, "params-destination"))
                    .setParamsSerializer(getServiceProp(serviceAlias, "params-serializer"))
                    .setParamsInjector(getServiceProp(serviceAlias, "params-injector"));

            String[][] metPatterns = getMethodPatterns(serviceAlias);

            for (Method method : interfaze.getDeclaredMethods()) {
                ConfigBuilders.MethodConfigBuilder mcb = ricb.startMethodConfig(method).setIgnoreNullOrEmptyValues(true);
                String methAlias = null;
                for (String[] pattern : metPatterns) {
                    methAlias = pattern[0];
                    String methPattern = pattern[1];
                    Method[] methods = Methods.getDeclaredMethodsThatMatches(interfaze, methPattern, true);
                    if (Arrays.asList(methods).contains(method)) {
                        mcb.setPath(getMethodProp(serviceAlias, methAlias, "path"))
                                .setHttpMethod(getMethodProp(serviceAlias, methAlias, "http-method"))
                                .setSocketTimeout(getMethodProp(serviceAlias, methAlias, "socket-timeout"))
                                .setConnectionTimeout(getMethodProp(serviceAlias, methAlias, "connection-timeout"))
                                .setRequestInterceptor(getMethodProp(serviceAlias, methAlias, "request-interceptor"))
                                .setResponseHandler(getMethodProp(serviceAlias, methAlias, "response-handler"))
                                .setErrorHandler(getMethodProp(serviceAlias, methAlias, "error-handler"))
                                .setParamsName(getMethodProp(serviceAlias, methAlias, "params-name"))
                                .setParamsDestination(getMethodProp(serviceAlias, methAlias, "params-destination"))
                                .setParamsSerializer(getMethodProp(serviceAlias, methAlias, "params-serializer"))
                                .setParamsInjector(getMethodProp(serviceAlias, methAlias, "params-injector"));
                        break;
                    }
                }
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    Class<? extends RequestInjector> typeInjector = RequestInjectors.getAnnotatedInjectorFor(method.getParameterTypes()[i]);
                    mcb.startParamConfig(i).setIgnoreNullOrEmptyValues(true)
                            .setName(getParamProp(serviceAlias, methAlias, i, "name"))
                            .setDestination(getParamProp(serviceAlias, methAlias, i, "destination"))
                            .setInjector(Configs.chooseInjector(typeInjector, getParamProp(serviceAlias, methAlias, i, "injector")))
                            .setSerializer(getParamProp(serviceAlias, methAlias, i, "serializer"))
                            .endParamConfig();
                }
                mcb.endMethodConfig();
            }

            return ricb.build(useDefaults);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigFactoryException(e);
        }
    }

    private String getServiceGlobalProp(String prop) {
        Object o = properties.get("service." + prop);
        return o != null ? o.toString() : null;
    }

    private String getServiceProp(String serviceAlias, String prop) {
        Object o = properties.get(getServicePropKey(serviceAlias, prop));
        return o != null ? o.toString() : null;
    }

    private String getMethodProp(String serviceAlias, String methodAlias, String prop) {
        Object o = properties.get(getMethodPropKey(serviceAlias, methodAlias, prop));
        return o != null ? o.toString() : null;
    }

    private String getParamProp(String serviceAlias, String methodAlias, int paramIndex, String prop) {
        Object o = properties.get(getParamPropKey(serviceAlias, methodAlias, paramIndex, prop));
        return o != null ? o.toString() : null;
    }

    private static String getServicePropKey(String serviceAlias, String prop) {
        return getServicePrefix(serviceAlias) + "." + prop;
    }

    private static String getMethodPropKey(String serviceAlias, String method, String prop) {
        return getMethodPrefix(serviceAlias, method) + "." + prop;
    }

    private static String getParamPropKey(String serviceAlias, String method, int index, String prop) {
        return getParamPrefix(serviceAlias, method, index) + "." + prop;
    }

    private static String getServicePrefix(String serviceAlias) {
        return "service." + serviceAlias;
    }

    private String[][] getMethodPatterns(String serviceAlias) {
        String servicePrefix = getServicePrefix(serviceAlias);
        List<String[]> patterns = new ArrayList<String[]>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith(servicePrefix)
                    && entry.getKey().toString().endsWith(".pattern")) {
                String methodAlias = entry.getKey().toString().substring((servicePrefix + ".method.").length());
                methodAlias = methodAlias.substring(0, methodAlias.length() - ".pattern".length());
                patterns.add(new String[]{
                        methodAlias,
                        entry.getValue().toString()
                });
            }
        }
        return patterns.toArray(new String[patterns.size()][2]);
    }

    private static String getMethodPrefix(String serviceAlias, String methodAlias) {
        return getServicePrefix(serviceAlias) + ".method." + methodAlias;
    }

    private static String getParamPrefix(String serviceAlias, String methodAlias, int index) {
        return getMethodPrefix(serviceAlias, methodAlias) + ".params." + index;
    }

    private String getClassAlias(Class c) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith("service.")
                    && entry.getKey().toString().endsWith(".class")
                    && entry.getValue().toString().equals(c.getName())
                    ) {
                String val = entry.getKey().toString().substring("service.".length());
                return val.substring(0, val.length() - ".class".length());
            }
        }
        return null;
    }
}
