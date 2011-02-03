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

package org.codegist.crest.config;

import org.codegist.crest.CRestContext;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.annotate.*;
import org.codegist.crest.injector.DefaultInjector;
import org.junit.Test;

import javax.ws.rs.DefaultValue;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class JaxRSAwareAnnotationDrivenInterfaceConfigFactoryTest extends AbstractInterfaceConfigFactoryTest {

    private final InterfaceConfigFactory configFactory = new JaxRSAwareAnnotationDrivenInterfaceConfigFactory();
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
        assertEquals(Stubs.Serializer2.class, cfg.getMethodConfig(RestInjectorOverrideInterface.M).getParamConfig(0).getSerializer().getClass());
        assertEquals(Stubs.RequestParameterInjector2.class, cfg.getMethodConfig(RestInjectorOverrideInterface.M).getParamConfig(0).getInjector().getClass());

        assertEquals(Stubs.Serializer3.class, cfg.getMethodConfig(RestInjectorOverrideInterface.M2).getParamConfig(0).getSerializer().getClass());
        assertEquals(Stubs.RequestParameterInjector1.class, cfg.getMethodConfig(RestInjectorOverrideInterface.M2).getParamConfig(0).getInjector().getClass());
    }

    @Test
    public void testTypeInjectorIsRead() throws ConfigFactoryException {
        InterfaceConfig cfg = configFactory.newConfig(TypeInjectorInterface.class, mockContext);
        assertEquals(Stubs.RequestParameterInjector1.class, cfg.getMethodConfig(TypeInjectorInterface.M).getParamConfig(0).getInjector().getClass());
        assertEquals(DefaultInjector.class, cfg.getMethodConfig(TypeInjectorInterface.M).getParamConfig(1).getInjector().getClass());
    }

    @Injector(Stubs.RequestParameterInjector1.class)
    @Serializer(Stubs.Serializer3.class)
    static class Model {

    }

    @EndPoint("http://dd")
    static interface RestInjectorOverrideInterface {
        void get(
                @Injector(Stubs.RequestParameterInjector2.class)
                @Serializer(Stubs.Serializer2.class) Model m);

        void get2(Model m);

        Method M = TestUtils.getMethod(RestInjectorOverrideInterface.class, "get", Model.class);
        Method M2 = TestUtils.getMethod(RestInjectorOverrideInterface.class, "get2", Model.class);
    }

    @EndPoint("http://dd")
    static interface TypeInjectorInterface {
        void get(Model m, Model[] ms);

        Method M = TestUtils.getMethod(TypeInjectorInterface.class, "get", Model.class, Model[].class);
    }


    @EndPoint("http://localhost:8080")
    @javax.ws.rs.Path("/my-path")
    interface MinimallyAnnotatedInterface extends Interface {
        @javax.ws.rs.Path("/m1")
        Object m1();

        Object m1(@javax.ws.rs.QueryParam("param") String a);

        @javax.ws.rs.Path("/m1")
        Object m1(@javax.ws.rs.PathParam("param1") String a, @javax.ws.rs.QueryParam("param2") int b);

        Object m1(@javax.ws.rs.QueryParam("param1") String a, @javax.ws.rs.QueryParam("param2") int[] b);

        @javax.ws.rs.Path("/m2/1")
        void m2();

        void m2(@javax.ws.rs.QueryParam("param1") float f, @javax.ws.rs.QueryParam("param2") String... a);
    }

    @EndPoint("http://localhost:8080")
    @Serializer(Stubs.Serializer1.class)
    @Injector(Stubs.RequestParameterInjector1.class)
    @javax.ws.rs.Path("/my-path")
    interface PartiallyAnnotatedInterface extends Interface {

        @javax.ws.rs.Path("/m1")
        @ResponseHandler(Stubs.ResponseHandler1.class)
        Object m1();

        @javax.ws.rs.Path("/m1")
        @javax.ws.rs.POST
        @Serializer(Stubs.Serializer2.class)
        Object m1(@Serializer(Stubs.Serializer3.class) @Injector(Stubs.RequestParameterInjector3.class) @javax.ws.rs.QueryParam("pname") String a);

        @javax.ws.rs.Path("/m1") @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(@javax.ws.rs.FormParam("d") String a, @javax.ws.rs.QueryParam("c") @DefaultValue("444") int b);

        @javax.ws.rs.Path("/m1") @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(@javax.ws.rs.PathParam("f") String a, @javax.ws.rs.QueryParam("c") int[] b);


        @javax.ws.rs.Path("/m2/1")
        @javax.ws.rs.GET
        @SocketTimeout(11)
        @ConnectionTimeout(12)
        void m2();

        void m2(@javax.ws.rs.QueryParam("fd") float f, @javax.ws.rs.QueryParam("cf") String... a);
    }

    @javax.ws.rs.Path("/my-path")
    @EndPoint("http://localhost:8080")
    @SocketTimeout( 1)
    @ConnectionTimeout( 2)
    @Encoding( "utf-8")

    @FormParam(value ="form-param", defaultValue ="form-value")
    @FormParams({
            @FormParam(value ="form-param1", defaultValue ="form-value1"),
            @FormParam(value ="form-param2", defaultValue ="form-value2")
    })
    @PathParam(value="path-param",defaultValue="path-value")
    @PathParams({
            @PathParam(value="path-param1",defaultValue="path-value1"),
            @PathParam(value="path-param2",defaultValue="path-value2")
    })
    @QueryParam(value ="query-param", defaultValue ="query-value")
    @QueryParams({
            @QueryParam(value ="query-param1", defaultValue ="query-value1"),
            @QueryParam(value ="query-param2", defaultValue ="query-value2")
    })
    @HeaderParam(value ="header-param", defaultValue ="header-value")
    @HeaderParams({
            @HeaderParam(value ="header-param1", defaultValue ="header-value1"),
            @HeaderParam(value ="header-param2", defaultValue ="header-value2")
    })

    @GlobalInterceptor(Stubs.RequestInterceptor1.class)
    @RequestInterceptor( Stubs.RequestInterceptor1.class)
    @ResponseHandler( Stubs.ResponseHandler1.class)
    @Serializer( Stubs.Serializer1.class)
    @Injector( Stubs.RequestParameterInjector1.class)
    @ErrorHandler( Stubs.ErrorHandler1.class)
    @RetryHandler(Stubs.RetryHandler1.class)
    interface FullyAnnotatedInterface extends Interface {


        @javax.ws.rs.Path("/m1")
        @javax.ws.rs.PUT
        @FormParams({
            @FormParam(value ="form-param", defaultValue ="over-value1"),
            @FormParam(value ="form-param3", defaultValue ="new-value")
        })
        @SocketTimeout(3)
        @ConnectionTimeout(4)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer3.class)
        @Injector ( Stubs.RequestParameterInjector2.class)
        @ErrorHandler ( Stubs.ErrorHandler2.class)
        @RetryHandler(Stubs.RetryHandler2.class)
        Object m1();

        @javax.ws.rs.Path("/m1")
        @javax.ws.rs.POST
        @PathParam(value="form-param",defaultValue="over-value1")
        @SocketTimeout(5)
        @ConnectionTimeout(6)
        @RequestInterceptor(Stubs.RequestInterceptor2.class)
        @ResponseHandler(Stubs.ResponseHandler2.class)
        @Serializer(Stubs.Serializer2.class)
        @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(
                @javax.ws.rs.HeaderParam("a") @javax.ws.rs.DefaultValue("deff")
                @Serializer(Stubs.Serializer3.class)
                @Injector(Stubs.RequestParameterInjector3.class)
                String a
        );

        @javax.ws.rs.Path("/m1")
        @javax.ws.rs.DELETE
        @SocketTimeout(7)
        @ConnectionTimeout(8)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer3.class)
        Object m1(
                @javax.ws.rs.FormParam("b")
                @Serializer(Stubs.Serializer1.class)
                @Injector(Stubs.RequestParameterInjector3.class)
                String a,
                @javax.ws.rs.QueryParam("c")
                @Serializer(Stubs.Serializer2.class)
                int b);

        @javax.ws.rs.Path("/m1")
        @javax.ws.rs.HEAD
        @SocketTimeout(9)
        @ConnectionTimeout(10)
        @RequestInterceptor(Stubs.RequestInterceptor1.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer1.class)
        Object m1(
                @javax.ws.rs.PathParam("d")
                @Serializer(Stubs.Serializer1.class)
                String a,
                @javax.ws.rs.FormParam("e")
                @Serializer(Stubs.Serializer3.class)
                int[] b);


        @javax.ws.rs.Path("/m2/1")
        @javax.ws.rs.GET
        @SocketTimeout(11)
        @ConnectionTimeout(12)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer1.class)
        void m2();

        @javax.ws.rs.Path("/m2/2")
        @javax.ws.rs.POST
        @SocketTimeout(13)
        @ConnectionTimeout(14)
        @RequestInterceptor(Stubs.RequestInterceptor2.class)
        @ResponseHandler(Stubs.ResponseHandler2.class)
        @Serializer(Stubs.Serializer2.class)
        void m2(
                @javax.ws.rs.PathParam("f")
                @Serializer(Stubs.Serializer3.class)
                float f,
                @javax.ws.rs.PathParam("g")
                @Serializer(Stubs.Serializer1.class)
                String... a);
    }


    @EndPoint("http://test-server:8080")
    @javax.ws.rs.Path("/path")
    @SocketTimeout(15)
    @ConnectionTimeout(10)
    @Encoding("utf-8")
    public static interface Rest {

        @javax.ws.rs.Path("/aaa")
        void aaa(@QueryParam(value ="a") int a, @javax.ws.rs.QueryParam("b") String[] b);

        Method AAA = TestUtils.getMethod(Rest.class, "aaa", int.class, String[].class);

        @javax.ws.rs.Path("/bbb/{p}")
        @ConnectionTimeout(55)
        void bbb(@Serializer(Stubs.Serializer2.class) @javax.ws.rs.QueryParam("a") int a,
                 @javax.ws.rs.PathParam("p") String[] b,
                 @javax.ws.rs.QueryParam("qq") String c);

        Method BBB = TestUtils.getMethod(Rest.class, "bbb", int.class, String[].class, String.class);

        @javax.ws.rs.Path("/ccc/{p}")
        @javax.ws.rs.POST
        void ccc(
                @javax.ws.rs.PathParam("p") int a,
                @javax.ws.rs.QueryParam("aa") int d,
                @javax.ws.rs.FormParam("bb") String[] b);

        Method CCC = TestUtils.getMethod(Rest.class, "ccc", int.class, int.class, String[].class);

        @javax.ws.rs.Path("/ddd")
        @javax.ws.rs.POST
        Object ddd(
                @FormParam(value ="obj") Object a,
                @javax.ws.rs.FormParam("bb") String[] b,
                @javax.ws.rs.FormParam("cb") String c);

        Method DDD = TestUtils.getMethod(Rest.class, "ddd", Object.class, String[].class, String.class);

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Rest.class)
                .setEndPoint("http://test-server:8080")
                .setContextPath("/path")
                .setMethodsSocketTimeout(15l)
                .setMethodsConnectionTimeout(10l)
                .setEncoding("utf-8")
                .startMethodConfig(AAA).setPath("/aaa")
                .startParamConfig(0).forQuery().setName("a").endParamConfig()
                .startParamConfig(1).forQuery().setName("b").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(BBB).setPath("/bbb/{p}")
                .setConnectionTimeout(55l)
                .startParamConfig(0).forQuery().setName("a").setSerializer(new Stubs.Serializer2()).endParamConfig()
                .startParamConfig(1).forPath().setName("p").endParamConfig()
                .startParamConfig(2).forQuery().setName("qq").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(CCC).setPath("/ccc/{p}")
                .setHttpMethod("POST")
                .startParamConfig(0).forPath().setName("p").endParamConfig()
                .startParamConfig(1).forQuery().setName("aa").endParamConfig()
                .startParamConfig(2).forForm().setName("bb").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(DDD).setPath("/ddd")
                .setHttpMethod("POST")
                .startParamConfig(0).forForm().setName("obj").endParamConfig()
                .startParamConfig(1).forForm() .setName("bb").endParamConfig()
                .startParamConfig(2).forForm() .setName("cb").endParamConfig()
                .endMethodConfig()
                .build();
    }
}
