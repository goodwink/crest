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
import java.util.*;

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
    private final static int RET_TYPE_CUSTOM = 2;
    private final static int RET_TYPE_RAW = 3;

    private final static int CFG_TYPE_ANNO = 0;
    private final static int CFG_TYPE_PROP = 1;
    private final static int CFG_TYPE_XML = 2;

    private final static int PROXY_TYPE_JDK = 0;
    private final static int PROXY_TYPE_CGLIB = 1;
    
    private final static int DESERIALIZER_XML_JAXB = 1;
    private final static int DESERIALIZER_XML_SIMPLEXML = 2;
    private final static int DESERIALIZER_XML_CUSTOM = 3;
    
    private final static int DESERIALIZER_JSON_JACKSON = 1;
    private final static int DESERIALIZER_JSON_CUSTOM = 2;

    private int retType = RET_TYPE_RAW;
    private int configType = CFG_TYPE_ANNO;
    private int proxyType = PROXY_TYPE_JDK;
    private int xmlDeserializer = DESERIALIZER_XML_JAXB;
    private int jsonDeserializer = DESERIALIZER_JSON_JACKSON;

    private Deserializer customXmlDeserializer;
    private Deserializer customJsonDeserializer;
    private final DeserializerFactory.Builder deserializerBuilder = new DeserializerFactory.Builder();
    private final Map<String,Object> xmlDeserializerConfig = new HashMap<String, Object>();
    private final Map<String,Object> jsonDeserializerConfig = new HashMap<String, Object>();
    private final Set<String> xmlMimes = new HashSet<String>(Arrays.asList(DEFAULT_XML_ACCEPT_HEADER));
    private final Set<String> jsonMimes = new HashSet<String>(Arrays.asList(DEFAULT_JSON_ACCEPT_HEADER));
    private String customMime;

    private Map<String, String> properties = null;
    private Document document = null;
    private InterfaceConfigFactory overridesFactory = null;

    private Map<String, Object> customProperties = new HashMap<String, Object>();
    private Map<String, String> placeholders = new HashMap<String, String>();
    private Map<Type, Serializer> serializersMap = new HashMap<Type, Serializer>();
    private final Map<String, ParamConfig> extraParams = new HashMap<String, ParamConfig>();

    private RestService restService;

    private boolean useHttpClient = false;

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

        DeserializerFactory deserializerFactory = buildDeserializerFactory();
        Maps.putIfNotPresent(customProperties, DeserializerFactory.class.getName(), deserializerFactory);

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

        /* Defaults the deserializer for all methods */
        switch(retType){
            case RET_TYPE_JSON:
                Maps.putIfNotPresent(customProperties, CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, deserializerFactory.buildForMimeType(DEFAULT_JSON_ACCEPT_HEADER));
                break;
            case RET_TYPE_XML:
                Maps.putIfNotPresent(customProperties, CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, deserializerFactory.buildForMimeType(DEFAULT_XML_ACCEPT_HEADER));
                break;
            case RET_TYPE_CUSTOM:
                Maps.putIfNotPresent(customProperties, CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, deserializerFactory.buildForMimeType(customMime));
                break;
            case RET_TYPE_RAW:
                break;
        }

        return new DefaultCRestContext(restService, proxyFactory, configFactory, customProperties);
    }

    private RestService buildRestService() {
        if (restService == null) {
            if (useHttpClient) {
                return HttpClientRestService.newRestService(customProperties);
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

    private DeserializerFactory buildDeserializerFactory() {
        Class<? extends Deserializer> jsonDeserializer = getJsonDeserializerClass();
        Class<? extends Deserializer> xmlDeserializer = getXmlDeserializerClass();
        if(jsonDeserializer != null) {
            deserializerBuilder.register(jsonDeserializer, jsonDeserializerConfig, jsonMimes.toArray(new String[jsonMimes.size()]));
        }else{
            deserializerBuilder.register(customJsonDeserializer, jsonMimes.toArray(new String[jsonMimes.size()]));
        }
        if(xmlDeserializer != null) {
            deserializerBuilder.register(xmlDeserializer, xmlDeserializerConfig, xmlMimes.toArray(new String[xmlMimes.size()]));
        }else{
            deserializerBuilder.register(customXmlDeserializer, xmlMimes.toArray(new String[xmlMimes.size()]));
        }
        return deserializerBuilder.build();
    }

    private Class<? extends Deserializer> getXmlDeserializerClass(){
        switch(this.xmlDeserializer){
            case DESERIALIZER_XML_JAXB:
                return JaxbDeserializer.class;
            case DESERIALIZER_XML_SIMPLEXML:
                return JaxbDeserializer.class;
            default:
                return null;
        }
    }
    private Class<? extends Deserializer> getJsonDeserializerClass(){
        switch(this.jsonDeserializer){
            case DESERIALIZER_JSON_JACKSON:
                return JacksonDeserializer.class;
            default:
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
     * Sets the concurrency level the interfaces built with the resulting CRest instance will support.
     * @param maxThread Thread count
     * @return current builder
     */
    public CRestBuilder setConcurrencyLevel(int maxThread){
        return setProperty(CREST_CONCURRENCY_LEVEL, maxThread);
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


    public CRestBuilder consumes(String mimeType, Deserializer deserializer) {
        return consumes(mimeType, deserializer, true);
    }

    public CRestBuilder consumes(String mimeType, Deserializer deserializer, boolean addAcceptHeader) {
        this.retType = RET_TYPE_CUSTOM;
        this.customMime = mimeType;
        if(addAcceptHeader) {
           addGlobalParam("Accept", mimeType, HttpRequest.DEST_HEADER, false);
        }
        return bindDeserializer(deserializer, mimeType);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     * <p>Adds a default Accept={@value CRestBuilder#DEFAULT_JSON_ACCEPT_HEADER} Header to all request
     *
     * @return current builder
     */
    public CRestBuilder consumesJson() {
        return consumesJson(true);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     * <p>If withAcceptHeader  is true, a default Accept={@value CRestBuilder#DEFAULT_JSON_ACCEPT_HEADER} Header will be added to all request
     *
     * @param withAcceptHeader indicate to wether add or not the default accept header to all requests
     * @return current builder
     */
    public CRestBuilder consumesJson(boolean withAcceptHeader) {
        return consumesJson(withAcceptHeader ? DEFAULT_JSON_ACCEPT_HEADER : null);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     * <p>The given accept header will be used for all requests.
     *
     * @param acceptHeader accept header to add to all requests
     * @return current builder
     */
    public CRestBuilder consumesJson(String acceptHeader) {
        this.retType = RET_TYPE_JSON;
        return addGlobalParam("Accept", acceptHeader, HttpRequest.DEST_HEADER, false);
    }


    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     * <p>Adds a default Accept={@value CRestBuilder#DEFAULT_XML_ACCEPT_HEADER} Header to all request
     *
     * @return current builder
     */
    public CRestBuilder consumesXml() {
        return consumesXml(true);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     * <p>If withAcceptHeader  is true, a default Accept={@value CRestBuilder#DEFAULT_XML_ACCEPT_HEADER} Header will be added to all request
     *
     * @param withAcceptHeader indicate to wether add or not the default accept header to all requests
     * @return current builder
     */
    public CRestBuilder consumesXml(boolean withAcceptHeader) {
        return consumesXml(withAcceptHeader ? DEFAULT_XML_ACCEPT_HEADER : null);
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     * <p>The given accept header will be used for all requests.
     *
     * @param acceptHeader accept header to add to all requests
     * @return current builder
     */
    public CRestBuilder consumesXml(String acceptHeader) {
        retType = RET_TYPE_XML;
        return addGlobalParam("Accept", acceptHeader, HttpRequest.DEST_HEADER, false);
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
        setProperty(OAUTH_CONSUMER_KEY, consumerKey);
        setProperty(OAUTH_CONSUMER_SECRET, consumerSecret);
        setProperty(OAUTH_ACCESS_TOKEN, accessToken);
        setProperty(OAUTH_ACCESS_TOKEN_SECRET, accessTokenSecret);
        setProperty(OAUTH_PARAM_DEST, authParamsInHeaders ? "header" : "url");
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
        return setProperty(SERIALIZER_DATE_FORMAT, format);
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
        return setProperty(SERIALIZER_BOOLEAN_TRUE, trueSerialized)
                .setProperty(SERIALIZER_BOOLEAN_FALSE, falseSerialized);
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
        return setProperty(SERIALIZER_LIST_SEPARATOR, sep);
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
        return addGlobalParam(name, value, HttpRequest.DEST_FORM);
    }

    /**
     * Adds a global header param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalHeaderParam(String name, String value) {
        return addGlobalParam(name, value, HttpRequest.DEST_HEADER);
    }

    /**
     * Adds a global query param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalQueryParam(String name, String value) {
        return addGlobalParam(name, value, HttpRequest.DEST_QUERY);
    }

    /**
     * Adds a global path param every services build with the resulting CRest instance will have.
     *
     * @param name  Param name
     * @param value Param value
     * @return current builder
     */
    public CRestBuilder addGlobalPathParam(String name, String value) {
        return addGlobalParam(name, value, HttpRequest.DEST_PATH);
    }

    private CRestBuilder addGlobalParam(String name, String value, String destination) {
        return addGlobalParam(name, value, destination, true);
    }

    private CRestBuilder addGlobalParam(String name, String value, String destination, boolean addIfEmptyValue) {
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


    public CRestBuilder bindJsonDeserializerWith(String... mimeTypes){
        this.jsonMimes.addAll(Arrays.asList(mimeTypes));
        return this;
    }
    public CRestBuilder bindXmlDeserializerWith(String... mimeTypes){
        this.xmlMimes.addAll(Arrays.asList(mimeTypes));
        return this;
    }
    public CRestBuilder bindDeserializer(Deserializer deserializer, String... mimeTypes){
        this.deserializerBuilder.register(deserializer, mimeTypes);
        return this;
    }

    public CRestBuilder deserializeXmlWith(Deserializer deserializer){
        this.xmlDeserializer = DESERIALIZER_XML_CUSTOM;
        this.customXmlDeserializer = deserializer;
        return this;
    }
    
    public CRestBuilder deserializeXmlWithJaxb(){
        this.xmlDeserializer = DESERIALIZER_XML_JAXB;
        this.xmlDeserializerConfig.clear();
        return this;
    }
    public CRestBuilder deserializeXmlWithJaxb(Class<?>... classToBeBound){
        deserializeXmlWithJaxb();
        this.xmlDeserializerConfig.put(JaxbDeserializer.MODEL_CLASSES_BOUND_PROP, classToBeBound);
        return this;
    }
    public CRestBuilder deserializeXmlWithJaxb(String contextPath){
        deserializeXmlWithJaxb();
        this.xmlDeserializerConfig.put(JaxbDeserializer.MODEL_CONTEXT_PATH_PROP, contextPath);
        return this;
    }
    public CRestBuilder deserializeXmlWithJaxb(Map<String,Object> jaxbConfig){
        deserializeXmlWithJaxb();
        this.xmlDeserializerConfig.clear();
        this.xmlDeserializerConfig.putAll(jaxbConfig);
        return this;
    }

    public CRestBuilder deserializeXmlWithSimpleXml() {
        this.xmlDeserializer = DESERIALIZER_XML_SIMPLEXML;  
        this.xmlDeserializerConfig.clear();
        return this;
    }
    public CRestBuilder deserializeXmlWithSimpleXml(String dateFormat) {
        deserializeXmlWithSimpleXml();
        this.xmlDeserializerConfig.put(SimpleXmlDeserializer.DATE_FORMAT_PROP, dateFormat);
        return this;
    }

    public CRestBuilder deserializeXmlWithSimpleXml(String trueVal, String falseVal) {
        deserializeXmlWithSimpleXml();
        this.xmlDeserializerConfig.put(SimpleXmlDeserializer.BOOLEAN_FORMAT_PROP, trueVal + ":" + falseVal);
        return this;
    }

    public CRestBuilder deserializeXmlWithSimpleXml(String dateFormat, String trueVal, String falseVal, boolean strict) {
        deserializeXmlWithSimpleXml();
        this.xmlDeserializerConfig.put(SimpleXmlDeserializer.BOOLEAN_FORMAT_PROP, trueVal + ":" + falseVal);
        this.xmlDeserializerConfig.put(SimpleXmlDeserializer.DATE_FORMAT_PROP, dateFormat);
        this.xmlDeserializerConfig.put(SimpleXmlDeserializer.STRICT_PROP, strict);
        return this;
    }

    public CRestBuilder deserializeXmlWithSimpleXml(Map<String, Object> config) {
        deserializeXmlWithSimpleXml();
        this.xmlDeserializerConfig.clear();
        this.xmlDeserializerConfig.putAll(config);
        return this;
    }

    public CRestBuilder deserializerJsonWith(Deserializer deserializer){
        this.jsonDeserializer = DESERIALIZER_JSON_CUSTOM;
        this.customJsonDeserializer = deserializer;
        return this;
    }
    public CRestBuilder deserializerJsonWithJackson(){
        this.jsonDeserializer = DESERIALIZER_JSON_JACKSON;
        this.jsonDeserializerConfig.clear();
        return this;
    }
    public CRestBuilder deserializerJsonWithJackson(Map<String,Object> config){
        deserializerJsonWithJackson();
        this.jsonDeserializerConfig.clear();
        this.jsonDeserializerConfig.putAll(config);
        return this;
    }

}