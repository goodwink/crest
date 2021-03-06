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

package org.codegist.crest;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.codegist.common.reflect.CglibProxyFactory;
import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.annotate.EndPoint;
import org.codegist.crest.config.*;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.Token;
import org.codegist.crest.security.AuthentificationManager;
import org.codegist.crest.security.OAuthentificationManager;
import org.codegist.crest.serializer.DeserializerFactory;
import org.codegist.crest.serializer.JacksonDeserializer;
import org.codegist.crest.serializer.JaxbDeserializer;
import org.codegist.crest.serializer.Serializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author laurent.gilles@codegist.org
 */
public class CRestBuilderTest {

    private CRestBuilder builder;

    @Before
    public void setup() {
        builder = new CRestBuilder();
    }

    @Test
    public void testDefault() {
        final CRestContext context = builder.buildContext();
        assertContext(context);
    }


    @Test
    public void testPlaceHolders() throws ConfigFactoryException {
        CRestContext cRestContext = builder
                .setConfigPlaceholder("server", "120")
                .setConfigPlaceholder("port", "8081")
                .buildContext();
        
        InterfaceConfig cfg = cRestContext.getConfigFactory().newConfig(In.class, cRestContext);
        assertEquals("http://120:8081", cfg.getEndPoint());

    }

    @EndPoint("http://{server}:{port}")
    static interface In {

    }

