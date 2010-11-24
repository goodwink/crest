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
import org.codegist.crest.interceptor.OAuthInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.interceptor.RequestParamDefaultsInterceptor;
import org.codegist.crest.serializer.Serializer;

import javax.xml.bind.JAXBException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>The default build :
 * <code><pre>
 * CRest crest = new CRestBuilder().build();
 * </pre></code>
 * <p>will create {@link org.codegist.crest.CRest} with the following features :
 * <p>- Annotation driven configuration handled by {@link org.codegist.crest.config.AnnotationDrivenInterfaceConfigFactory}, lookup for {@link org.codegist.crest.annotate.RestApi},{@link org.codegist.crest.annotate.RestMethod},{@link org.codegist.crest.annotate.RestParam} annotation on the interfaces passed to the factory.
 * <p>- Raw response return, meaning the given interface method return type must be either java.io.String, java.io.InputStream or java.io.Reader.
 * <p>- HTTP calls handled by {@link org.codegist.crest.DefaultRestService}
 * <p>- Uses JDK dynamics proxies to instanciates given interfaces
 * <p/>
 * <p>This default configuration has the benefit to not require any third party dependencies, but is not the recommanded one.
 * <p>For best performances, it is recommended to use the CGLib proxy factory, {@link org.codegist.common.reflect.CglibProxyFactory} (requires cglib available in the classpath) and the apache http client backed rest service {@link org.codegist.crest.HttpClientRestService}, see {@link CRestBuilder#useHttpClientRestService()}.
 *
 * @see org.codegist.crest.config.AnnotationDrivenInterfaceConfigFactory
 * @see org.codegist.crest.config.PropertiesDrivenInterfaceFactory
 * @see org.codegist.crest.DefaultRestService
 * @see org.codegist.crest.HttpClientRestService
 * @see org.codegist.common.reflect.CglibProxyFactory
 * @see org.codegist.common.reflect.JdkProxyFactory
 * @see DefaultCRest
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class CRestBuilder {

    private final static int RET_TYPE_JSON = 0;
    private final static int RET_TYPE_XML = 1;
    private final static int RET_TYPE_RAW = 2;

    private final static int CFG_TYPE_ANNO = 0;
    private final static int CFG_TYPE_PROP = 1;

    private final static int PROXY_TYPE_JDK = 0;
    private final static int PROXY_TYPE_CGLIB = 1;

    private int retType = RET_TYPE_RAW;
    private int configType = CFG_TYPE_ANNO;
    private int proxyType = PROXY_TYPE_JDK;
    private Properties properties = null;
    private InterfaceConfigFactory overridesFactory = null;
    private String modelPackageName = null;
    private Class<?> modelPackageFactory = null;

    private Map<String, Object> customProperties = new HashMap<String, Object>();
    private Map<Type, Serializer> serializersMap = new HashMap<Type, Serializer>();

    private Map<String, Object> body = new HashMap<String, Object>();
    private Map<String, String> queryString = new HashMap<String, String>();
    private Map<String, String> headers = new HashMap<String, String>();

    private RestService restService;

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private boolean authParamsInHeaders;
    private boolean useHttpClient = false;
    private int maxConnections = 1;
    private int maxConnectionsPerRoute = 1;

    public CRest build() {

        if (restService == null) {
            if (useHttpClient) {
                restService = HttpClientRestService.newRestService(maxConnections, maxConnectionsPerRoute);
            } else {
                restService = new DefaultRestService();
            }

        }

        ProxyFactory proxyFactory;
        switch (proxyType) {
            default:
            case PROXY_TYPE_JDK:
                proxyFactory = new JdkProxyFactory();
                break;
            case PROXY_TYPE_CGLIB:
                proxyFactory = new CglibProxyFactory();
                break;
        }

        Marshaller marshaller = null;
        Unmarshaller unmarshaller = null;
        switch (retType) {

            case RET_TYPE_JSON:
                marshaller = new JacksonMarshaller();
                unmarshaller = new JacksonMarshaller();
                break;
            default:
            case RET_TYPE_RAW:
                marshaller = null;
                unmarshaller = null;
                break;
            case RET_TYPE_XML:
                try {
                    if (modelPackageFactory != null) {
                        marshaller = new JaxbMarshaller(modelPackageFactory);
                        unmarshaller = new JaxbMarshaller(modelPackageFactory);
                    } else if (Strings.isNotBlank(modelPackageName)) {
                        marshaller = new JaxbMarshaller(modelPackageName);
                        unmarshaller = new JaxbMarshaller(modelPackageName);
                    } else {
                        throw new IllegalArgumentException("You must specify the package name or factory class of the target object model when using xml responses.");
                    }

                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

        InterfaceConfigFactory configFactory;
        switch (configType) {
            default:
            case CFG_TYPE_ANNO:
                if (properties != null) {
                    configFactory = new OverridingInterfaceConfigFactory(
                            new AnnotationDrivenInterfaceConfigFactory(),
                            new PropertiesDrivenInterfaceFactory(properties, false)
                    );
                } else {
                    configFactory = new AnnotationDrivenInterfaceConfigFactory();
                }
                break;
            case CFG_TYPE_PROP:
                configFactory = new PropertiesDrivenInterfaceFactory(properties);
                break;
        }
        if (overridesFactory != null) {
            configFactory = new OverridingInterfaceConfigFactory(configFactory, overridesFactory);
        }

        RequestInterceptor paramInterceptor = null;
        if (!Maps.areEmpties(queryString, headers, body)) {
            paramInterceptor = new RequestParamDefaultsInterceptor(queryString, headers, body);
        }

        RequestInterceptor oauthInterceptor = null;
        if (Strings.isNotBlank(accessToken) && Strings.isNotBlank(accessTokenSecret) && Strings.isNotBlank(consumerKey) && Strings.isNotBlank(consumerSecret)) {
            oauthInterceptor = new OAuthInterceptor(authParamsInHeaders ? OAuthInterceptor.OAuthParamDest.HEADERS : OAuthInterceptor.OAuthParamDest.URL, consumerSecret, consumerKey, accessTokenSecret, accessToken);
        }

        RequestInterceptor globalInterceptor = null;

        if (paramInterceptor != null && oauthInterceptor != null) {
            globalInterceptor = new CompositeRequestInterceptor(paramInterceptor, oauthInterceptor);
        } else if (paramInterceptor != null) {
            globalInterceptor = paramInterceptor;
        } else if (oauthInterceptor != null) {
            globalInterceptor = oauthInterceptor;
        }

        if (globalInterceptor != null) {
            try {
                configFactory = new OverridingInterfaceConfigFactory(configFactory, new ConfigBuilders.InterfaceConfigBuilder()
                        .setRequestInterceptor(globalInterceptor)
                        .buildOverrideTemplate());
            } catch (Exception e) {
                throw new CRestException(e);
            }
        }

        customProperties = Maps.defaultsIfNull(customProperties);
        Maps.putIfNotPresent(customProperties, Marshaller.class.getName(), marshaller);
        Maps.putIfNotPresent(customProperties, Unmarshaller.class.getName(), unmarshaller);
        Maps.putIfNotPresent(customProperties, CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP, serializersMap);

        CRestContext context = new DefaultCRestContext(restService, proxyFactory, configFactory, customProperties);
        return new DefaultCRest(context);
    }

    /**
     * Sets a default request query string parameter for requests fired from every services build with the resulting CRest instance.
     *
     * @param name  query string parameter name
     * @param value query string parameter value
     * @return current builder
     */
    public CRestBuilder addGlobalRequestParam(String name, String value) {
        this.queryString.put(name, value);
        return this;
    }

    /**
     * Adds all default request query string parameters for requests fired from every services build with the resulting CRest instance.
     *
     * @param param query string parameters map
     * @return current builder
     */
    public CRestBuilder addGlobalRequestParams(Map<String, String> param) {
        this.queryString.putAll(param);
        return this;
    }

    /**
     * Sets all default request query string parameters for requests fired from every services build with the resulting CRest instance.
     *
     * @param param query string parameters map
     * @return current builder
     */
    public CRestBuilder setGlobalRequestParams(Map<String, String> param) {
        this.queryString = param;
        return this;
    }

    /**
     * Sets a default request header for requests fired from every services build with the resulting CRest instance.
     *
     * @param name  header key
     * @param value header value
     * @return current builder
     */
    public CRestBuilder addGlobalRequestHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Adds all default request headers for requests fired from every services build with the resulting CRest instance.
     *
     * @param headers headers map
     * @return current builder
     */
    public CRestBuilder addGlobalRequestHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Sets default headers for requests fired from every services build with the resulting CRest instance.
     *
     * @param headers headers map
     * @return current builder
     */
    public CRestBuilder setGlobalRequestHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Sets a default request body parameters for requests fired from every services build with the resulting CRest instance.
     *
     * @param name  body parameter key
     * @param value body parameter value
     * @return current builder
     */
    public CRestBuilder addGlobalRequestBody(String name, Object value) {
        this.body.put(name, value);
        return this;
    }

    /**
     * Adds all default body parameters for requests fired from every services build with the resulting CRest instance.
     *
     * @param body body parameters map
     * @return current builder
     */
    public CRestBuilder addGlobalRequestBodies(Map<String, Object> body) {
        this.body.putAll(body);
        return this;
    }

    /**
     * Adds all default body parameters for requests fired from every services build with the resulting CRest instance.
     *
     * @param body body parameters map
     * @return current builder
     */
    public CRestBuilder setGlobalRequestBodies(Map<String, Object> body) {
        this.body = body;
        return this;
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
     * @see CRestContext#getProperties()
     */
    public CRestBuilder addProperty(String name, Object value) {
        customProperties.put(name, value);
        return this;
    }

    /**
     * Sets a custom serializer for the given type the resulting CRest instance will use to serialize method arguments.
     * <p>The given type reflects the given Interface type, polymorphism is not considered.
     * @param type Type to seralize
     * @param serializer Serializer
     * @return current builder
     * @see CRestContext#getProperties()
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
     * @see CRestContext#getProperties()
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
     * @see CRestContext#getProperties()
     */
    public CRestBuilder setProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
        return this;
    }

    /**
     * Resulting CRest instance will handle annotated configurated interfaces.
     *
     * @return current builder
     * @see org.codegist.crest.config.AnnotationDrivenInterfaceConfigFactory
     */
    public CRestBuilder withAnnotatedConfig() {
        this.configType = CFG_TYPE_ANNO;
        this.properties = null;
        return this;
    }

    /**
     * Resulting CRest instance will handle properties bases configuration.
     * <p>Given properties must be able to configure any possible interface given to the resulting CRest instance.
     *
     * @param props
     * @return current builder
     */
    public CRestBuilder withPropertiesConfig(Properties props) {
        this.configType = CFG_TYPE_PROP;
        this.properties = props;
        return this;
    }

    /**
     * Resulting CRest instance will overrides any configuration resulting from its current {@link org.codegist.crest.config.InterfaceConfigFactory} with the given properties.
     * <p>Properties must be formatted as documentated in {@link org.codegist.crest.config.PropertiesDrivenInterfaceFactory}
     * <p>Can be used for instance to override the server end-point for differents devs environment.
     *
     * @param props properties
     * @return current builder
     * @see org.codegist.crest.config.PropertiesDrivenInterfaceFactory
     */
    public CRestBuilder overrideDefaultConfigWith(Properties props) {
        this.properties = props;
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
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.authParamsInHeaders = authParamsInHeaders;
        return this;
    }

}                                                                                                        