package org.codegist.crest;

import org.codegist.common.io.IOs;
import org.codegist.common.marshal.Marshaller;
import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.config.ConfigBuilders;
import org.codegist.crest.config.InterfaceConfig;
import org.codegist.crest.config.PreconfiguredInterfaceConfigFactory;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.serializer.Serializer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.codegist.crest.TestUtils.getMethod;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class DefaultCRestTest {

    private static final String MODEL_RESPONSE_JSON = "{\"a\":\"aaa\",\"b\":\"bbb\",\"c\":\"ccc\"}";
    private static final Model MODEL_RESPONSE = new Model("aaa", "bbb", "ccc");
    final int a = 100;
    final int d = 1000;
    final String[] b = {"b b1", "bb2"};
    final Model m = new Model("aVal", "bVal", "cVal");
    final String c = "cc";
    final String c2 = "c/c";

    private static final Marshaller mockMarshaller = mock(Marshaller.class);
    private ProxyFactory mockProxyFactory = TestUtils.mockProxyFactory();

    @BeforeClass
    public static void setup() {
        when(mockMarshaller.<Object>marshall(any(InputStream.class), any(Type.class))).thenReturn(MODEL_RESPONSE);

    }

    @Test
    public void testRawReturnType() throws IOException {

        RestService mockRestService = mock(RestService.class);
        when(mockRestService.exec(any(HttpRequest.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                try {
                    return new HttpResponse(new HttpRequest.Builder("http://test", "utf-8").build(), 200, new ByteArrayInputStream(MODEL_RESPONSE_JSON.getBytes()), "text/html");
                } catch (URISyntaxException e) {
                    return null;
                }
            }
        });
        DefaultCRest rawConfiguredFactory = new DefaultCRest(
                new DefaultCRestContext(
                        mockRestService,
                        mockProxyFactory,
                        new PreconfiguredInterfaceConfigFactory(RawInterface.CONFIG),
                        null)); // No marshaller
        RawInterface raw = rawConfiguredFactory.build(RawInterface.class);


        raw.testVoid();
        assertEquals(MODEL_RESPONSE_JSON, IOs.toString(raw.testInputStream()));
        assertEquals(MODEL_RESPONSE_JSON, IOs.toString(raw.testReader()));
        assertEquals(MODEL_RESPONSE_JSON, raw.testString());


        DefaultCRest jsonConfiguredFactory = new DefaultCRest(new DefaultCRestContext(
                mockRestService,
                mockProxyFactory,
                new PreconfiguredInterfaceConfigFactory(ModelInterface.CONFIG),
                new HashMap<String, Object>() {{
                    put(Marshaller.class.getName(), mockMarshaller);
                }}
        ));
        ModelInterface model = jsonConfiguredFactory.build(ModelInterface.class);
        model.testVoid();
        assertEquals(MODEL_RESPONSE_JSON, IOs.toString(model.testInputStream()));
        assertEquals(MODEL_RESPONSE_JSON, IOs.toString(model.testReader()));
        try {
            assertEquals(MODEL_RESPONSE_JSON, model.testString());
            fail("Should have fail as it should have try to marshall it");
        } catch (Exception e) {

        }
        assertNotNull(model.testModel());
    }

    public static interface RawInterface {
        void testVoid();

        String testString();

        InputStream testInputStream();

        Reader testReader();

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(RawInterface.class, "http://test.com").build();
    }


    public static interface ModelInterface {
        void testVoid();

        String testString();

        InputStream testInputStream();

        Reader testReader();

        Model testModel();

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(ModelInterface.class, "http://test.com").build();
    }


    @Test
    public void testFactory() {
        RestService mockRestService = mock(RestService.class);
        when(mockRestService.exec(argThat(new ArgumentMatcher<HttpRequest>() {
            @Override
            public boolean matches(Object o) {
                HttpRequest r = (HttpRequest) o;
                assertNotNull(r);
                assertEquals("utf-8", r.getEncoding());
                String u = r.getUri().toString();
                if (u.contains("aaa")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(10, r.getConnectionTimeout().intValue());
                    assertEquals(HttpMethod.GET, r.getMeth());
                    assertEquals("http://test-server:8080/path/aaa", r.getUri().toString());
                    assertEquals(0, r.getBodyParams().size());
                    assertEquals(2, r.getQueryParams().size());
                } else if (u.contains("bbb")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(55, r.getConnectionTimeout().intValue());
                    assertEquals(HttpMethod.GET, r.getMeth());
                    assertEquals("http://test-server:8080/path/bbb/c/c", r.getUri().toString());
                    assertEquals(0, r.getBodyParams().size());
                    assertEquals(2, r.getQueryParams().size());
                } else if (u.contains("ccc")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(10, r.getConnectionTimeout().intValue());
                    assertEquals(HttpMethod.POST, r.getMeth());
                    assertEquals("http://test-server:8080/path/ccc/100", r.getUri().toString());
                    assertEquals(1, r.getBodyParams().size());
                    assertEquals(1, r.getQueryParams().size());
                    assertEquals("1000", r.getQueryParams().get("aa"));
                    assertEquals("b b1,bb2", r.getBodyParams().get("bb"));
                } else if (u.contains("ddd")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(10, r.getConnectionTimeout().intValue());
                    assertEquals(HttpMethod.POST, r.getMeth());
                    assertEquals("http://test-server:8080/path/ddd", r.getUri().toString());
                    assertEquals(1, r.getQueryParams().size());
                    assertEquals(3, r.getBodyParams().size());
                    assertEquals("aVal", r.getBodyParams().get("aa"));
                    assertEquals("cVal", r.getBodyParams().get("c"));
                    assertEquals("b b1,bb2", r.getBodyParams().get("bb"));

                    assertEquals("cc", r.getQueryParams().get("c"));
                }

                return true;
            }

        }))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new HttpResponse(null, 200, new ByteArrayInputStream(MODEL_RESPONSE_JSON.getBytes()), "text/html");
            }
        });

        CRest factory = new DefaultCRest(new DefaultCRestContext(
                mockRestService,
                new JdkProxyFactory(),
                new PreconfiguredInterfaceConfigFactory(Rest.CONFIG),
                new HashMap<String, Object>() {{
                    put(Marshaller.class.getName(), mockMarshaller);
                }}
        ));

        Rest restInterface = factory.build(Rest.class);
        restInterface.aaa(a, b);
        restInterface.bbb(a, b, c2);
        restInterface.ccc(a, d, b);
        Model res = restInterface.ddd(m, b, c);
        assertNotNull(res);
        assertEquals("aaa", res.getA());
        assertEquals("bbb", res.getB());
        assertEquals("ccc", res.getC());
    }

    public static interface Rest {

        void aaa(int a, String[] b);

        Method AAA = getMethod(Rest.class, "aaa", int.class, String[].class);

        void bbb(int a, String[] b, String c);

        Method BBB = getMethod(Rest.class, "bbb", int.class, String[].class, String.class);

        void ccc(int a, int d, String[] b);

        Method CCC = getMethod(Rest.class, "ccc", int.class, int.class, String[].class);


        Model ddd(Model a, String[] b, String c);

        Method DDD = getMethod(Rest.class, "ddd", Model.class, String[].class, String.class);

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Rest.class, "http://test-server:8080")
                .setPath("/path")
                .setMethodsSocketTimeout(15l)
                .setMethodsConnectionTimeout(10l)
                .setEncoding("utf-8")
                .startMethodConfig(AAA).setPath("/aaa?b={1}&a={0}").endMethodConfig()
                .startMethodConfig(BBB).setPath("/bbb/{2}?b={1}&a={0}")
                .setConnectionTimeout(55l)
                .startParamConfig(0).setSerializer(new Ser()).endParamConfig()
                .endMethodConfig()
                .startMethodConfig(CCC).setPath("/ccc/{0}?aa={1}")
                .setHttpMethod(HttpMethod.POST)
                .startParamConfig(0).setDestination("URL").endParamConfig()
                .startParamConfig(1).setDestination("URL").endParamConfig()
                .startParamConfig(2).setDestination("BODY").setName("bb").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(DDD).setPath("/ddd?c={2}")
                .setHttpMethod(HttpMethod.POST)
                .startParamConfig(0)
                .setDestination("BODY")
                .setInjector(new AnnotatedBeanParamInjector())
                .endParamConfig()
                .startParamConfig(1).setDestination("BODY").setName("bb").endParamConfig()
                .endMethodConfig()
                .build();
    }

    public static class Ser implements Serializer {
        @Override
        public String serialize(ParamContext context) {
            return "oooo";
        }
    }


    public static class Model {
        @RestSerialize(name = "aa")
        private String a;
        @RestSerialize(exclude = true)
        private String b;
        private String c;

        public Model() {

        }

        public Model(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }
    }


    /**
     * Created by IntelliJ IDEA.
     * User: laurent.gilles
     * Date: 15-Oct-2010
     * Time: 12:58:49
     * To change this template use File | Settings | File Templates.
     */
    static class AnnotatedBeanParamInjector implements RequestInjector {

        @Override
        public void inject(HttpRequest.Builder builder, ParamContext context) {
            if (context.getArgValue() == null) return;

            Map map = new HashMap();
            Field[] fields = context.getArgValue().getClass().getDeclaredFields();

            for (final Field f : fields) {
                AccessController.doPrivileged(new SetAccessible(f));
                RestSerialize annot = f.getAnnotation(RestSerialize.class);
                if (annot != null && annot.exclude()) continue;

                String name = annot == null || annot.name() == null || annot.name().isEmpty() ? f.getName() : annot.name();

                Object val = null;
                try {
                    val = f.get(context.getArgValue());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (val != null) {
                    map.put(name, val.toString());
                }
            }

            if (context.isForUrl()) {
                builder.addQueryParams(map);
            } else {
                builder.addBodyParams(map);
            }

        }

        private static class SetAccessible implements PrivilegedAction {
            private final Field field;

            private SetAccessible(Field field) {
                this.field = field;
            }

            @Override
            public Object run() {
                field.setAccessible(true);
                return null;
            }
        }
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})
    public @interface RestSerialize {

        String name() default "";

        boolean exclude() default false;

    }


}