    @Test
    public void testProperties() {
        final CRestContext context = builder
                .setProperties(new HashMap<String, Object>() {{
                    put("a", "b");
                    put("b", "c");
                }})
                .addProperties(new HashMap<String, Object>() {{
                    put("c", "d");
                    put("d", "e");
                }})
                .setProperty("e", "f")
                .setProperty("f", "g")
                .buildContext();
        assertContext(new ContextAdapter() {
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put("a", "b");
                    put("b", "c");
                    put("c", "d");
                    put("d", "e");
                    put("e", "f");
                    put("f", "g");
                }};
            }
        }, context);
    }


    @Test
    public void testOAuth() {
        final CRestContext context = builder
                .usePreauthentifiedOAuth("consumerKey", "consumerSecret", "accessToken", "accessTokenSecret")
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public InterfaceConfigFactory getConfigFactory() {
                return new OverridingInterfaceConfigFactory(mock(InterfaceConfigFactory.class), mock(InterfaceConfig.class));
            }

            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.OAUTH_CONSUMER_KEY, "consumerKey");
                    put(CRestProperty.OAUTH_CONSUMER_SECRET, "consumerSecret");
                    put(CRestProperty.OAUTH_ACCESS_TOKEN, "accessToken");
                    put(CRestProperty.OAUTH_ACCESS_TOKEN_SECRET, "accessTokenSecret");
                    put(CRestProperty.OAUTH_PARAM_DEST, "header");
                    put(AuthentificationManager.class.getName(), new OAuthentificationManager(mock(OAuthenticator.class), mock(Token.class)));
                }};
            }
        }, context);
    }

    @Test
    public void testSerializers() {
        final CRestContext context = builder
                .setBooleanSerializer("a", "b")
                .setDateSerializerFormat("dd/mm/yyyy")
                .setListSerializerSeparator("-")
                .setSerializer(Integer.class, Stubs.Serializer1.INSTANCE)
                .setSerializer(long.class, Stubs.Serializer2.INSTANCE)
                .buildContext();
        assertContext(new ContextAdapter() {
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.SERIALIZER_LIST_SEPARATOR, "-");
                    put(CRestProperty.SERIALIZER_DATE_FORMAT, "dd/mm/yyyy");
                    put(CRestProperty.SERIALIZER_BOOLEAN_TRUE, "a");
                    put(CRestProperty.SERIALIZER_BOOLEAN_FALSE, "b");
                    put(CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP, new HashMap<Type, Serializer>() {{
                        put(Integer.class, Stubs.Serializer1.INSTANCE);
                        put(long.class, Stubs.Serializer2.INSTANCE);
                    }});
                }};
            }
        }, context);
    }


    @Test
    public void testConfigXml() {
        final CRestContext context = builder
                .withXmlConfig(null)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public InterfaceConfigFactory getConfigFactory() {
                return new XmlDrivenInterfaceConfigFactory(null);
            }
        }, context);
    }

    @Test
    public void testConfigProperties() {
        final CRestContext context = builder
                .withPropertiesConfig(null)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public InterfaceConfigFactory getConfigFactory() {
                return new PropertiesDrivenInterfaceConfigFactory(null);
            }
        }, context);
    }

    @Test
    public void testConfigAnnotation() {
        final CRestContext context = builder
                .withAnnotatedConfig()
                .buildContext();
        assertContext(context);
    }

    @Test
    public void testOverrideConfigWithProperties() {
        final CRestContext context = builder
                .overrideDefaultConfigWith(mock(Map.class))
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public InterfaceConfigFactory getConfigFactory() {
                return new OverridingInterfaceConfigFactory(mock(InterfaceConfigFactory.class), mock(InterfaceConfig.class));
            }
        }, context);
    }

    @Test
    public void testOverrideConfigWithXml() {
        final CRestContext context = builder
                .overrideDefaultConfigWith(mock(Document.class))
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public InterfaceConfigFactory getConfigFactory() {
                return new OverridingInterfaceConfigFactory(mock(InterfaceConfigFactory.class), mock(InterfaceConfig.class));
            }
        }, context);
    }

    @Test
    public void testOverrideConfigWithConfig() {
        final CRestContext context = builder
                .overrideDefaultConfigWith(mock(InterfaceConfigFactory.class))
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public InterfaceConfigFactory getConfigFactory() {
                return new OverridingInterfaceConfigFactory(mock(InterfaceConfigFactory.class), mock(InterfaceConfig.class));
            }
        }, context);
    }

    @Test
    public void testSetRestService() {
        final CRestContext context = builder
                .setRestService(Stubs.RestService1.INSTANCE)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public RestService getRestService() {
                return Stubs.RestService1.INSTANCE;
            }
        }, context);
    }

    @Test
    public void testConsumesCustom() {
        final CRestContext context = builder
                .consumes("blabla", new Stubs.Deserializer1())
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new Stubs.Deserializer1());
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[]{
                            new ConfigBuilders.ParamConfigBuilder(null)
                            .setName("Accept")
                            .setDefaultValue("blabla")
                            .setDestination(HttpRequest.DEST_HEADER)
                            .build()
                    });
                }};
            }
        }, context);
    }
    @Test
    public void testConsumesCustom2() {
        final CRestContext context = builder
                .consumes("blabla", new Stubs.Deserializer1(), false)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new Stubs.Deserializer1());
                }};
            }
        }, context);
    }

    @Test
    public void testConsumesJson() {
        final CRestContext context = builder
                .consumesJson()
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JacksonDeserializer(mock(ObjectMapper.class)));
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[]{
                            new ConfigBuilders.ParamConfigBuilder(null)
                            .setName("Accept")
                            .setDefaultValue("application/json")
                            .setDestination(HttpRequest.DEST_HEADER)
                            .build()
                    });
                }};
            }
        }, context);
    }
    @Test
    public void testConsumesJson2() {
        final CRestContext context = builder
                .consumesJson(false)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
            return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JacksonDeserializer(mock(ObjectMapper.class)));
                }};
            }
        }, context);
    }
    @Test
    public void testConsumesJson3() {
        final CRestContext context = builder
                .consumesJson(null)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
            return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JacksonDeserializer(mock(ObjectMapper.class)));
                }};
            }
        }, context);
    }
    @Test
    public void testConsumesJson4() {
        final CRestContext context = builder
                .consumesJson("fff")
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JacksonDeserializer(mock(ObjectMapper.class)));
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[]{
                            new ConfigBuilders.ParamConfigBuilder(null)
                            .setName("Accept")
                            .setDefaultValue("fff")
                            .setDestination(HttpRequest.DEST_HEADER)
                            .build()
                    });
                }};
            }
        }, context);
    }

    @Test
    public void testConsumesXml1() {
        final CRestContext context = builder
                .deserializeXmlWithJaxb(String.class)
                .consumesXml()
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JaxbDeserializer(mock(JAXBContext.class)));
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[]{
                            new ConfigBuilders.ParamConfigBuilder(null)
                            .setName("Accept")
                            .setDefaultValue("application/xml")
                            .setDestination(HttpRequest.DEST_HEADER)
                            .build()
                    });
                }};
            }
        }, context);
    }

    @Test
    public void testConsumesXml2() {
        final CRestContext context = builder
                .deserializeXmlWithJaxb(String.class)
                .consumesXml()
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JaxbDeserializer(mock(JAXBContext.class)));
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[]{
                            new ConfigBuilders.ParamConfigBuilder(null)
                            .setName("Accept")
                            .setDefaultValue("application/xml")
                            .setDestination(HttpRequest.DEST_HEADER)
                            .build()
                    });
                }};
            }
        }, context);
    }
    @Test
    public void testConsumesXml3() {
        final CRestContext context = builder
                .deserializeXmlWithJaxb(String.class)
                .consumesXml(false)
                .buildContext();
        assertContext(new ContextAdapter(){
            @Override
            public Map<String, Object> getProperties() {
            return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JaxbDeserializer(mock(JAXBContext.class)));
                }};
            }
        }, context);
    }

    @Test
    public void testConsumesXml4() {
        final CRestContext context = builder
                .deserializeXmlWithJaxb(String.class)
                .consumesXml(null)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
            return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JaxbDeserializer(mock(JAXBContext.class)));
                }};
            }
        }, context);
    }

    @Test
    public void testConsumesXml5() {
        final CRestContext context = builder
                .deserializeXmlWithJaxb(String.class)
                .consumesXml("ddd")
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_DESERIALIZER, new JaxbDeserializer(mock(JAXBContext.class)));
                    put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[]{
                            new ConfigBuilders.ParamConfigBuilder(null)
                            .setName("Accept")
                            .setDefaultValue("ddd")
                            .setDestination(HttpRequest.DEST_HEADER)
                            .build()
                    });
                }};
            }
        }, context);
    }


    @Test
    public void testProxiesCGLib() {
        final CRestContext context = builder.useCglibProxies().buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public ProxyFactory getProxyFactory() {
                return new CglibProxyFactory();
            }
        }, context);
    }

    @Test
    public void testProxiesJdk() {
        final CRestContext context = builder.useJdkProxies().buildContext();
        assertContext(context);
    }

    @Test
    public void testApacheHttpClient1() {
        CRestContext context = builder
                .useHttpClientRestService()
                .buildContext();
        assertHttpClient(context, 0);
        HttpClientRestService service = (HttpClientRestService) context.getRestService();
        assertTrue(service.getHttpClient().getConnectionManager() instanceof SingleClientConnManager);
    }

    @Test
    public void testApacheHttpClient2() {
        CRestContext context = builder
                .useHttpClientRestService()
                .setConcurrencyLevel(2)
                .buildContext();
        assertHttpClient(context, 2);
        HttpClientRestService service = (HttpClientRestService) context.getRestService();
        assertTrue(service.getHttpClient().getConnectionManager() instanceof ThreadSafeClientConnManager);
        assertEquals(2, ConnManagerParams.getMaxTotalConnections(service.getHttpClient().getParams()));
        assertEquals(2, ConnManagerParams.getMaxConnectionsPerRoute(service.getHttpClient().getParams()).getMaxForRoute(new HttpRoute(new HttpHost("127.0.0.1"))));

    }

    public void assertHttpClient(final CRestContext context, final int concurrencyLvl) {
        assertContext(
                new ContextAdapter() {
                    @Override
                    public Map<String, Object> getProperties() {
                        return new HashMap<String, Object>(){{
                            if(concurrencyLvl > 0 )
                            put(CRestProperty.CREST_CONCURRENCY_LEVEL, concurrencyLvl);
                        }};
                    }

                    @Override
                    public RestService getRestService() {
                        return new HttpClientRestService();
                    }
                },
                context);
    }

    private void assertContext(final CRestContext context) {
        assertContext(null, context);
    }

    private void assertContext(final CRestContext expected, final CRestContext context) {
        if (expected != null && expected.getConfigFactory() != null) {
            assertEquals(expected.getConfigFactory().getClass(), context.getConfigFactory().getClass());
        } else {
            assertEquals(CRestAnnotationDrivenInterfaceConfigFactory.class, context.getConfigFactory().getClass());
        }
        if (expected != null && expected.getProxyFactory() != null) {
            assertEquals(expected.getProxyFactory().getClass(), context.getProxyFactory().getClass());
        } else {
            assertEquals(JdkProxyFactory.class, context.getProxyFactory().getClass());
        }
        if (expected != null && expected.getRestService() != null) {
            assertEquals(expected.getRestService().getClass(), context.getRestService().getClass());
        } else {
            assertEquals(DefaultRestService.class, context.getRestService().getClass());
        }

        Map<String, Object> expectedProps = new HashMap<String, Object>() {{
            put(RestService.class.getName(), context.getRestService());
            put(ProxyFactory.class.getName(), context.getProxyFactory());
            put(DeserializerFactory.class.getName(), new DeserializerFactory.Builder().build());
            put(InterfaceConfigFactory.class.getName(), context.getConfigFactory());
            put(CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP, Collections.emptyMap());
            put(CRestProperty.CONFIG_PLACEHOLDERS_MAP, Collections.emptyMap());
            put(AuthentificationManager.class.getName(), null);
            put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new ParamConfig[0]);
        }};
        if (expected != null && expected.getProperties() != null) {
            expectedProps.putAll(expected.getProperties());
        }

        assertMapEquals(expectedProps, context.getProperties());
    }

    private static void assertMapEquals(Map<?, ?> control, Map<?, ?> test) {
        assertEquals(control.size(), test.size());
        for (Map.Entry<?, ?> entry : control.entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                assertNull(test.get(entry.getKey()));
            } else if (val instanceof Map) {
                assertMapEquals((Map) val, (Map) test.get(entry.getKey()));
            } else if (val instanceof String || val instanceof Number || val instanceof Boolean) {
                assertEquals(val, test.get(entry.getKey()));
            } else if(val instanceof ParamConfig[]){
                ParamConfig[] valv = (ParamConfig[]) val;
                ParamConfig[] testv = (ParamConfig[]) test.get(entry.getKey());

                assertArrayEquals(valv, testv);
            } else{

                assertEquals(TestUtils.getClass(val), TestUtils.getClass(test.get(entry.getKey())));
            }
        }
    }


    private abstract static class ContextAdapter implements CRestContext {
        public RestService getRestService() {
            return null;
        }

        public ProxyFactory getProxyFactory() {
            return null;
        }

        public InterfaceConfigFactory getConfigFactory() {
            return null;
        }

        public Map<String, Object> getProperties() {
            return null;
        }
    }
}
