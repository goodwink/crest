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
 * <p>- The same logic as the annotation config applies here, config fallbacks from param to method to interface until one config is found, otherwise defaults to any respective default value ({@link org.codegist.crest.config.InterfaceConfig}, {@link MethodConfig}, {@link ParamConfig}).
 * <code><pre>
 * package my.rest.interface;
 * class Interface {
 *     String get();
 *     String get(String s);
 *     void push(String s);
 * }
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * 
 * &lt;crest-config&gt;
 *     &lt;service class="my.rest.interface.Interface" encoding="utf-8"&gt;
 *         &lt;end-point&gt;http://localhost:8080&lt;/end-point&gt;
 *         &lt;context-path&gt;/my-path&lt;/context-path&gt;
 *         &lt;global-interceptor&gt;my.rest.interceptor.MyRequestInterceptor&lt;/global-interceptor&gt;
 *         &lt;methods&gt;
 *             &lt;default socket-timeout="1" connection-timeout="2" method="DELETE"&gt;
 *                 &lt;path&gt;/hello&lt;/path&gt;
 *                 &lt;request-interceptor&gt;my.rest.MyRequestHandler2&lt;/request-interceptor&gt;
 *                 &lt;response-handler&gt;my.rest.MyResponseHandler&lt;/response-handler&gt;
 *                 &lt;error-handler&gt;my.rest.MyErrorHandler&lt;/error-handler&gt;
 *                 &lt;retry-handler&gt;my.rest.MyRetryHandler&lt;/retry-handler&gt;
 *                 &lt;params&gt;
 *                     &lt;static-param destination="BODY" name="my-param-name"&gt;value1&lt;/static-param&gt;
 *                     &lt;default destination="BODY" name="name"&gt;
 *                         &lt;serializer&gt;my.rest.serializer.MyParamSerializer&lt;/serializer&gt;
 *                         &lt;injector&gt;my.rest.injector.MyRequestInjector&lt;/injector&gt;
 *                     &lt;/default&gt;
 *                 &lt;/params&gt;
 *             &lt;/default&gt;
 *             &lt;method match="get\(.*\)" socket-timeout="3" connection-timeout="4" method="PUT"&gt;
 *                 &lt;path&gt;/get&lt;/path&gt;
 *                 &lt;request-interceptor&gt;my.rest.interceptor.MyRequestInterceptor2&lt;/request-interceptor&gt;
 *                 &lt;response-handler&gt;my.rest.MyResponseHandler2&lt;/response-handler&gt;
 *                 &lt;error-handler&gt;my.rest.MyErrorHandler2&lt;/error-handler&gt;
 *                 &lt;params&gt;
 *                     &lt;static-param destination="HEADER" name="my-param-name-2"&gt;value&lt;/static-param&gt;
 *                     &lt;static-param destination="BODY" name="body-param-2"&gt;new-value&lt;/static-param&gt;
 *                     &lt;default destination="URL" name="name1"&gt;
 *                         &lt;serializer&gt;my.rest.serializer.MyParamSerializer2&lt;/serializer&gt;
 *                         &lt;injector&gt;my.rest.injector.MyRequestInjector2&lt;/injector&gt;
 *                     &lt;/default&gt;
 *                     &lt;param index="0" destination="URL" name="a"&gt;
 *                         &lt;serializer&gt;my.rest.serializer.MyParamSerializer3&lt;/serializer&gt;
 *                         &lt;injector&gt;my.rest.interceptor.MyRequestInterceptor3&lt;/injector&gt;
 *                     &lt;/param&gt;
 *                 &lt;/params&gt;
 *             &lt;/method&gt;
 *             &lt;method match="push\(\)" &gt;
 *                 &lt;path&gt;/push&lt;/path&gt;
 *             &lt;/method&gt;
 *         &lt;/methods&gt;
 *     &lt;/service&gt;
 * 	&lt;service class="my.rest.interface.Interface2" encoding="utf-8"&gt;
 * 	(...)
 * 	&lt;/service&gt;
 * &lt;/crest-config&gt;
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
public class XmlDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    private final Document config;
    private final boolean useDefaults;
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    public XmlDrivenInterfaceConfigFactory(Document config, boolean useDefaults) {
        this.config = config;
        this.useDefaults = useDefaults;
    }

    public XmlDrivenInterfaceConfigFactory(Document config) {
        this(config, true);
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {

        try {
            String globalEndpoint = getString(config, "/crest-config/@end-point");

            String endPoint = Strings.defaultIfBlank(getString(config, "/crest-config/service[@class=\"%s\"]/end-point", interfaze.getName()), globalEndpoint);
            if (isBlank(endPoint)) throw new IllegalArgumentException("end-point not found!");

            Node interfaceConfig = getNode(config, "/crest-config/service[@class=\"%s\"]", interfaze.getName());
            ConfigBuilders.InterfaceConfigBuilder icb = new ConfigBuilders.InterfaceConfigBuilder(interfaze, context.getProperties()).setIgnoreNullOrEmptyValues(true)
                    .setEndPoint(endPoint)
                    .setContextPath(getString(interfaceConfig, "context-path"))
                    .setGlobalInterceptor(getString(interfaceConfig, "global-interceptor"))
                    .setEncoding(getString(interfaceConfig, "@encoding"))

                    .setMethodsConnectionTimeout(getString(interfaceConfig, "methods/default/@connection-timeout"))
                    .setMethodsSocketTimeout(getString(interfaceConfig, "methods/default/@socket-timeout"))
                    .setMethodsHttpMethod(getString(interfaceConfig, "methods/default/@method"))
                    .setMethodsResponseHandler(getString(interfaceConfig, "methods/default/response-handler"))
                    .setMethodsErrorHandler(getString(interfaceConfig, "methods/default/error-handler"))
                    .setMethodsRetryHandler(getString(interfaceConfig, "methods/default/retry-handler"))
                    .setMethodsRequestInterceptor(getString(interfaceConfig, "methods/default/request-interceptor"))
                    .setMethodsPath(getString(interfaceConfig, "methods/default/path"))

                    .setParamsSerializer(getString(interfaceConfig, "methods/default/params/default/serializer"))
                    .setParamsInjector(getString(interfaceConfig, "methods/default/params/default/injector"));

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
                            icb.addMethodsExtraParam(name, methExtraParam.getTextContent(), methExtraParam.getNodeName());
                        }

                        mcb.setPath(getString(methodNode, "path"))
                                .setHttpMethod(getString(methodNode, "@method"))
                                .setSocketTimeout(getString(methodNode, "@socket-timeout"))
                                .setConnectionTimeout(getString(methodNode, "@connection-timeout"))
                                .setRequestInterceptor(getString(methodNode, "request-interceptor"))
                                .setResponseHandler(getString(methodNode, "response-handler"))
                                .setErrorHandler(getString(methodNode, "error-handler"))
                                .setRetryHandler(getString(methodNode, "retry-handler"))
                                .setParamsSerializer(getString(methodNode, "params/default/serializer"))
                                .setParamsInjector(getString(methodNode, "params/default/injector"));
                        break;
                    }
                }
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    ConfigBuilders.ParamConfigBuilder pcb = mcb.startParamConfig(i).setIgnoreNullOrEmptyValues(true);
                    // Injects user type annotated config.
                    Configs.injectAnnotatedConfig(pcb, method.getParameterTypes()[i]);
                    Node paramNode = getNode(methodNode, "params/*[(name() = 'form' or name() = 'path' or name() = 'query' or name() = 'header') and @index='%d']", i);
                    pcb.setName(getString(paramNode, "@name"))
                            .setDestination(paramNode.getNodeName())
                            .setInjector(getString(paramNode, "injector"))
                            .setSerializer(getString(paramNode, "serializer"))
                            .endParamConfig();
                }
                mcb.endMethodConfig();
            }

            return icb.build(useDefaults);
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
