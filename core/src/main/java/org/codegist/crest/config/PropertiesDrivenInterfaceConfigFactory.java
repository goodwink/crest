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

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.Strings;
import org.codegist.common.reflect.Methods;
import org.codegist.crest.CRestContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static org.codegist.common.lang.Strings.defaultIfBlank;
import static org.codegist.common.lang.Strings.isBlank;


/**
 * <p>Properties based config factory of any possible interfaces given to the factory.
 * <p>Usefull when the end-point should be read externally instead, eg for profil (dev,integration,prod)
 * <p>Expected format for a single Interface config is of the following :
 * <p>- Any property not specified as mandatory is optional.
 * <p>- The same logic as the annotation config applies here, config fallbacks from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link ParamConfig}).
 * <code><pre>
 * package my.rest.interface;
 * class Interface {
 *     String get();
 *     String get(String s);
 *     void push(String s);
 * }
 * -----------------------------------------------
 * service.test.class=my.rest.interface.Interface  #Mandatory if no global server is configured
 * # interface specifics configs
 * service.test.end-point=http://localhost:8080    #Mandatory if no global server is configured
 * service.test.context-path=/my-path
 * service.test.encoding=utf-8
 * service.test.global-interceptor=my.rest.interceptor.MyRequestInterceptor
 * # default interface method configs
 * service.test.path=/hello
 * service.test.static-param.my-param-name.value=value1
 * service.test.static-param.my-param-name.destination=BODY
 * service.test.http-method=DELETE
 * service.test.socket-timeout=1
 * service.test.connection-timeout=2
 * service.test.request-interceptor=my.rest.MyRequestHandler2
 * service.test.response-handler=my.rest.MyResponseHandler
 * service.test.error-handler=my.rest.MyErrorHandler
 * service.test.retry-handler=my.rest.MyRetryHandler
 * # default method param configs
 * service.test.name=name
 * service.test.destination=BODY
 * service.test.serializer=my.rest.serializer.MyParamSerializer
 * service.test.injector=my.rest.injector.MyRequestInjector
 * <p/>
 * service.test.method.m1.pattern=get\\(.*\\)  #Pattern matching one or more methods. Config will apply to all matched method. Applies to "String get()" amd "String get(String)"
 * # method specifics configs
 * service.test.method.m1.path=/get
 * service.test.method.m1.static-param.my-param-name-2.value=value
 * service.test.method.m1.static-param.my-param-name-2.destination=HEADER
 * service.test.method.m1.http-method=PUT
 * service.test.method.m1.socket-timeout=3
 * service.test.method.m1.connection-timeout=4
 * service.test.method.m1.request-interceptor=my.rest.interceptor.MyRequestInterceptor2
 * service.test.method.m1.response-handler=my.rest.MyResponseHandler2
 * service.test.method.m1.error-handler=my.rest.MyErrorHandler2
 * # default param configs
 * service.test.method.m1.name=name1
 * service.test.method.m1.destination=URL
 * service.test.method.m1.injector=my.rest.injector.MyRequestInjector2
 * service.test.method.m1.serializer=my.rest.serializer.MyParamSerializer2
 *
 * # param specific configs
 * service.test.method.m1.params.0.name=a     # Param config applies also to all matched method as long as the method as enough param, otherwise is ignored.
 * service.test.method.m1.params.0.destination=URL
 * service.test.method.m1.params.0.serializer=my.rest.serializer.MyParamSerializer3
 * service.test.method.m1.params.0.injector=my.rest.interceptor.MyRequestInterceptor3
 * <p/>
 * service.test.method.m2.pattern=push\\(\\)
 * service.test.method.m2.path=/push
 * (...)
 * service.test2.class=my.rest.interface.Interface2
 * (...)
 * </pre></code>
 * <p>Can contain as much interface config as needed in a single Properties (or Map) object.
 * <p>A shortcut to configure the server for all interfaces is :
 * <code><pre>
 * service.end-point=My server url
 * </pre></code>
 * <p>The interface specific end-point if specified override the global one.
 *
 * @see org.codegist.crest.config.InterfaceConfig
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class PropertiesDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    //TODO this class would need a nice clean up....
    private final Map<String, String> properties;
    private final boolean useDefaults;

    public PropertiesDrivenInterfaceConfigFactory(Map<String, String> properties) {
        this(properties, true);
    }

    public PropertiesDrivenInterfaceConfigFactory(Map<String, String> properties, boolean useDefaults) {
        this.properties = Maps.unmodifiable(properties);
        this.useDefaults = useDefaults;
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        try {
            String globalServer = getServiceGlobalProp("end-point");
            String serviceAlias = getClassAlias(interfaze);
            String server = defaultIfBlank(getServiceProp(serviceAlias, "end-point"), globalServer);
            if (isBlank(server)) throw new IllegalArgumentException("server not found!");

            ConfigBuilders.InterfaceConfigBuilder ricb = new ConfigBuilders.InterfaceConfigBuilder(interfaze, server, context.getProperties()).setIgnoreNullOrEmptyValues(true);
            ricb.setContextPath(getServiceProp(serviceAlias, "context-path"))
                    .setEncoding(getServiceProp(serviceAlias, "encoding"))
                    .setGlobalInterceptor(getServiceProp(serviceAlias, "global-interceptor"))
                    .setMethodsConnectionTimeout(getServiceProp(serviceAlias, "connection-timeout"))
                    .setMethodsSocketTimeout(getServiceProp(serviceAlias, "socket-timeout"))
                    .setMethodsResponseHandler(getServiceProp(serviceAlias, "response-handler"))
                    .setMethodsErrorHandler(getServiceProp(serviceAlias, "error-handler"))
                    .setMethodsRetryHandler(getServiceProp(serviceAlias, "retry-handler"))
                    .setMethodsRequestInterceptor(getServiceProp(serviceAlias, "request-interceptor"))
                    .setMethodsPath(getServiceProp(serviceAlias, "path"))
                    .setMethodsHttpMethod(getServiceProp(serviceAlias, "http-method"))
                    .setParamsName(getServiceProp(serviceAlias, "name"))
                    .setParamsDestination(getServiceProp(serviceAlias, "destination"))
                    .setParamsSerializer(getServiceProp(serviceAlias, "serializer"))
                    .setParamsInjector(getServiceProp(serviceAlias, "injector"));

            String[] paramPrefixes = getStaticParamsPrefix(serviceAlias);
            for(String prefix : paramPrefixes){
                String name = prefix.substring(prefix.lastIndexOf(".") + 1);
                String destination = getProp(prefix, "destination");
                String value = getProp(prefix, "value");
                Destination dest = Strings.isBlank(destination) ? Destination.URL : Destination.valueOf(destination);
                ricb.addMethodsStaticParam(name, value, dest);
            }

            String[][] metPatterns = getMethodPatterns(serviceAlias);

            for (Method method : interfaze.getDeclaredMethods()) {
                ConfigBuilders.MethodConfigBuilder mcb = ricb.startMethodConfig(method).setIgnoreNullOrEmptyValues(true);
                String methAlias = null;
                for (String[] pattern : metPatterns) {
                    methAlias = pattern[0];
                    String methPattern = pattern[1];
                    Method[] methods = Methods.getDeclaredMethodsThatMatches(interfaze, methPattern, true);
                    if (Arrays.asList(methods).contains(method)) {

                        String[] mparamPrefixes = getStaticParamsPrefix(serviceAlias, methAlias);
                        for(String prefix : mparamPrefixes){
                            String name = prefix.substring(prefix.lastIndexOf(".") + 1);
                            String destination = getProp(prefix, "destination");
                            String value = getProp(prefix, "value");
                            Destination dest = Strings.isBlank(destination) ? Destination.URL : Destination.valueOf(destination);
                            mcb.addStaticParam(name, value, dest);
                        }

                        mcb.setPath(getMethodProp(serviceAlias, methAlias, "path"))
                                .setHttpMethod(getMethodProp(serviceAlias, methAlias, "http-method"))
                                .setSocketTimeout(getMethodProp(serviceAlias, methAlias, "socket-timeout"))
                                .setConnectionTimeout(getMethodProp(serviceAlias, methAlias, "connection-timeout"))
                                .setRequestInterceptor(getMethodProp(serviceAlias, methAlias, "request-interceptor"))
                                .setResponseHandler(getMethodProp(serviceAlias, methAlias, "response-handler"))
                                .setErrorHandler(getMethodProp(serviceAlias, methAlias, "error-handler"))
                                .setRetryHandler(getMethodProp(serviceAlias, methAlias, "retry-handler"))
                                .setParamsName(getMethodProp(serviceAlias, methAlias, "name"))
                                .setParamsDestination(getMethodProp(serviceAlias, methAlias, "destination"))
                                .setParamsSerializer(getMethodProp(serviceAlias, methAlias, "serializer"))
                                .setParamsInjector(getMethodProp(serviceAlias, methAlias, "injector"));
                        break;
                    }
                }
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    ConfigBuilders.ParamConfigBuilder pcb = mcb.startParamConfig(i).setIgnoreNullOrEmptyValues(true);
                    // Injects user type annotated config.
                    Configs.injectAnnotatedConfig(pcb, method.getParameterTypes()[i]);

                    pcb.setName(getParamProp(serviceAlias, methAlias, i, "name"))
                        .setDestination(getParamProp(serviceAlias, methAlias, i, "destination"))
                        .setInjector(getParamProp(serviceAlias, methAlias, i, "injector"))
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

    private String getProp(String prefix, String prop) {
        Object o = properties.get(prefix + "." + prop);
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
        for (Map.Entry<String, String> entry : properties.entrySet()) {

            if (entry.getKey().startsWith(servicePrefix)
                    && entry.getKey().endsWith(".pattern")) {
                String methodAlias = entry.getKey().substring((servicePrefix + ".method.").length());
                methodAlias = methodAlias.substring(0, methodAlias.length() - ".pattern".length());
                patterns.add(new String[]{
                        methodAlias,
                        entry.getValue()
                });
            }
        }
        return patterns.toArray(new String[patterns.size()][2]);
    }
    private String[] getStaticParamsPrefix(String serviceAlias) {
        String prefix = getServiceStaticParamPrefix(serviceAlias);
        Map<String,String> ps = Maps.extractByPattern(properties, Pattern.quote(prefix) + "\\..*");
        Set<String> names = new LinkedHashSet<String>();
        for (Map.Entry<String, String> entry : ps.entrySet()) {
            String paramName = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
            names.add(paramName);
        }

        return names.toArray(new String[names.size()]);
    }
    private String[] getStaticParamsPrefix(String serviceAlias, String methodPrefix) {
        String prefix = getMethodStaticParamPrefix(serviceAlias, methodPrefix);
        Map<String,String> ps = Maps.extractByPattern(properties, Pattern.quote(prefix) + "\\..*");
        Set<String> names = new LinkedHashSet<String>();
        for (Map.Entry<String, String> entry : ps.entrySet()) {
            String paramName = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
            names.add(paramName);
        }

        return names.toArray(new String[names.size()]);
    }

    private static String getMethodStaticParamPrefix(String serviceAlias, String methodAlias) {
        return getMethodPrefix(serviceAlias, methodAlias) + ".static-param";
    }
    private static String getServiceStaticParamPrefix(String serviceAlias) {
        return getServicePrefix(serviceAlias) + ".static-param";
    }

    private static String getMethodPrefix(String serviceAlias, String methodAlias) {
        return getServicePrefix(serviceAlias) + ".method." + methodAlias;
    }

    private static String getParamPrefix(String serviceAlias, String methodAlias, int index) {
        return getMethodPrefix(serviceAlias, methodAlias) + ".params." + index;
    }

    private String getClassAlias(Class c) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().startsWith("service.")
                    && entry.getKey().endsWith(".class")
                    && entry.getValue().equals(c.getName())
                    ) {
                String val = entry.getKey().substring("service.".length());
                return val.substring(0, val.length() - ".class".length());
            }
        }
        return null;
    }
}
