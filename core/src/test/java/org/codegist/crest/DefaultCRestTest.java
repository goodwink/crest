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

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.codegist.common.io.IOs;
import org.codegist.common.lang.Disposables;
import org.codegist.common.lang.Strings;
import org.codegist.common.reflect.JdkProxyFactory;
import org.codegist.common.reflect.ProxyFactory;
import org.codegist.crest.annotate.EndPoint;
import org.codegist.crest.annotate.HeaderParam;
import org.codegist.crest.annotate.Path;
import org.codegist.crest.annotate.QueryParam;
import org.codegist.crest.config.*;
import org.codegist.crest.handler.MaxAttemptRetryHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.serializer.Deserializer;
import org.codegist.crest.serializer.DeserializerFactory;
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
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.codegist.crest.TestUtils.getMethod;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DefaultCRestTest {

    private static final String MODEL_RESPONSE_JSON = "{\"a\":\"aaa\",\"b\":\"bbb\",\"c\":\"ccc\"}";
    private static final Model MODEL_RESPONSE = new Model("aaa", "bbb", "ccc");
    final int a = 100;
    final int d = 1000;
    final String[] b = {"b b1", "bb2"};
    final Model m = new Model("aVal", "bVal", "cVal");
    final String c = "cc";
    final String c2 = "c/c";

    private static final Deserializer mockDeserializer = mock(Deserializer.class);
    private static final DeserializerFactory mockDeserializerFACTORY = mock(DeserializerFactory.class); static {
        when(mockDeserializerFACTORY.buildForMimeType(anyString())).thenReturn(mockDeserializer);
    }
    private ProxyFactory mockProxyFactory = TestUtils.mockProxyFactory();

    @BeforeClass
    public static void setup() {   
        when(mockDeserializer.<Object>deserialize(any(Reader.class), any(Type.class))).thenReturn(MODEL_RESPONSE);
    }

    @Test
    public void testPath(){
        RestService service = mock(RestService.class);
        when(service.exec(argThat(new ArgumentMatcher<HttpRequest>() {

            public boolean matches(Object o) {
                HttpRequest r = (HttpRequest) o;
                String expected = r.getQueryParams().get("expected");
                assertEquals(expected, r.getUri().toString());
                return true;
            }
        }))).thenAnswer(new Answer<Object>() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new HttpResponse(null, 200, null, (new HttpResource() {
                    final InputStream stream = new ByteArrayInputStream("".getBytes());
                    public InputStream getContent() throws HttpException {
                        return stream;
                    }

                    public void release() throws HttpException {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            throw new HttpException(e);
                        }
                    }
                }));
            }
        });
        CRest crest = new DefaultCRest(new DefaultCRestContext(service, mockProxyFactory, new CRestAnnotationDrivenInterfaceConfigFactory(), Collections.<String, Object>emptyMap()));
        I1 i1 = crest.build(I1.class);
        I2 i2 = crest.build(I2.class);
        i1.m("http://localhost/service/path/meth/path");
        i2.m("http://localhost/service/path/meth/path");
        Map<String,Object> properties = new HashMap<String, Object>();
        properties.put(CRestProperty.CREST_URL_ADD_SLASHES, false);
        crest = new DefaultCRest(new DefaultCRestContext(service, mockProxyFactory, new CRestAnnotationDrivenInterfaceConfigFactory(), properties));
        i1 = crest.build(I1.class);
        i2 = crest.build(I2.class);
        i1.m("http://localhostservice/pathmeth/path");
        i2.m("http://localhost/service/path/meth/path");

    }
    @EndPoint("http://localhost")
    @Path("service/path")
    public static interface I1 {
        @Path("meth/path")
        void m(@QueryParam("expected") String path);
    }
    @EndPoint("http://localhost")
    @Path("/service/path")
    public static interface I2 {
        @Path("/meth/path")
        void m(@QueryParam("expected") String path);
    }

    @Test
    public void testDispose(){
        ClientConnectionManager conMan = mock(ClientConnectionManager.class);
        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.getConnectionManager()).thenReturn(conMan);
        RestService service = new HttpClientRestService(mockClient);

        CRest crest = new DefaultCRest(new DefaultCRestContext(service, mockProxyFactory, mock(InterfaceConfigFactory.class), Collections.<String, Object>emptyMap()));
        Disposables.dispose(crest);
        verify(conMan).shutdown();
    }

    @Test
    public void testSuccessWithRetry(){
        int maxRetries = 3;
        int throwErrorsCount = 3;
        CRest crest = buildRetryableCrest(maxRetries,throwErrorsCount);
        RetryTest interfaze = crest.build(RetryTest.class);
        interfaze.doIt();
    }
    
    @Test(expected = CRestException.class)
    public void testFailureWithRetry(){
        int maxRetries = 3;
        int throwErrorsCount = 4;
        CRest crest = buildRetryableCrest(maxRetries,throwErrorsCount);
        RetryTest interfaze = crest.build(RetryTest.class);
        interfaze.doIt();
    }

    private CRest buildRetryableCrest(final int maxRetry, final int throwErrorsCount){
        RetryHandler retryHandler  = mock(RetryHandler.class);
        when(retryHandler.retry(any(ResponseContext.class), any(Exception.class), anyInt())).thenAnswer(new Answer<Object>() {
            private RetryHandler delegate = new MaxAttemptRetryHandler(maxRetry);
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return delegate.retry(
                        (ResponseContext)invocationOnMock.getArguments()[0],
                        (Exception)invocationOnMock.getArguments()[1],
                        (Integer)invocationOnMock.getArguments()[2]);
            }
        });
        RestService mockRestService = mock(RestService.class);
        when(mockRestService.exec(any(HttpRequest.class))).thenAnswer(new Answer<Object>() {
            private int i = 0;

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if(++i < throwErrorsCount)
                    throw new HttpException("error!", new HttpResponse((HttpRequest)invocationOnMock.getArguments()[0], 400));
                else
                    return new HttpResponse((HttpRequest)invocationOnMock.getArguments()[0], 200, null, (new HttpResource() {
                        final InputStream stream = new ByteArrayInputStream(new byte[]{10});
                        public InputStream getContent() throws HttpException {
                            return stream;
                        }

                        public void release() throws HttpException {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                throw new HttpException(e);
                            }
                        }
                    }));
            }
        });
        return new DefaultCRest(
                new DefaultCRestContext(
                        mockRestService,
                        mockProxyFactory,
                        new PreconfiguredInterfaceConfigFactory(
                                new ConfigBuilders.InterfaceConfigBuilder(RetryTest.class).setEndPoint("http://test.com")
                                        .setMethodsRetryHandler(retryHandler)
                                        .build()
                        ),
                        null));
    }

    private static interface RetryTest {
        String doIt();
    }

    @Test
    public void testRawReturnType() throws IOException {

        RestService mockRestService = mock(RestService.class);
        when(mockRestService.exec(any(HttpRequest.class))).thenAnswer(new Answer<Object>() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Map<String,List<String>> headers = new HashMap<String, List<String>>();
                headers.put("Content-Type", Arrays.asList("charset=utf-8"));
                try {
                    return new HttpResponse(new HttpRequest.Builder("http://test", "utf-8").build(), 200, headers, (new HttpResource() {
                        final InputStream stream = new ByteArrayInputStream(MODEL_RESPONSE_JSON.getBytes());
                        public InputStream getContent() throws HttpException {
                            return stream;
                        }

                        public void release() throws HttpException {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                throw new HttpException(e);
                            }
                        }
                    }));
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
                        null));
        RawInterface raw = rawConfiguredFactory.build(RawInterface.class);

        raw.testVoid();
        assertEquals(MODEL_RESPONSE_JSON, IOs.toString(raw.testInputStream()));
        assertEquals(MODEL_RESPONSE_JSON, IOs.toString(raw.testReader()));
        assertEquals(MODEL_RESPONSE_JSON, raw.testString());

        DefaultCRest jsonConfiguredFactory = new DefaultCRest(new DefaultCRestContext(
                mockRestService,
                mockProxyFactory,
                new PreconfiguredInterfaceConfigFactory(new ConfigBuilders.InterfaceConfigBuilder(ModelInterface.class).setEndPoint("http://test.com").setMethodsDeserializer(mockDeserializer).build()),
                null
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

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(RawInterface.class).setEndPoint("http://test.com").build();
    }


    public static interface ModelInterface {
        void testVoid();

        String testString();

        InputStream testInputStream();

        Reader testReader();

        Model testModel();
    }

    @Test
    public void testAcceptHeader(){
        RestService mockRestService = mock(RestService.class);
        when(mockRestService.exec(argThat(new ArgumentMatcher<HttpRequest>() {

            public boolean matches(Object o) {
                HttpRequest r = (HttpRequest) o;
                assertNotNull(r);
                String expectedHeader = r.getQueryParams().get("expected-header");
                String header = Strings.defaultIfBlank(r.getHeaderParams().get("Accept"), "");
                assertEquals(expectedHeader,header );
                return true;
            }
        }))).thenAnswer(new Answer<Object>() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new HttpResponse(null, 200, null, (new HttpResource() {
                    final InputStream stream = new ByteArrayInputStream("".getBytes());
                    public InputStream getContent() throws HttpException {
                        return stream;
                    }

                    public void release() throws HttpException {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            throw new HttpException(e);
                        }
                    }
                }));
            }
        });

        CRest crest = new CRestBuilder().consumesJson().setRestService(mockRestService).build();
        Int instance = crest.build(Int.class);
        instance.test("application/json");
        instance.test();

        crest = new CRestBuilder().consumesJson(false).setRestService(mockRestService).build();
        instance = crest.build(Int.class);
        instance.test(null);
        instance.test();

        crest = new CRestBuilder().consumesJson("text/json").setRestService(mockRestService).build();
        instance = crest.build(Int.class);
        instance.test("text/json");
        instance.test();

    }

    @EndPoint("http://hello")
    public static interface Int {
        public void test(@QueryParam("expected-header") String expectedHeader);

        @QueryParam(value="expected-header", defaultValue = "overridden")
        @HeaderParam(value="Accept", defaultValue = "overridden")
        public void test();
    }


    @Test
    public void testFactory() {
        RestService mockRestService = mock(RestService.class);
        when(mockRestService.exec(argThat(new ArgumentMatcher<HttpRequest>() {

            public boolean matches(Object o) {
                HttpRequest r = (HttpRequest) o;
                assertNotNull(r);
                assertEquals("utf-8", r.getEncoding());
                String u = r.getUri().toString();
                if (u.contains("aaa")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(10, r.getConnectionTimeout().intValue());
                    assertEquals("GET", r.getMeth());
                    assertEquals("http://test-server:8080/path/aaa", r.getUri().toString());
                    assertEquals(0, r.getFormParams().size());
                    assertEquals(2, r.getQueryParams().size());
                } else if (u.contains("bbb")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(55, r.getConnectionTimeout().intValue());
                    assertEquals("GET", r.getMeth());
                    assertEquals("http://test-server:8080/path/bbb/c/c", r.getUri().toString());
                    assertEquals(0, r.getFormParams().size());
                    assertEquals(2, r.getQueryParams().size());
                } else if (u.contains("ccc")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(10, r.getConnectionTimeout().intValue());
                    assertEquals("POST", r.getMeth());
                    assertEquals("http://test-server:8080/path/ccc/100", r.getUri().toString());
                    assertEquals(1, r.getFormParams().size());
                    assertEquals(1, r.getQueryParams().size());
                    assertEquals("1000", r.getQueryParams().get("aa"));
                    assertEquals("b b1,bb2", r.getFormParams().get("bb"));
                } else if (u.contains("ddd")) {
                    assertEquals(15, r.getSocketTimeout().intValue());
                    assertEquals(10, r.getConnectionTimeout().intValue());
                    assertEquals("POST", r.getMeth());
                    assertEquals("http://test-server:8080/path/ddd", r.getUri().toString());
                    assertEquals(1, r.getQueryParams().size());
                    assertEquals(3, r.getFormParams().size());
                    assertEquals("aVal", r.getFormParams().get("aa"));
                    assertEquals("cVal", r.getFormParams().get("c"));
                    assertEquals("b b1,bb2", r.getFormParams().get("bb"));

                    assertEquals("cc", r.getQueryParams().get("c"));
                }

                return true;
            }

        }))).thenAnswer(new Answer<Object>() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new HttpResponse(null, 200, null, (new HttpResource() {
                    final InputStream stream = new ByteArrayInputStream(MODEL_RESPONSE_JSON.getBytes());
                    public InputStream getContent() throws HttpException {
                        return stream;
                    }

                    public void release() throws HttpException {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            throw new HttpException(e);
                        }
                    }
                }));
            }
        });

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Rest.class)
                .setEndPoint("http://test-server:8080")
                .setPath("/path")
                .setMethodsSocketTimeout(15l)
                .setMethodsConnectionTimeout(10l)
                .setEncoding("utf-8")
                .setMethodsDeserializer(mockDeserializer)
                .startMethodConfig(Rest.AAA).setPath("/aaa")
                .startParamConfig(0).setName("a").forQuery().endParamConfig()
                .startParamConfig(1).setName("b").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Rest.BBB).setPath("/bbb/{p}")
                .setConnectionTimeout(55l)
                .startParamConfig(0).setName("a").setSerializer(new Ser()).endParamConfig()
                .startParamConfig(1).setName("b").endParamConfig()
                .startParamConfig(2).forPath().setName("p").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Rest.CCC).setPath("/ccc/{p}")
                .setHttpMethod("POST")
                .startParamConfig(0).forPath().setName("p").endParamConfig()
                .startParamConfig(1).forQuery().setName("aa").endParamConfig()
                .startParamConfig(2).forForm().setName("bb").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Rest.DDD).setPath("/ddd")
                .setHttpMethod("POST")
                .startParamConfig(0).forForm().setName("name").setInjector(new AnnotatedBeanParamInjector()).endParamConfig()
                .startParamConfig(1).forForm().setName("bb").endParamConfig()
                .startParamConfig(2).forQuery().setName("c").endParamConfig()
                .endMethodConfig()
                .build();
        CRest factory = new DefaultCRest(new DefaultCRestContext(
                mockRestService,
                new JdkProxyFactory(),
                new PreconfiguredInterfaceConfigFactory(CONFIG),
                null
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


    }

    public static class Ser implements Serializer {

        public String serialize(Object value) {
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
    static class AnnotatedBeanParamInjector implements Injector {


        public void inject(HttpRequest.Builder builder, ParamContext context) {
            if (context.getRawValue() == null) return;

            Map map = new HashMap();
            Field[] fields = context.getRawValue().getClass().getDeclaredFields();

            for (final Field f : fields) {
                AccessController.doPrivileged(new SetAccessible(f));
                RestSerialize annot = f.getAnnotation(RestSerialize.class);
                if (annot != null && annot.exclude()) continue;

                String name = annot == null || Strings.isBlank(annot.name()) ? f.getName() : annot.name();

                Object val = null;
                try {
                    val = f.get(context.getRawValue());
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
                builder.addFormParams(map);
            }

        }

        private static class SetAccessible implements PrivilegedAction {
            private final Field field;

            private SetAccessible(Field field) {
                this.field = field;
            }


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
