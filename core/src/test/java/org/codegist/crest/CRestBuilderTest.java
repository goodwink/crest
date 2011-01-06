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
import org.codegist.common.marshal.JacksonMarshaller;
import org.codegist.common.marshal.JaxbMarshaller;
import org.codegist.common.marshal.Marshaller;
import org.codegist.common.marshal.Unmarshaller;
import org.codegist.common.reflect.CglibProxyFactory;
import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.*;
import org.codegist.crest.oauth.OAuthenticator;
import org.codegist.crest.oauth.Token;
import org.codegist.crest.security.AuthentificationManager;
import org.codegist.crest.security.OAuthentificationManager;
import org.codegist.crest.serializer.Serializer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
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
    public void testExpectsJson() {
        final CRestContext context = builder
                .expectsJson()
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(Marshaller.class.getName(), new JacksonMarshaller());
                    put(Unmarshaller.class.getName(), new JacksonMarshaller());
                }};
            }
        }, context);
    }

    @Test
    public void testExpectsXml1() {
        final CRestContext context = builder
                .expectsXml(Object.class)
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(Marshaller.class.getName(), new JaxbMarshaller(Object.class));
                    put(Unmarshaller.class.getName(), new JaxbMarshaller(Object.class));
                }};
            }
        }, context);
    }

    @Test
    public void testExpectsXml2() {
        final CRestContext context = builder
                .expectsXml("org.codegist.crest")
                .buildContext();
        assertContext(new ContextAdapter() {
            @Override
            public Map<String, Object> getProperties() {
                return new HashMap<String, Object>() {{
                    put(Marshaller.class.getName(), new JaxbMarshaller(Object.class));
                    put(Unmarshaller.class.getName(), new JaxbMarshaller(Object.class));
                }};
            }
        }, context);
    }

    @Test
    public void testExpectsRaw() {
        final CRestContext context = builder.returnRawResults().buildContext();
        assertContext(context);
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
        assertHttpClient(context);
        HttpClientRestService service = (HttpClientRestService) context.getRestService();
        assertTrue(service.getHttpClient().getConnectionManager() instanceof SingleClientConnManager);
    }

    @Test
    public void testApacheHttpClient2() {
        CRestContext context = builder
                .useHttpClientRestService(2)
                .buildContext();
        assertHttpClient(context);
        HttpClientRestService service = (HttpClientRestService) context.getRestService();
        assertTrue(service.getHttpClient().getConnectionManager() instanceof ThreadSafeClientConnManager);
        assertEquals(2, ConnManagerParams.getMaxTotalConnections(service.getHttpClient().getParams()));
        assertEquals(2, ConnManagerParams.getMaxConnectionsPerRoute(service.getHttpClient().getParams()).getMaxForRoute(new HttpRoute(new HttpHost("127.0.0.1"))));

    }

    @Test
    public void testApacheHttpClient3() {
        CRestContext context = builder
                .useHttpClientRestService(4, 2)
                .buildContext();
        assertHttpClient(context);
        HttpClientRestService service = (HttpClientRestService) context.getRestService();
        assertTrue(service.getHttpClient().getConnectionManager() instanceof ThreadSafeClientConnManager);
        assertEquals(4, ConnManagerParams.getMaxTotalConnections(service.getHttpClient().getParams()));
        assertEquals(2, ConnManagerParams.getMaxConnectionsPerRoute(service.getHttpClient().getParams()).getMaxForRoute(new HttpRoute(new HttpHost("127.0.0.1"))));

    }

    public void assertHttpClient(final CRestContext context) {
        assertContext(
                new ContextAdapter() {
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
            assertEquals(AnnotationDrivenInterfaceConfigFactory.class, context.getConfigFactory().getClass());
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
            put(InterfaceConfigFactory.class.getName(), context.getConfigFactory());
            put(CRestProperty.SERIALIZER_CUSTOM_SERIALIZER_MAP, Collections.emptyMap());
            put(AuthentificationManager.class.getName(), null);
            put(Marshaller.class.getName(), null);
            put(Unmarshaller.class.getName(), null);
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
            } else {
                assertEquals(val.getClass(), test.get(entry.getKey()).getClass());
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
