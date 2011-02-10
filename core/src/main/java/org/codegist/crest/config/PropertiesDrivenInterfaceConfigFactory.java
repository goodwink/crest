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
import org.codegist.common.reflect.Methods;
import org.codegist.crest.CRestContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static org.codegist.common.lang.Strings.defaultIfBlank;


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
 * }
 * -----------------------------------------------
 * service.test.class=my.rest.interface.Interface
 * #interface specifics
 * service.test.end-point=http://localhost:8080  #mandatory
 * service.test.context-path=/my-path
 * service.test.encoding=utf-8
 * service.test.global-interceptor=my.rest.interface.MyRequestInterceptor1
 * #default methods
 * service.test.path=/hello
 * service.test.params.form.form-param=form-value
 * service.test.params.form.form-param1=form-value1
 * service.test.params.form.form-param2=form-value2
 * service.test.params.header.header-param=header-value
 * service.test.params.header.header-param1=header-value1
 * service.test.params.header.header-param2=header-value2
 * service.test.params.query.query-param=query-value
 * service.test.params.query.query-param1=query-value1
 * service.test.params.query.query-param2=query-value2
 * service.test.params.path.path-param=path-value
 * service.test.params.path.path-param1=path-value1
 * service.test.params.path.path-param2=path-value2
 * service.test.socket-timeout=1
 * service.test.connection-timeout=2
 * service.test.request-interceptor=my.rest.interface.MyRequestInterceptor1
 * service.test.response-handler=my.rest.interface.MyResponseHandler1
 * service.test.error-handler=my.rest.interface.MyErrorHandler1
 * service.test.retry-handler=my.rest.interface.MyRetryHandler1
 * service.test.http-method=DELETE
 * #default params
 * service.test.serializer=my.rest.interface.MySerializer1
 * service.test.injector=my.rest.interface.MyRequestParameterInjector1
 * 
 * service.test.method.m1.pattern=m1\\(\\)
 * #methods specifices
 * service.test.method.m1.path=/m1
 * service.test.method.m1.params.form.form-param=over-value1
 * service.test.method.m1.params.form.form-param3=new-value
 * service.test.method.m1.http-method=PUT
 * service.test.method.m1.socket-timeout=3
 * service.test.method.m1.connection-timeout=4
 * service.test.method.m1.request-interceptor=my.rest.interface.MyRequestInterceptor3
 * service.test.method.m1.response-handler=my.rest.interface.MyResponseHandler1
 * service.test.method.m1.error-handler=my.rest.interface.MyErrorHandler2
 * service.test.method.m1.retry-handler=my.rest.interface.MyRetryHandler2
 * #default params
 * service.test.method.m1.serializer=my.rest.interface.MySerializer3
 * service.test.method.m1.injector=my.rest.interface.MyRequestParameterInjector2
 * 
 * service.test.method.m2.pattern=m1\\(java\\.lang\\.String\\)
 * #methods specifices
 * service.test.method.m2.path=/m1
 * service.test.method.m2.params.path.form-param=over-value1
 * service.test.method.m2.http-method=POST
 * service.test.method.m2.socket-timeout=5
 * service.test.method.m2.connection-timeout=6
 * service.test.method.m2.request-interceptor=my.rest.interface.MyRequestInterceptor2
 * service.test.method.m2.response-handler=my.rest.interface.MyResponseHandler2
 * #default params
 * service.test.method.m2.serializer=my.rest.interface.MySerializer2
 * service.test.method.m2.injector=my.rest.interface.MyRequestParameterInjector2
 * 
 * service.test.method.m2.params.0.name=a  #mandatory
 * service.test.method.m2.params.0.type=header
 * service.test.method.m2.params.0.default=deff
 * service.test.method.m2.params.0.serializer=my.rest.interface.MySerializer3
 * service.test.method.m2.params.0.injector=my.rest.interface.MyRequestParameterInjector3
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
    private final boolean buildTemplates;

    public PropertiesDrivenInterfaceConfigFactory(Map<String, String> properties) {
        this(properties, false);
    }

    public PropertiesDrivenInterfaceConfigFactory(Map<String, String> properties, boolean buildTemplates) {
        this.properties = Maps.unmodifiable(properties);
        this.buildTemplates = buildTemplates;
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {
        try {
            String globalServer = getServiceGlobalProp("end-point");
            String serviceAlias = getClassAlias(interfaze);
            String endPoint = defaultIfBlank(getServiceProp(serviceAlias, "end-point"), globalServer);

            ConfigBuilders.InterfaceConfigBuilder ricb = new ConfigBuilders.InterfaceConfigBuilder(interfaze, context.getProperties()).setIgnoreNullOrEmptyValues(true);
            ricb    .setEndPoint(endPoint)
                    .setContextPath(getServiceProp(serviceAlias, "context-path"))
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
                    .setParamsSerializer(getServiceProp(serviceAlias, "serializer"))
                    .setParamsInjector(getServiceProp(serviceAlias, "injector"));

            Map<Destination, List<String[]>> params = getParams(serviceAlias);
            for(Map.Entry<Destination, List<String[]>> paramDest : params.entrySet()){
                Destination dest = paramDest.getKey();
                for(String[] param : paramDest.getValue()){
                    String name = param[0];
                    String value = param[1];
                    ricb.addMethodsExtraParam(name, value, dest.toString());
                }
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

                        Map<Destination, List<String[]>> mparamPrefixes = getParams(serviceAlias, methAlias);
                        for(Map.Entry<Destination, List<String[]>> paramDest : mparamPrefixes.entrySet()){
                            Destination dest = paramDest.getKey();
                            for(String[] param : paramDest.getValue()){
                                String name = param[0];
                                String value = param[1];
                                mcb.startExtraParamConfig(name)
                                        .setIgnoreNullOrEmptyValues(true)
                                        .setDestination(dest)
                                        .setDefaultValue(value)
                                        .endParamConfig();
                            }
                        }

                        mcb.setPath(getMethodProp(serviceAlias, methAlias, "path"))
                                .setHttpMethod(getMethodProp(serviceAlias, methAlias, "http-method"))
                                .setSocketTimeout(getMethodProp(serviceAlias, methAlias, "socket-timeout"))
                                .setConnectionTimeout(getMethodProp(serviceAlias, methAlias, "connection-timeout"))
                                .setRequestInterceptor(getMethodProp(serviceAlias, methAlias, "request-interceptor"))
                                .setResponseHandler(getMethodProp(serviceAlias, methAlias, "response-handler"))
                                .setErrorHandler(getMethodProp(serviceAlias, methAlias, "error-handler"))
                                .setRetryHandler(getMethodProp(serviceAlias, methAlias, "retry-handler"))
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
                        .setDestination(getParamProp(serviceAlias, methAlias, i, "type"))
                        .setDefaultValue(getParamProp(serviceAlias, methAlias, i, "default"))
                        .setInjector(getParamProp(serviceAlias, methAlias, i, "injector"))
                        .setSerializer(getParamProp(serviceAlias, methAlias, i, "serializer"))
                        .endParamConfig();
                }
                mcb.endMethodConfig();
            }

            return ricb.build(buildTemplates, true);
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

    private Map<Destination, List<String[]>> getParams(String serviceAlias) {
        String prefix = getServiceParamPrefix(serviceAlias);
        return getParamsByPrefix(prefix);
    }
    private Map<Destination, List<String[]>> getParams(String serviceAlias, String methodPrefix) {
        String prefix = getMethodParamPrefix(serviceAlias, methodPrefix);
        return getParamsByPrefix(prefix);
    }
    private Map<Destination,List<String[]>> getParamsByPrefix(String prefix){
        Map<String,String> ps = Maps.extractByPattern(properties, Pattern.quote(prefix) + "\\.[a-z]+\\..*");
        Map<Destination, List<String[]>> res = new HashMap<Destination, List<String[]>>();
        for (Map.Entry<String, String> entry : ps.entrySet()) {
            String[] split = entry.getKey().split("\\.");
            String paramName = split[split.length - 1];
            String type = split[split.length - 2];

            Destination dest = Destination.valueOf(type.toUpperCase());
            List<String[]> values = res.get(dest);
            if(values == null) {
                res.put(dest, values = new ArrayList<String[]>());
            }
            values.add(new String[]{paramName, entry.getValue()});
        }

        return res;
    }

    private static String getMethodParamPrefix(String serviceAlias, String methodAlias) {
        return getMethodPrefix(serviceAlias, methodAlias) + ".params";
    }
    private static String getServiceParamPrefix(String serviceAlias) {
        return getServicePrefix(serviceAlias) + ".params";
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
