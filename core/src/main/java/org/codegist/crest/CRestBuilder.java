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
import org.codegist.common.marshal.JacksonMarshaller;
import org.codegist.common.marshal.JaxbMarshaller;
import org.codegist.common.marshal.Marshaller;
import org.codegist.common.marshal.Unmarshaller;
import org.codegist.common.reflect.CglibProxyFactory;
import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.*;
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.OAuthenticatorV10;
import org.codegist.crest.oauth.Token;
import org.codegist.crest.security.AuthentificationManager;
import org.codegist.crest.security.OAuthentificationManager;
import org.codegist.crest.security.interceptor.AuthentificationInterceptor;
import org.codegist.crest.serializer.Serializer;
import org.w3c.dom.Document;

import java.lang.reflect.Type;
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
 * <p>- CRest annotation have priority over Jax-RS equivalent annotations
 * <p/>
 * <p>This default configuration has the benefit to not require any third party dependencies, but is not the recommanded one.
 * <p>For best performances, it is recommended to use the CGLib proxy factory, {@link org.codegist.common.reflect.CglibProxyFactory} (requires cglib available in the classpath) and the apache http client backed rest service {@link org.codegist.crest.HttpClientRestService}, see {@link CRestBuilder#useHttpClientRestService()}.
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 * @see org.codegist.crest.config.AnnotationDrivenInterfaceConfigFactory
 * @see org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactory
 * @see org.codegist.crest.DefaultRestService
 * @see org.codegist.crest.HttpClientRestService
 * @see org.codegist.common.reflect.CglibProxyFactory
 * @see org.codegist.common.reflect.JdkProxyFactory
 * @see DefaultCRest
 */
public class CRestBuilder {

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
    private Map<String,String> properties = null;
    private Document document = null;
    private InterfaceConfigFactory overridesFactory = null;
    private boolean dynamicOverride= true;
    private boolean crestPriority= true;
    private String modelPackageName = null;
    private Class<?> modelPackageFactory = null;

    private Map<String, Object> customProperties = new HashMap<String, Object>();
    private Map<Type, Serializer> serializersMap = new HashMap<Type, Serializer>();

    private RestService restService;

    private boolean globalAuthentification = false;
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

        Marshaller marshaller = buildMarshaller();
        Maps.putIfNotPresent(customProperties, Marshaller.class.getName(), marshaller);

        Unmarshaller unmarshaller = buildUnmarshaller();
        Maps.putIfNotPresent(customProperties, Unmarshaller.class.getName(), unmarshaller);

        AuthentificationManager authentificationManager = buildAuthentificationManager(restService);
        Maps.putIfNotPresent(customProperties, AuthentificationManager.class.getName(), authentificationManager);

        RequestInterceptor globalInterceptor = buildRequestInterceptor();

        InterfaceConfigFactory configFactory = buildInterfaceConfigFactory();
        if (globalInterceptor != null) {
            try {
                configFactory = new OverridingInterfaceConfigFactory(configFactory, new ConfigBuilders.InterfaceConfigBuilder()
                        .setGlobalInterceptor(globalInterceptor)
                        .buildOverrideTemplate());
            } catch (Exception e) {
                throw new CRestException(e);
            }
        }
        Maps.putIfNotPresent(customProperties, InterfaceConfigFactory.class.getName(), configFactory);


        /* Put then in the properties. These are not part of the API */
        Maps.putIfNotPresent(customProperties, CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP, serializersMap);
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

    private Unmarshaller buildUnmarshaller() {
        return (Unmarshaller) buildMarshaller();
    }

    private Marshaller buildMarshaller() {
        switch (retType) {
            case RET_TYPE_JSON:
                return new JacksonMarshaller();
            default:
            case RET_TYPE_RAW:
                return null;
            case RET_TYPE_XML:
                try {
                    if (modelPackageFactory != null) {
                        return new JaxbMarshaller(modelPackageFactory);
                    } else if (Strings.isNotBlank(modelPackageName)) {
                        return new JaxbMarshaller(modelPackageName);
                    } else {
                        throw new IllegalArgumentException("You must specify the package name or factory class of the target object model when using xml responses.");
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
    }

    private RequestInterceptor buildRequestInterceptor() {
        RequestInterceptor paramInterceptor = null;
        RequestInterceptor authentificationInterceptor = null;
        AuthentificationManager manager = (AuthentificationManager) customProperties.get(AuthentificationManager.class.getName());
        if (globalAuthentification) {
            // Global authentification
            authentificationInterceptor = new AuthentificationInterceptor(manager);
        }

        RequestInterceptor globalInterceptor = null;

        if (authentificationInterceptor != null) {
            globalInterceptor = new CompositeRequestInterceptor(paramInterceptor, authentificationInterceptor);
        }
        return globalInterceptor;
    }

    private InterfaceConfigFactory buildInterfaceConfigFactory() {
        InterfaceConfigFactory configFactory;
        switch (configType) {
            default:
            case CFG_TYPE_ANNO:
                if (properties != null) {
                    configFactory = new OverridingInterfaceConfigFactory(
                            new AnnotationDrivenInterfaceConfigFactory(crestPriority),
                            new PropertiesDrivenInterfaceConfigFactory(properties, false),
                            false
                    );
                } else if (document != null) {
                    configFactory = new OverridingInterfaceConfigFactory(
                            new AnnotationDrivenInterfaceConfigFactory(crestPriority),
                            new XmlDrivenInterfaceConfigFactory(document, false),
                            false
                    );
                }else {
                    configFactory = new AnnotationDrivenInterfaceConfigFactory(crestPriority);
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
            configFactory = new OverridingInterfaceConfigFactory(configFactory, overridesFactory, dynamicOverride);
        }
        return configFactory;
    }

    private AuthentificationManager buildAuthentificationManager(RestService restService) {
        String consumerKey = (String) customProperties.get(OAUTH_CONSUMER_KEY);
        String consumerSecret = (String) customProperties.get(OAUTH_CONSUMER_SECRET);
        String accessTok = (String) customProperties.get(OAUTH_ACCESS_TOKEN);
        String accessTokenSecret = (String) customProperties.get(OAUTH_ACCESS_TOKEN_SECRET);
        String paramDest = (String) customProperties.get(OAUTH_PARAM_DEST);
        Map<String, String> accessTokenExtras = (Map<String, String>) customProperties.get(OAUTH_ACCESS_TOKEN_EXTRAS);

        if (Strings.isBlank(consumerKey)
                || Strings.isBlank(consumerSecret)
                || Strings.isBlank(accessTok)
                || Strings.isBlank(accessTokenSecret)) return null;

        customProperties.put(OAUTH_CONSUMER_KEY, consumerKey);
        customProperties.put(OAUTH_CONSUMER_SECRET, consumerSecret);
        customProperties.put(OAUTH_ACCESS_TOKEN, accessTok);
        customProperties.put(OAUTH_ACCESS_TOKEN_SECRET, accessTokenSecret);
        customProperties.put(OAUTH_PARAM_DEST, paramDest);

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
    public CRestBuilder withPropertiesConfig(Map<String,String> props) {
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
    public CRestBuilder overrideDefaultConfigWith(Map<String,String> props) {
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
     * Use dynamic overrides
     *
     * @see org.codegist.crest.CRestBuilder#overrideDefaultConfigWith(org.codegist.crest.config.InterfaceConfigFactory, boolean)
     * @param overridesFactory config overrider factory
     * @return current builder
     */
    public CRestBuilder overrideDefaultConfigWith(InterfaceConfigFactory overridesFactory) {
        return overrideDefaultConfigWith(overridesFactory, true);
    }

    /**
     * Resulting CRest instance will overrides any configuration resulting from its internal {@link org.codegist.crest.config.InterfaceConfigFactory} with the configuration issued by the given overridesFactory.
     * <p>This factory is meant to returns template configs, thus can return configuration with null values that will be interpreted as fallbacking to the current  {@link org.codegist.crest.config.InterfaceConfigFactory}.
     *
     * @param overridesFactory config overrider factory
     * @param dynamicOverride If InterfaceConfig instances build by overridesFactory can change their values over time, set it to true, otherwise to false
     * @return current builder
     */
    public CRestBuilder overrideDefaultConfigWith(InterfaceConfigFactory overridesFactory, boolean dynamicOverride) {
        this.overridesFactory = overridesFactory;
        this.dynamicOverride = dynamicOverride;
        return this;
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from JSON to user object model.
     * <p>Interfaces given to the CRest instance can return any object type as long as the marshaller can unmarshall them. (requires jackson available in the classpath)
     *
     * @return current builder
     * @see org.codegist.common.marshal.JacksonMarshaller
     */
    public CRestBuilder expectsJson() {
        this.retType = RET_TYPE_JSON;
        return this;
    }

    /**
     * Resulting CRest instance will create interface instances that will return raw response.
     * <p>Given interface methods return types must be either java.lang.String, java.io.Reader or java.io.InputStream
     *
     * @return current builder
     */
    public CRestBuilder returnRawResults() {
        this.retType = RET_TYPE_RAW;
        return this;
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     *
     * @param factory The JAXb user object model factory class
     * @return current builder
     * @see org.codegist.common.marshal.JaxbMarshaller
     */
    public CRestBuilder expectsXml(Class<?> factory) {
        retType = RET_TYPE_XML;
        this.modelPackageFactory = factory;
        return this;
    }

    /**
     * Resulting CRest instance will create interface instances that will auto marshall the response from XML to user object model.
     * <p>Interface given to the CRest instance can return any object type as long as the marshaller can unmarshall them.
     *
     * @param modelPackageName The package name where the user object model is located
     * @return current builder
     * @see org.codegist.common.marshal.JaxbMarshaller
     */
    public CRestBuilder expectsXml(String modelPackageName) {
        retType = RET_TYPE_XML;
        this.modelPackageName = modelPackageName;
        return this;
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
        this.globalAuthentification = true;
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
     * @param format Date format to use
     * @see CRestProperty#SERIALIZER_DATE_FORMAT
     * @return current builder
     */
    public CRestBuilder setDateSerializerFormat(String format){
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(SERIALIZER_DATE_FORMAT, format);
        return this;
    }

    /**
     * Sets how boolean should be serialized.
     * <p>Shortcut to:
     * <p>builder.setProperty(CRestProperty.SERIALIZER_BOOLEAN_TRUE, trueSerialized)
     * <p>builder.setProperty(CRestProperty.SERIALIZER_BOOLEAN_FALSE, falseSerialized)
     * @param trueSerialized String representing serialized form of TRUE
     * @param falseSerialized String representing serialized form of FALSE
     * @see CRestProperty#SERIALIZER_BOOLEAN_TRUE
     * @see CRestProperty#SERIALIZER_BOOLEAN_FALSE
     * @return current builder
     */
    public CRestBuilder setBooleanSerializer(String trueSerialized, String falseSerialized){
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(SERIALIZER_BOOLEAN_TRUE, trueSerialized);
        customProperties.put(SERIALIZER_BOOLEAN_FALSE, falseSerialized);
        return this;
    }

    /**
     * Sets the list separator for the list serializer
     * <p>Shortcut to builder.setProperty(CRestProperty.SERIALIZER_LIST_SEPARATOR, sep)
     * @param sep Separator string
     * @see CRestProperty#SERIALIZER_LIST_SEPARATOR
     * @return current builder
     */
    public CRestBuilder setListSerializerSeparator(String sep){
        this.customProperties = Maps.defaultsIfNull(customProperties);
        customProperties.put(SERIALIZER_LIST_SEPARATOR, sep);
        return this;
    }


    /**
     * JaxRS annotation will take priority over CRest's equivalent annotations
     * @return current builder
     */
    public CRestBuilder prioritiseJaxRSAnnotations(){
        this.crestPriority = false;
        return this;
    }
}