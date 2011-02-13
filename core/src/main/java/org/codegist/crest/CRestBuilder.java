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

import org.codegist.common.collect.Maps;
import org.codegist.common.lang.Strings;
import org.codegist.common.reflect.CglibProxyFactory;
import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.*;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.OAuthenticatorV10;
import org.codegist.crest.oauth.Token;
import org.codegist.crest.security.AuthentificationManager;
import org.codegist.crest.security.OAuthentificationManager;
import org.codegist.crest.security.interceptor.AuthentificationInterceptor;
import org.codegist.crest.serializer.*;
import org.w3c.dom.Document;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.codegist.crest.CRestProperty.*;

/**
 * <p>The default build :
 * <code><pre>
 * CRest crest = new CRestBuilder().build();
 * </pre></code>
 * <p>will create {@link org.codegist.crest.CRest} with the following features :
 * <p>- Annotation driven configuration handled by {@link org.codegist.crest.config.CRestAnnotationDrivenInterfaceConfigFactory}, lookup for annotation in package {@link org.codegist.crest.annotate}.
 * <p>- Raw response return, meaning the given interface method return type must be either java.io.String, java.io.InputStream or java.io.Reader.
 * <p>- HTTP calls handled by {@link org.codegist.crest.DefaultRestService}
 * <p>- Uses JDK dynamics proxies to instanciates given interfaces
 * <p/>
 * <p>This default configuration has the benefit to not require any third party dependencies, but is not the recommanded one.
 * <p>For best performances, it is recommended to use the CGLib proxy factory, {@link org.codegist.common.reflect.CglibProxyFactory} (requires cglib available in the classpath) and the apache http client backed rest service {@link org.codegist.crest.HttpClientRestService}, see {@link CRestBuilder#useHttpClientRestService()}.
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 * @see org.codegist.crest.config.CRestAnnotationDrivenInterfaceConfigFactory
 * @see org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactory
 * @see org.codegist.crest.DefaultRestService
 * @see org.codegist.crest.HttpClientRestService
 * @see org.codegist.common.reflect.CglibProxyFactory
 * @see org.codegist.common.reflect.JdkProxyFactory
 * @see DefaultCRest
 */
public class CRestBuilder {
    private final static String DEFAULT_JSON_ACCEPT_HEADER = "application/json";
    private final static String DEFAULT_XML_ACCEPT_HEADER = "application/xml";

    private final static int RET_TYPE_JSON = 0;
    private final static int RET_TYPE_XML = 1;
    private final static int RET_TYPE_RAW = 2;

    private final static int CFG_TYPE_ANNO = 0;
    private final static int CFG_TYPE_PROP = 1;
    private final static int CFG_TYPE_XML = 2;

    private final static int PROXY_TYPE_JDK = 0;
    private final static int PROXY_TYPE_CGLIB = 1;

    private int retType = RET_TYPE_RAW;
    private int configType = CFG_TYPE_ANNO;
    private int proxyType = PROXY_TYPE_JDK;


    private JsonDeserializerChooser jsonChooser = new JsonDeserializerChooser();
    private XmlDeserializerChooser xmlChooser = new XmlDeserializerChooser();

    private Map<String, String> properties = null;
    private Document document = null;
    private InterfaceConfigFactory overridesFactory = null;
    private String modelPackageName = null;
    private Class<?> modelPackageFactory = null;

    private Map<String, Object> customProperties = new HashMap<String, Object>();
    private Map<String, String> placeholders = new HashMap<String, String>();
    private Map<Type, Serializer> serializersMap = new HashMap<Type, Serializer>();
    private final Map<String, ParamConfig> extraParams = new HashMap<String, ParamConfig>();

    private RestService restService;

    private boolean useHttpClient = false;
    private int maxConnections = 1;
    private int maxConnectionsPerRoute = 1;


    public CRest build() {
        CRestContext context = buildContext();
        return new DefaultCRest(context);
    }

