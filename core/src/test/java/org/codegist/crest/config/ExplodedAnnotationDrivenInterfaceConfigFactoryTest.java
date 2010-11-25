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

package org.codegist.crest.config;

import org.codegist.crest.CRestContext;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.annotate.RestInjector;
import org.codegist.crest.annotate.exploded.*;
import org.codegist.crest.annotate.exploded.Destination;
import org.codegist.crest.injector.DefaultRequestInjector;
import static org.codegist.crest.HttpMethod.*;
import static org.codegist.crest.config.Destination.*;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ExplodedAnnotationDrivenInterfaceConfigFactoryTest extends AbstractInterfaceConfigFactoryTest {

    private final InterfaceConfigFactory configFactory = new ExplodedAnnotationDrivenInterfaceConfigFactory();
    private final CRestContext mockContext = mock(CRestContext.class);

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConfig() throws ConfigFactoryException {
        configFactory.newConfig(String.class, mockContext);
    }

    @Test
    public void testMinimalConfig() throws ConfigFactoryException {
        assertMinimalExpected(configFactory.newConfig(MinimallyAnnotatedInterface.class, mockContext), MinimallyAnnotatedInterface.class);
    }

    @Test
    public void testPartialConfig() throws ConfigFactoryException {
        assertPartialExpected(configFactory.newConfig(PartiallyAnnotatedInterface.class, mockContext), PartiallyAnnotatedInterface.class);
    }

    @Test
    public void testFullConfig() throws ConfigFactoryException {
        assertFullExpected(configFactory.newConfig(FullyAnnotatedInterface.class, mockContext), FullyAnnotatedInterface.class);
    }

    @Test
    public void realUseCaseTest() throws ConfigFactoryException {
        InterfaceConfig cfg = configFactory.newConfig(Rest.class, mockContext);
        InterfaceConfigTestHelper.assertExpected(cfg, Rest.CONFIG, Rest.class);
        InterfaceConfigTestHelper.assertExpected(Rest.CONFIG, cfg, Rest.class);
    }

    @Test
    public void testInterfaceOverridesTypeInjector() throws ConfigFactoryException {
        InterfaceConfig cfg = configFactory.newConfig(RestInjectorOverrideInterface.class, mockContext);
        assertEquals(Stubs.RequestParameterInjector2.class, cfg.getMethodConfig(RestInjectorOverrideInterface.M).getParamConfig(0).getInjector().getClass());
    }

    @Test
    public void testTypeInjectorIsRead() throws ConfigFactoryException {
        InterfaceConfig cfg = configFactory.newConfig(TypeInjectorInterface.class, mockContext);
        assertEquals(Stubs.RequestParameterInjector1.class, cfg.getMethodConfig(TypeInjectorInterface.M).getParamConfig(0).getInjector().getClass());
        assertEquals(DefaultRequestInjector.class, cfg.getMethodConfig(TypeInjectorInterface.M).getParamConfig(1).getInjector().getClass());
    }

    @RestInjector(Stubs.RequestParameterInjector1.class)
    static class Model {

    }

    @EndPoint("http://dd")
    static interface RestInjectorOverrideInterface {
        void get(@Injector(Stubs.RequestParameterInjector2.class) Model m);

        Method M = TestUtils.getMethod(RestInjectorOverrideInterface.class, "get", Model.class);
    }

    @EndPoint("http://dd")
    static interface TypeInjectorInterface {
        void get(Model m, Model[] ms);

        Method M = TestUtils.getMethod(TypeInjectorInterface.class, "get", Model.class, Model[].class);
    }

    @EndPoint("http://localhost:8080")
    @ContextPath("/my-path")
    interface MinimallyAnnotatedInterface extends Interface {
        @Path("/m1")
        Object m1();

        Object m1(String a);

        @Path("/m1")
        Object m1(String a, int b);

        Object m1(String a, int[] b);

        @Path("/m2/1")
        void m2();

        void m2(float f, String... a);
    }

    @EndPoint("http://localhost:8080")
    @ContextPath("/my-path")
    @Serializer(Stubs.Serializer1.class)
    @Injector(Stubs.RequestParameterInjector1.class)
    interface PartiallyAnnotatedInterface extends Interface {

        @Path("/m1")
        @ResponseHandler(Stubs.ResponseHandler1.class)
        Object m1();

        @Path("/m1")
        @HttpMethod(POST)
        @Serializer(Stubs.Serializer2.class)
        Object m1(@Serializer(Stubs.Serializer3.class) @Injector(Stubs.RequestParameterInjector3.class) String a);

        @Path("/m1") @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(String a, @Name("c") int b);

        @Path("/m1") @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(String a, @Name("c") int[] b);


        @Path("/m2/1")
        @HttpMethod(GET)
        @SocketTimeout(11)
        @ConnectionTimeout(12)
        void m2();

        void m2(float f, String... a);
    }

    @EndPoint("http://localhost:8080")
    @ContextPath("/my-path")
    @SocketTimeout( 1)
    @ConnectionTimeout( 2)
    @Encoding( "utf-8")
    @Path( "/hello")
    @HttpMethod( DELETE)
    @GlobalInterceptor(Stubs.RequestInterceptor1.class)
    @RequestInterceptor( Stubs.RequestInterceptor1.class)
    @ResponseHandler( Stubs.ResponseHandler1.class)
    @Serializer( Stubs.Serializer1.class)
    @Name( "name")
    @Destination(BODY)
    @Injector( Stubs.RequestParameterInjector1.class)
    @ErrorHandler( Stubs.ErrorHandler1.class)
    interface FullyAnnotatedInterface extends Interface {


        @Path("/m1")
        @HttpMethod(PUT)
        @SocketTimeout(3)
        @ConnectionTimeout(4)
        @Name("name1")
        @Destination(URL)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer3.class)
        @Injector ( Stubs.RequestParameterInjector2.class)
        @ErrorHandler ( Stubs.ErrorHandler2.class)
        Object m1();

        @Path("/m1")
        @HttpMethod(POST)
        @SocketTimeout(5)
        @ConnectionTimeout(6)
        @Name("name1")
        @Destination(URL)
        @RequestInterceptor(Stubs.RequestInterceptor2.class)
        @ResponseHandler(Stubs.ResponseHandler2.class)
        @Serializer(Stubs.Serializer2.class)
        @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(
                @Name("a")
                @Destination(URL)
                @Serializer(Stubs.Serializer3.class)
                @Injector(Stubs.RequestParameterInjector3.class)
                String a
        );

        @Path("/m1")
        @HttpMethod(DELETE)
        @SocketTimeout(7)
        @ConnectionTimeout(8)
        @Name("name2")
        @Destination(URL)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer3.class)
        Object m1(
                @Name("b")
                @Destination(BODY)
                @Serializer(Stubs.Serializer1.class)
                @Injector(Stubs.RequestParameterInjector3.class)
                String a,
                @Name("c")
                @Destination(URL)
                @Serializer(Stubs.Serializer2.class)
                int b);

        @Path("/m1")
        @HttpMethod(HEAD)
        @SocketTimeout(9)
        @ConnectionTimeout(10)
        @Name("name2")
        @Destination(URL)
        @RequestInterceptor(Stubs.RequestInterceptor1.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer1.class)
        Object m1(
                @Name("d")
                @Destination(URL)
                @Serializer(Stubs.Serializer1.class)
                String a,
                @Name("e")
                @Destination(BODY)
                @Serializer(Stubs.Serializer3.class)
                int[] b);


        @Path("/m2/1")
        @HttpMethod(GET)
        @SocketTimeout(11)
        @ConnectionTimeout(12)
        @Name("name2")
        @Destination(URL)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer1.class)
        void m2();

        @Path("/m2/2")
        @HttpMethod(POST)
        @SocketTimeout(13)
        @ConnectionTimeout(14)
        @Name("name2")
        @Destination(URL)
        @RequestInterceptor(Stubs.RequestInterceptor2.class)
        @ResponseHandler(Stubs.ResponseHandler2.class)
        @Serializer(Stubs.Serializer2.class)
        void m2(
                @Name("f")
                @Destination(URL)
                @Serializer(Stubs.Serializer3.class)
                float f,
                @Name("g")
                @Destination(URL)
                @Serializer(Stubs.Serializer1.class)
                String... a);
    }


    @EndPoint("http://test-server:8080")
    @ContextPath("/path")
    @SocketTimeout(15)
    @ConnectionTimeout(10)
    @Encoding("utf-8")
    public static interface Rest {

        @Path("/aaa?b={1}&a={0}")
        void aaa(int a, String[] b);

        Method AAA = TestUtils.getMethod(Rest.class, "aaa", int.class, String[].class);

        @Path("/bbb/{2}?b={1}&a={0}")
        @ConnectionTimeout(55)
        void bbb(@Serializer(Stubs.Serializer2.class) int a, String[] b, String c);

        Method BBB = TestUtils.getMethod(Rest.class, "bbb", int.class, String[].class, String.class);

        @Path("/ccc/{0}?aa={1}")
        @HttpMethod(POST)
        void ccc(
                @Destination(URL) int a,
                @Destination(URL) int d,
                @Destination(BODY) @Name("bb") String[] b);

        Method CCC = TestUtils.getMethod(Rest.class, "ccc", int.class, int.class, String[].class);

        @Path("/ddd?c={2}")
        @HttpMethod(POST)
        Object ddd(
                @Destination(BODY) Object a,
                @Destination(BODY) @Name("bb") String[] b,
                String c);

        Method DDD = TestUtils.getMethod(Rest.class, "ddd", Object.class, String[].class, String.class);

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Rest.class, "http://test-server:8080")
                .setPath("/path")
                .setMethodsSocketTimeout(15l)
                .setMethodsConnectionTimeout(10l)
                .setEncoding("utf-8")
                .startMethodConfig(AAA).setPath("/aaa?b={1}&a={0}").endMethodConfig()
                .startMethodConfig(BBB).setPath("/bbb/{2}?b={1}&a={0}")
                .setConnectionTimeout(55l)
                .startParamConfig(0).setSerializer(new Stubs.Serializer2()).endParamConfig()
                .endMethodConfig()
                .startMethodConfig(CCC).setPath("/ccc/{0}?aa={1}")
                .setHttpMethod(POST)
                .startParamConfig(0).setDestination(URL).endParamConfig()
                .startParamConfig(1).setDestination(URL).endParamConfig()
                .startParamConfig(2).setDestination(BODY).setName("bb").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(DDD).setPath("/ddd?c={2}")
                .setHttpMethod(POST)
                .startParamConfig(0).setDestination(BODY)/*.setAssembler(new SimpleAnnotatedBeanAssembler())*/.endParamConfig()
                .startParamConfig(1).setDestination(BODY).setName("bb").endParamConfig()
                .endMethodConfig()
                .build();
    }
}
