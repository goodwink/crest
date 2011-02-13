/*
 * Copyright 2010 CodeGist.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.common.lang.Strings;
import org.codegist.common.reflect.Methods;
import org.codegist.crest.CRestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.codegist.common.lang.Strings.isBlank;

/**
 * <p>Xml based config factory of any possible interfaces given to the factory.
 * <p>Usefull when the end-point should be read externally instead, eg for profil (dev,integration,prod)
 * <p>Expected format for a single Interface config is of the following :
 * <p>- Any property not specified as mandatory is optional.
 * <p>- The same logic as the annotation config applies here, config fallbacks from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link MethodParamConfig}).
 * <code><pre>
 * package my.rest.interface;
 * class Interface {
 *     String get();
 *     String get(String s);
 * }
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * 
 * &lt;crest-config&gt;
    &lt;service class="my.rest.interface.Interface" encoding="utf-8"&gt;
        &lt;end-point&gt;http://localhost:8080&lt;/end-point&gt; &lt;!--Mandatory--&gt; 
        &lt;path&gt;/my-path/hello&lt;/path&gt;
        &lt;global-interceptor&gt;my.rest.interface.MyRequestInterceptor1&lt;/global-interceptor&gt;
        &lt;methods&gt;
            &lt;default socket-timeout="1" connection-timeout="2" method="DELETE"&gt;
                &lt;request-interceptor&gt;my.rest.interface.MyRequestInterceptor1&lt;/request-interceptor&gt;
                &lt;response-handler&gt;my.rest.interface.MyResponseHandler1&lt;/response-handler&gt;
                &lt;error-handler&gt;my.rest.interface.MyErrorHandler1&lt;/error-handler&gt;
                &lt;retry-handler&gt;my.rest.interface.MyRetryHandler1&lt;/retry-handler&gt;
                &lt;params&gt;
                    &lt;serializer&gt;my.rest.interface.MySerializer1&lt;/serializer&gt;
                    &lt;injector&gt;my.rest.interface.MyRequestParameterInjector1&lt;/injector&gt;
                    &lt;form name="form-param"&gt;form-value&lt;/form&gt;
                    &lt;form name="form-param1"&gt;form-value1&lt;/form&gt;
                    &lt;form name="form-param2"&gt;form-value2&lt;/form&gt;
                    &lt;header name="header-param"&gt;header-value&lt;/header&gt;
                    &lt;header name="header-param1"&gt;header-value1&lt;/header&gt;
                    &lt;header name="header-param2"&gt;header-value2&lt;/header&gt;
                    &lt;query name="query-param"&gt;query-value&lt;/query&gt;
                    &lt;query name="query-param1"&gt;query-value1&lt;/query&gt;
                    &lt;query name="query-param2"&gt;query-value2&lt;/query&gt;
                    &lt;path name="path-param"&gt;path-value&lt;/path&gt;
                    &lt;path name="path-param1"&gt;path-value1&lt;/path&gt;
                    &lt;path name="path-param2"&gt;path-value2&lt;/path&gt;
                &lt;/params&gt;
            &lt;/default&gt;
            &lt;method match="m1\(\)" socket-timeout="3" connection-timeout="4" method="PUT"&gt;
                &lt;path&gt;/m1&lt;/path&gt;
                &lt;request-interceptor&gt;my.rest.interface.MyRequestInterceptor3&lt;/request-interceptor&gt;
                &lt;response-handler&gt;my.rest.interface.MyResponseHandler1&lt;/response-handler&gt;
                &lt;error-handler&gt;my.rest.interface.MyErrorHandler2&lt;/error-handler&gt;
                &lt;retry-handler&gt;my.rest.interface.MyRetryHandler2&lt;/retry-handler&gt;
                &lt;params&gt;
                    &lt;serializer&gt;my.rest.interface.MySerializer3&lt;/serializer&gt;
                    &lt;injector&gt;my.rest.interface.MyRequestParameterInjector2&lt;/injector&gt;
                    &lt;form name="form-param"&gt;over-value1&lt;/form&gt;
                    &lt;form name="form-param3"&gt;new-value&lt;/form&gt;
                &lt;/params&gt;
            &lt;/method&gt;
            &lt;method match="m1\(java\.lang\.String\)" socket-timeout="5" connection-timeout="6" method="POST"&gt;
                &lt;path&gt;/m1&lt;/path&gt;
                &lt;request-interceptor&gt;my.rest.interface.MyRequestInterceptor2&lt;/request-interceptor&gt;
                &lt;response-handler&gt;my.rest.interface.MyResponseHandler2&lt;/response-handler&gt;
                &lt;params&gt;
                    &lt;serializer&gt;my.rest.interface.MySerializer2&lt;/serializer&gt;
                    &lt;injector&gt;my.rest.interface.MyRequestParameterInjector2&lt;/injector&gt;
                    &lt;path name="form-param"&gt;over-value1&lt;/path&gt;
                    &lt;header index="0" name="a" default="deff"&gt;   &lt;!--mandatory--&gt;
                        &lt;serializer&gt;my.rest.interface.MySerializer3&lt;/serializer&gt;
                        &lt;injector&gt;my.rest.interface.MyRequestParameterInjector3&lt;/injector&gt;
                    &lt;/header&gt;
                &lt;/params&gt;
            &lt;/method&gt;
        &lt;/methods&gt;
    &lt;/service&gt;
 * 	&lt;service class="my.rest.interface.Interface2" encoding="utf-8"&gt;
 * 	(...)
 * 	&lt;/service&gt;
 * &lt;/crest-config&gt;
 * </pre></code>
 * <p>Can contain as much interface config as needed.
 * <p>A shortcut to configure the server for all interfaces is :
 * <code><pre>
 * &lt;crest-config end-point="hello"&gt;
 * </pre></code>
 * <p>The interface specific end-point if specified override the global one.
 *
 * @see org.codegist.crest.config.InterfaceConfig
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class XmlDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    private final Document config;
    private final boolean buildTemplates;
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    public XmlDrivenInterfaceConfigFactory(Document config, boolean buildTemplates) {
        this.config = config;
        this.buildTemplates = buildTemplates;
    }

    public XmlDrivenInterfaceConfigFactory(Document config) {
        this(config, false);
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {

        try {
            String globalEndpoint = getString(config, "/crest-config/@end-point");

            String endPoint = Strings.defaultIfBlank(getString(config, "/crest-config/service[@class=\"%s\"]/end-point", interfaze.getName()), globalEndpoint);
            if (isBlank(endPoint)) throw new IllegalArgumentException("end-point not found!");

            Node interfaceConfig = getNode(config, "/crest-config/service[@class=\"%s\"]", interfaze.getName());
            ConfigBuilders.InterfaceConfigBuilder icb = new ConfigBuilders.InterfaceConfigBuilder(interfaze, context.getProperties()).setIgnoreNullOrEmptyValues(true)
                    .setEndPoint(endPoint)
                    .setPath(getString(interfaceConfig, "path"))
                    .setGlobalInterceptor(getString(interfaceConfig, "global-interceptor"))
                    .setEncoding(getString(interfaceConfig, "@encoding"))

                    .setMethodsConnectionTimeout(getString(interfaceConfig, "methods/default/@connection-timeout"))
                    .setMethodsSocketTimeout(getString(interfaceConfig, "methods/default/@socket-timeout"))
                    .setMethodsHttpMethod(getString(interfaceConfig, "methods/default/@method"))
                    .setMethodsResponseHandler(getString(interfaceConfig, "methods/default/response-handler"))
                    .setMethodsErrorHandler(getString(interfaceConfig, "methods/default/error-handler"))
                    .setMethodsRetryHandler(getString(interfaceConfig, "methods/default/retry-handler"))
                    .setMethodsRequestInterceptor(getString(interfaceConfig, "methods/default/request-interceptor"))

                    .setParamsSerializer(getString(interfaceConfig, "methods/default/params/serializer"))
                    .setParamsInjector(getString(interfaceConfig, "methods/default/params/injector"));

            NodeList extraParams = getNodes(interfaceConfig, "methods/default/params/*[(name() = 'form' or name() = 'path' or name() = 'query' or name() = 'header') and not(@index)]");
            if (extraParams != null) {
                for (int i = 0; i < extraParams.getLength(); i++) {
                    Node extraParam = extraParams.item(i);
                    String name = getString(extraParam, "@name");
                    icb.addMethodsExtraParam(name, extraParam.getTextContent(), extraParam.getNodeName());
                }
            }

            NodeList methodNodes = getNodes(interfaceConfig, "methods/method");

            Map<String, Node> patterns = new HashMap<String, Node>();
            if (methodNodes != null) {
                for (int i = 0; i < methodNodes.getLength(); i++) {
                    patterns.put(getString(methodNodes.item(i), "@match"), methodNodes.item(i));
                }
            }

            for (Method method : interfaze.getDeclaredMethods()) {

                ConfigBuilders.MethodConfigBuilder mcb = icb.startMethodConfig(method).setIgnoreNullOrEmptyValues(true);
                Node methodNode = null;
                for (Map.Entry<String, Node> entry : patterns.entrySet()) {
                    String pattern = entry.getKey();
                    Method[] methods = Methods.getDeclaredMethodsThatMatches(interfaze, pattern, true);
                    if (Arrays.asList(methods).contains(method)) {
                        methodNode = entry.getValue();

                        NodeList methExtraParams = getNodes(methodNode, "params/*[(name() = 'form' or name() = 'path' or name() = 'query' or name() = 'header') and not(@index)]");
                        for (int i = 0; i < methExtraParams.getLength(); i++) {
                            Node methExtraParam = methExtraParams.item(i);
                            String name = getString(methExtraParam, "@name");
                            mcb.addExtraParam(name, methExtraParam.getTextContent(), methExtraParam.getNodeName());
                        }

                        mcb.setPath(getString(methodNode, "path"))
                                .setHttpMethod(getString(methodNode, "@method"))
                                .setSocketTimeout(getString(methodNode, "@socket-timeout"))
                                .setConnectionTimeout(getString(methodNode, "@connection-timeout"))
                                .setRequestInterceptor(getString(methodNode, "request-interceptor"))
                                .setResponseHandler(getString(methodNode, "response-handler"))
                                .setErrorHandler(getString(methodNode, "error-handler"))
                                .setRetryHandler(getString(methodNode, "retry-handler"))
                                .setParamsSerializer(getString(methodNode, "params/serializer"))
                                .setParamsInjector(getString(methodNode, "params/injector"));
                        break;
                    }
                }
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    ConfigBuilders.MethodParamConfigBuilder pcb = mcb.startParamConfig(i).setIgnoreNullOrEmptyValues(true);
                    // Injects user type annotated config.
                    Configs.injectAnnotatedConfig(pcb, method.getParameterTypes()[i]);
                    Node paramNode = getNode(methodNode, "params/*[(name() = 'form' or name() = 'path' or name() = 'query' or name() = 'header') and @index='%d']", i);
                    pcb.setName(getString(paramNode, "@name"))
                            .setDefaultValue(getString(paramNode, "@default"))
                            .setDestination(paramNode.getNodeName())
                            .setInjector(getString(paramNode, "injector"))
                            .setSerializer(getString(paramNode, "serializer"))
                            .endParamConfig();
                }
                mcb.endMethodConfig();
            }

            return icb.build(buildTemplates, true);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigFactoryException(e);
        }
    }


    private synchronized String getString(Node node, String xpathFormat, Object... args) throws XPathExpressionException {
        return node != null ? (String) (XPATH.evaluate(String.format(xpathFormat, args), node, XPathConstants.STRING)) : "";
    }

    private synchronized Node getNode(Node node, String xpathFormat, Object... args) throws XPathExpressionException {
        return node != null ? (Node) (XPATH.evaluate(String.format(xpathFormat, args), node, XPathConstants.NODE)) : null;
    }

    private synchronized NodeList getNodes(Node node, String xpathFormat, Object... args) throws XPathExpressionException {
        return node != null ? (NodeList) (XPATH.evaluate(String.format(xpathFormat, args), node, XPathConstants.NODESET)) : null;
    }
}