    CRestContext buildContext() {
        customProperties = Maps.defaultsIfNull(customProperties);

        RestService restService = buildRestService();
        Maps.putIfNotPresent(customProperties, RestService.class.getName(), restService);

        ProxyFactory proxyFactory = buildProxyFactory();
        Maps.putIfNotPresent(customProperties, ProxyFactory.class.getName(), proxyFactory);

        Deserializer deserializer = buildDeserializer();
        Maps.putIfNotPresent(customProperties, Deserializer.class.getName(), deserializer);

        AuthentificationManager authentificationManager = buildAuthentificationManager(restService);
        Maps.putIfNotPresent(customProperties, AuthentificationManager.class.getName(), authentificationManager);

        InterfaceConfigFactory configFactory = buildInterfaceConfigFactory();

        if (authentificationManager != null) {
            RequestInterceptor authentificationInterceptor = new AuthentificationInterceptor(authentificationManager);
            try {
                configFactory = new OverridingInterfaceConfigFactory(configFactory, new ConfigBuilders.InterfaceConfigBuilder()
                        .setGlobalInterceptor(authentificationInterceptor)
                        .buildTemplate());
            } catch (Exception e) {
                throw new CRestException(e);
            }
        }
        Maps.putIfNotPresent(customProperties, InterfaceConfigFactory.class.getName(), configFactory);

        /* Put then in the properties. These are not part of the API */
        Maps.putIfNotPresent(customProperties, CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP, Maps.unmodifiable(serializersMap));
        Maps.putIfNotPresent(customProperties, CRestProperty.CONFIG_PLACEHOLDERS_MAP, Maps.unmodifiable(placeholders));
        Maps.putIfNotPresent(customProperties, CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, this.extraParams.values().toArray(new ParamConfig[this.extraParams.size()]));

        return new DefaultCRestContext(restService, proxyFactory, configFactory, customProperties);
    }

    private RestService buildRestService() {
        if (restService == null) {
            if (useHttpClient) {
                return HttpClientRestService.newRestService(maxConnections, maxConnectionsPerRoute);
            } else {
                return new DefaultRestService();
            }
        } else {
            return restService;
        }
    }

    private ProxyFactory buildProxyFactory() {
        switch (proxyType) {
            default:
            case PROXY_TYPE_JDK:
                return new JdkProxyFactory();
            case PROXY_TYPE_CGLIB:
                return new CglibProxyFactory();
        }
    }

    private Deserializer buildDeserializer() {
        switch (retType) {
            case RET_TYPE_JSON:
                return jsonChooser.getDeserializer();
            case RET_TYPE_XML:
                return xmlChooser.getDeserializer();
            default:
            case RET_TYPE_RAW:
                return null;
        }
    }

    private InterfaceConfigFactory buildInterfaceConfigFactory() {
        InterfaceConfigFactory configFactory;
        switch (configType) {
            default:
            case CFG_TYPE_ANNO:
                InterfaceConfigFactory baseConfigFactory = new CRestAnnotationDrivenInterfaceConfigFactory();

                if (properties != null) {
                    configFactory = new OverridingInterfaceConfigFactory(
                            baseConfigFactory,
                            new PropertiesDrivenInterfaceConfigFactory(properties, true));
                } else if (document != null) {
                    configFactory = new OverridingInterfaceConfigFactory(
                            baseConfigFactory,
                            new XmlDrivenInterfaceConfigFactory(document, true));
                } else {
                    configFactory = baseConfigFactory;
                }
                break;
            case CFG_TYPE_PROP:
                configFactory = new PropertiesDrivenInterfaceConfigFactory(properties);
                break;
            case CFG_TYPE_XML:
                configFactory = new XmlDrivenInterfaceConfigFactory(document);
                break;
        }
        if (overridesFactory != null) {
            configFactory = new OverridingInterfaceConfigFactory(configFactory, overridesFactory);
        }
        return configFactory;
    }


    private AuthentificationManager buildAuthentificationManager(RestService restService) {
        String consumerKey = (String) customProperties.get(OAUTH_CONSUMER_KEY);
        String consumerSecret = (String) customProperties.get(OAUTH_CONSUMER_SECRET);
        String accessTok = (String) customProperties.get(OAUTH_ACCESS_TOKEN);
        String accessTokenSecret = (String) customProperties.get(OAUTH_ACCESS_TOKEN_SECRET);
        Map<String, String> accessTokenExtras = (Map<String, String>) customProperties.get(OAUTH_ACCESS_TOKEN_EXTRAS);

        if (Strings.isBlank(consumerKey)
                || Strings.isBlank(consumerSecret)
                || Strings.isBlank(accessTok)
                || Strings.isBlank(accessTokenSecret)) return null;

        Token consumerToken = new Token(consumerKey, consumerSecret);
        OAuthenticator authenticator = new OAuthenticatorV10(restService, consumerToken, customProperties);
        Token accessToken = new Token(accessTok, accessTokenSecret, accessTokenExtras);

        return new OAuthentificationManager(authenticator, accessToken);
    }

    /**
     * Resulting CRest instance's RestService will be a single threaded instance of {@link org.codegist.crest.HttpClientRestService}.
     *
     * @return current builder
     * @see org.codegist.crest.HttpClientRestService
     */
    public CRestBuilder useHttpClientRestService() {
        return useHttpClientRestService(1);
    }

    /**
     * Resulting CRest instance's RestService will be a multi-threaded instance of {@link org.codegist.crest.HttpClientRestService}.
     *
     * @param maxConnections max concurrent connections (includes max connection per route as well)
     * @return current builder
     * @see org.codegist.crest.HttpClientRestService
     */
    public CRestBuilder useHttpClientRestService(int maxConnections) {
        return useHttpClientRestService(maxConnections, maxConnections);
    }

    /**
     * Resulting CRest instance's RestService will be a multi-threaded instance of {@link org.codegist.crest.HttpClientRestService}.
     *
     * @param maxConnections         max concurrent connections
     * @param maxConnectionsPerRoute max connection per route
     * @return current builder
     * @see org.codegist.crest.HttpClientRestService
     */
    public CRestBuilder useHttpClientRestService(int maxConnections, int maxConnectionsPerRoute) {
        this.maxConnections = maxConnections;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        this.useHttpClient = true;
        return this;
    }

    /**
     * Overrides the rest service every services build with the resulting CRest instance will use.
     *
     * @param restService rest service instance
     * @return current builder
     */
    public CRestBuilder setRestService(RestService restService) {
        this.restService = restService;
        return this;
    }

    /**
     * Sets a custom property every services build with the resulting CRest instance will be passed.
     *
     * @param name  property key
     * @param value property value
     * @return current builder
     * @see InterfaceContext#getProperties()
     */
    public CRestBuilder setProperty(String name, Object value) {
        customProperties.put(name, value);
        return this;
    }

    /**
     * Sets a custom serializer for the given type the resulting CRest instance will use to serialize method arguments.
     * <p>The given type reflects the given Interface type, polymorphism is not considered.
     *
     * @param type       Type to seralize
     * @param serializer Serializer
     * @return current builder
     * @see InterfaceContext#getProperties()
     */
    public CRestBuilder setSerializer(Type type, Serializer serializer) {
        serializersMap.put(type, serializer);
        return this;
    }

    /**
     * Adds all custom properties every services build with the resulting CRest instance will be passed.
     *
     * @param customProperties properties map
     * @return current builder
     * @see InterfaceContext#getProperties()
     */
    public CRestBuilder addProperties(Map<String, Object> customProperties) {
        this.customProperties.putAll(customProperties);
        return this;
    }

    /**
     * Sets a custom properties every services build with the resulting CRest instance will be passed.
     *
     * @param customProperties properties map
     * @return current builder
     * @see InterfaceContext#getProperties()
     */
    public CRestBuilder setProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
        return this;
    }

    /**
     * Resulting CRest instance will handle annotated configurated interfaces.
     *
     * @return current builder
     * @see org.codegist.crest.config.CRestAnnotationDrivenInterfaceConfigFactory
     */
    public CRestBuilder withAnnotatedConfig() {
        this.configType = CFG_TYPE_ANNO;
        this.properties = null;
        return this;
    }

    /**
     * Resulting CRest instance will handle properties based configuration.
     * <p>Given properties must be able to configure any possible interface given to the resulting CRest instance.
     *
     * @param props configuration properties
     * @return current builder
     */
    public CRestBuilder withPropertiesConfig(Map<String, String> props) {
        this.configType = CFG_TYPE_PROP;
        this.properties = props;
        return this;
    }

    /**
     * Resulting CRest instance will handle xml based configuration.
     * <p>Given xml must be able to configure any possible interface given to the resulting CRest instance.
     *
     * @param document xml configuration document
     * @return current builder
     */
    public CRestBuilder withXmlConfig(Document document) {
        this.configType = CFG_TYPE_XML;
        this.document = document;
        return this;
    }

    /**
     * Resulting CRest instance will overrides any configuration resulting from its current {@link org.codegist.crest.config.InterfaceConfigFactory} with the given properties.
     * <p>Properties must be formatted as documentated in {@link org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactory}
     * <p>Can be used for instance to override the server end-point for differents devs environment.
     *
     * @param props properties
     * @return current builder
     * @see org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactory
     */
    public CRestBuilder overrideDefaultConfigWith(Map<String, String> props) {
        this.properties = props;
        return this;
    }

    /**
     * Resulting CRest instance will overrides any configuration resulting from its current {@link org.codegist.crest.config.InterfaceConfigFactory} with the given xml configuration.
     * <p>Document must be formatted as documentated in {@link org.codegist.crest.config.XmlDrivenInterfaceConfigFactory}
     * <p>Can be used for instance to override the server end-point for differents devs environment.
     *
     * @param document xml configuration
     * @return current builder
     * @see org.codegist.crest.config.XmlDrivenInterfaceConfigFactory
     */
    public CRestBuilder overrideDefaultConfigWith(Document document) {
        this.document = document;
        return this;
    }

    /**
     * Resulting CRest instance will overrides any configuration resulting from its internal {@link org.codegist.crest.config.InterfaceConfigFactory} with the configuration issued by the given overridesFactory.
     * <p>This factory is meant to returns template configs, thus can return configuration with null values that will be interpreted as fallbacking to the current  {@link org.codegist.crest.config.InterfaceConfigFactory}.
     *
     * @param overridesFactory config overrider factory
     * @return current builder
     */
    public CRestBuilder overrideDefaultConfigWith(InterfaceConfigFactory overridesFactory) {
        this.overridesFactory = overridesFactory;
        return this;
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     * <p>Adds a default Accept={@value CRestBuilder#DEFAULT_JSON_ACCEPT_HEADER} Header to all request
     *
     * @return current builder
     */
    public JsonDeserializerChooser expectsJson() {
        return expectsJson(true);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     * <p>If withAcceptHeader  is true, a default Accept={@value CRestBuilder#DEFAULT_JSON_ACCEPT_HEADER} Header will be added to all request
     *
     * @param withAcceptHeader indicate to wether add or not the default accept header to all requests
     * @return current builder
     */
    public JsonDeserializerChooser expectsJson(boolean withAcceptHeader) {
        if (withAcceptHeader) {
            return expectsJson(DEFAULT_JSON_ACCEPT_HEADER);
        } else {
            return expectsJson(null);
        }
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     * <p>The given accept header will be used for all requests.
     *
     * @param acceptHeader accept header to add to all requests
     * @return current builder
     */
    public JsonDeserializerChooser expectsJson(String acceptHeader) {
        this.retType = RET_TYPE_JSON;
        addGlobalParam("Accept", acceptHeader, Destination.HEADER, false);
        return jsonChooser;
    }


    /**
     * Resulting CRest instance will create interface instances that will return raw response.
     * <p>Given interface methods return types must be either java.lang.String, java.io.Reader or java.io.InputStream
     * <p>No Accept header is used
     *
     * @return current builder
     */
    public CRestBuilder returnRawResults() {
        return returnRawResults(null);
    }

    /**
     * Resulting CRest instance will create interface instances that will return raw response.
     * <p>Given interface methods return types must be either java.lang.String, java.io.Reader or java.io.InputStream
     * <p>The given accept header will be used for all requests.
     *
     * @param acceptHeader accept header to add to all requests
     * @return current builder
     */
    public CRestBuilder returnRawResults(String acceptHeader) {
        this.retType = RET_TYPE_RAW;
        return addGlobalParam("Accept", acceptHeader, Destination.HEADER, false);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     * <p>Adds a default Accept={@value CRestBuilder#DEFAULT_XML_ACCEPT_HEADER} Header to all request
     *
     * @return current builder
     */
    public XmlDeserializerChooser expectsXml() {
        return expectsXml(true);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     * <p>If withAcceptHeader  is true, a default Accept={@value CRestBuilder#DEFAULT_XML_ACCEPT_HEADER} Header will be added to all request
     *
     * @param withAcceptHeader indicate to wether add or not the default accept header to all requests
     * @return current builder
     */
    public XmlDeserializerChooser expectsXml(boolean withAcceptHeader) {
        if (withAcceptHeader) {
            return expectsXml(DEFAULT_XML_ACCEPT_HEADER);
        } else {
            return expectsXml(null);
        }
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     * <p>The given accept header will be used for all requests.
     *
     * @param acceptHeader accept header to add to all requests
     * @return current builder
     */
    public XmlDeserializerChooser expectsXml(String acceptHeader) {
        retType = RET_TYPE_XML;
        addGlobalParam("Accept", acceptHeader, Destination.HEADER, false);
        return xmlChooser;
    }


    /**
     * Resulting CRest instance will use native jdk proxies to build interface instances.
     *
     * @return current builder
     * @see org.codegist.common.reflect.JdkProxyFactory
     */
    public CRestBuilder useJdkProxies() {
        this.proxyType = PROXY_TYPE_JDK;
        return this;
    }

    /**
     * Resulting CRest instance will use cglib proxies to build interface instances. (requires cglib available in the classpath)
     *
     * @return current builder
     * @see org.codegist.common.reflect.CglibProxyFactory
     */
    public CRestBuilder useCglibProxies() {
        this.proxyType = PROXY_TYPE_CGLIB;
        return this;
    }

    /**
     * <p>Authentification parameters are added to the request headers.
     * <p>See  {@link CRestBuilder#usePreauthentifiedOAuth(String, String, String, String, boolean)}
     *
     * @param consumerKey       Consumer key
     * @param consumerSecret    Consumer secret
     * @param accessToken       Preauthentified access token
     * @param accessTokenSecret Preauthentified access token secret
     * @return current builder
     * @see CRestBuilder#usePreauthentifiedOAuth(String, String, String, String, boolean)
     */
    public CRestBuilder usePreauthentifiedOAuth(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        return usePreauthentifiedOAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret, true);
    }

    /**
     * Resulting CRest instance will authentify every requests using OAuth (http://oauth.net/) authentification mechanism, using a pre-authentified access token and consumer information.
     *
     * @param consumerKey         Consumer key
     * @param consumerSecret      Consumer secret
     * @param accessToken         Preauthentified access token
     * @param accessTokenSecret   Preauthentified access token secret
     * @param authParamsInHeaders If true, adds the authentification information into the request headers, otherwise in the query string
     * @return current builder
     */
    public CRestBuilder usePreauthentifiedOAuth(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, boolean authParamsInHeaders) {
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(OAUTH_CONSUMER_KEY, consumerKey);
        customProperties.put(OAUTH_CONSUMER_SECRET, consumerSecret);
        customProperties.put(OAUTH_ACCESS_TOKEN, accessToken);
        customProperties.put(OAUTH_ACCESS_TOKEN_SECRET, accessTokenSecret);
        customProperties.put(OAUTH_PARAM_DEST, authParamsInHeaders ? "header" : "url");
        return this;
    }

    /**
     * Sets date serializer format to the given format.
     * <p>Shortcut to builder.setProperty(CRestProperty.SERIALIZER_DATE_FORMAT, format)
     *
     * @param format Date format to use
     * @return current builder
     * @see CRestProperty#SERIALIZER_DATE_FORMAT
     */
    public CRestBuilder setDateSerializerFormat(String format) {
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(SERIALIZER_DATE_FORMAT, format);
        return this;
    }

    /**
     * Sets how boolean should be serialized.
     * <p>Shortcut to:
     * <p>builder.setProperty(CRestProperty.SERIALIZER_BOOLEAN_TRUE, trueSerialized)
     * <p>builder.setProperty(CRestProperty.SERIALIZER_BOOLEAN_FALSE, falseSerialized)
     *
     * @param trueSerialized  String representing serialized form of TRUE
     * @param falseSerialized String representing serialized form of FALSE
     * @return current builder
     * @see CRestProperty#SERIALIZER_BOOLEAN_TRUE
     * @see CRestProperty#SERIALIZER_BOOLEAN_FALSE
     */
    public CRestBuilder setBooleanSerializer(String trueSerialized, String falseSerialized) {
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(SERIALIZER_BOOLEAN_TRUE, trueSerialized);
        customProperties.put(SERIALIZER_BOOLEAN_FALSE, falseSerialized);
        return this;
    }

    /**
     * Sets the list separator for the list serializer
     * <p>Shortcut to builder.setProperty(CRestProperty.SERIALIZER_LIST_SEPARATOR, sep)
     *
     * @param sep Separator string
     * @return current builder
     * @see CRestProperty#SERIALIZER_LIST_SEPARATOR
     */
    public CRestBuilder setListSerializerSeparator(String sep) {
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(SERIALIZER_LIST_SEPARATOR, sep);
        return this;
    }

    /**
     * Sets a placeholder key/value that will be used to replace interface config eg:
     * <p>Calling
     * <pre><code>
     *      new CRestBuilder()
     *          .setConfigPlaceholder("my.server", "127.0.0.1")
     *          .setConfigPlaceholder("my.port", "8080");
     * </code></pre>
     * <p>will replace any place holder found in any interface location, eg:
     * <code>@EndPoint("http://{my.server}:{my.port}")</code>
     * <br>or
     * <p>for properties files: <code>service.test.end-point=http://{my.server}:{my.port}</code>
     *
     * @param placeholder Placeholder key
     * @param value       Placeholder value
     * @return current builder
     * @see CRestProperty#CONFIG_PLACEHOLDERS_MAP
     */
    public CRestBuilder setConfigPlaceholder(String placeholder, String value) {
        placeholders.put(placeholder, value);
        return this;
    }


    /**
     * Adds a global form param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalFormParam(String name, String value) {
        return addGlobalParam(name, value, Destination.FORM);
    }

    /**
     * Adds a global header param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalHeaderParam(String name, String value) {
        return addGlobalParam(name, value, Destination.HEADER);
    }

    /**
     * Adds a global query param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalQueryParam(String name, String value) {
        return addGlobalParam(name, value, Destination.QUERY);
    }

    /**
     * Adds a global path param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalPathParam(String name, String value) {
        return addGlobalParam(name, value, Destination.PATH);
    }

    private CRestBuilder addGlobalParam(String name, String value, Destination destination) {
        return addGlobalParam(name, value, destination, true);
    }

    private CRestBuilder addGlobalParam(String name, String value, Destination destination, boolean addIfEmptyValue) {
        if (Strings.isBlank(value) && !addIfEmptyValue) {
            extraParams.remove(name);
        } else {
            ParamConfig param = new ConfigBuilders.ParamConfigBuilder(customProperties)
                    .setName(name)
                    .setDefaultValue(value)
                    .setDestination(destination)
                    .build();
            extraParams.put(name, param);
        }

        return this;
    }

    public class XmlDeserializerChooser {

        private Deserializer deserializer;

        public CRestBuilder handledByJaxB(String packageName) {
            this.deserializer = new JaxbDeserializer(packageName);
            return CRestBuilder.this;
        }

        public CRestBuilder handledByJaxB(Class<?> factoryClass) {
            this.deserializer = new JaxbDeserializer(factoryClass);
            return CRestBuilder.this;
        }

        public CRestBuilder handledByJaxB(Map<String, Object> config) {
            this.deserializer = new JaxbDeserializer(config);
            return CRestBuilder.this;
        }

        public CRestBuilder handledBySimpleXml() {
            this.deserializer = new SimpleXmlDeserializer();
            return CRestBuilder.this;
        }

        public CRestBuilder handledBySimpleXml(String dateFormat) {
            return handledBySimpleXml(dateFormat, null, null, SimpleXmlDeserializer.DEFAULT_STRICT);
        }

        public CRestBuilder handledBySimpleXml(String trueVal, String falseVal) {
            return handledBySimpleXml(null, trueVal, falseVal, SimpleXmlDeserializer.DEFAULT_STRICT);
        }

        public CRestBuilder handledBySimpleXml(String dateFormat, String trueVal, String falseVal, boolean strict) {
            Map<String, Object> config = new HashMap<String, Object>();
            if (Strings.isNotBlank(trueVal) && Strings.isNotBlank(falseVal)) {
                config.put(SimpleXmlDeserializer.BOOLEAN_FORMAT_PROP, trueVal + ":" + falseVal);
            }
            if (Strings.isNotBlank(dateFormat)) {
                config.put(SimpleXmlDeserializer.DATE_FORMAT_PROP, dateFormat);
            }
            config.put(SimpleXmlDeserializer.STRICT_PROP, strict);
            return handledBySimpleXml(config);
        }

        public CRestBuilder handledBySimpleXml(Map<String, Object> config) {
            this.deserializer = new SimpleXmlDeserializer(config);
            return CRestBuilder.this;
        }

        public CRestBuilder handledBy(Deserializer deserializer) {
            this.deserializer = deserializer;
            return CRestBuilder.this;
        }

        Deserializer getDeserializer() {
            return deserializer;
        }

    }

    public class JsonDeserializerChooser {

        private Deserializer deserializer;

        /**
         * Json deserialization will be handled by Jackson
         *
         * @return current builder
         */
        public CRestBuilder handledByJackson() {
            return handledByJackson(Collections.<String, Object>emptyMap());
        }

        /**
         * Json deserialization will be handled by Jackson
         *
         * @return current builder
         */
        public CRestBuilder handledByJackson(Map<String, Object> config) {
            this.deserializer = new JacksonDeserializer(config);
            return CRestBuilder.this;
        }

        /**
         * Json deserialization will be handled by the given deserializer
         *
         * @param deserializer deserializer to use for json deserialization
         * @return current builder
         */
        public CRestBuilder handledBy(Deserializer deserializer) {
            this.deserializer = deserializer;
            return CRestBuilder.this;
        }

        Deserializer getDeserializer() {
            return deserializer;
        }
    }

}