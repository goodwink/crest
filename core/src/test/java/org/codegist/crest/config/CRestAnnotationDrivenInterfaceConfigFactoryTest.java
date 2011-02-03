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

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CRestAnnotationDrivenInterfaceConfigFactoryTest extends AbstractInterfaceConfigFactoryTest {

    private final InterfaceConfigFactory configFactory = new CRestAnnotationDrivenInterfaceConfigFactory();
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
                @Serializer(Stubs.Serializer2.class)
                @FormParam(value ="d") Model m);

        void get2(@FormParam(value ="d") Model m);

        Method M = TestUtils.getMethod(RestInjectorOverrideInterface.class, "get", Model.class);
        Method M2 = TestUtils.getMethod(RestInjectorOverrideInterface.class, "get2", Model.class);
    }

    @EndPoint("http://dd")
    static interface TypeInjectorInterface {
        void get(@FormParam(value ="d") Model m, @FormParam(value ="ds") Model[] ms);

        Method M = TestUtils.getMethod(TypeInjectorInterface.class, "get", Model.class, Model[].class);
    }

    @EndPoint("http://localhost:8080")
    @ContextPath("/my-path")
    interface MinimallyAnnotatedInterface extends Interface {
        @Path("/m1")
        Object m1();

        Object m1(@QueryParam(value ="param") String a);

        @Path("/m1")
        Object m1(@PathParam("param1") String a, @QueryParam(value ="param2") int b);

        Object m1(@QueryParam(value ="param1") String a, @QueryParam(value ="param2") int[] b);

        @Path("/m2/1")
        void m2();

        void m2(@QueryParam(value ="param1") float f, @QueryParam(value ="param2") String... a);
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
        @POST
        @Serializer(Stubs.Serializer2.class)
        Object m1(@Serializer(Stubs.Serializer3.class) @Injector(Stubs.RequestParameterInjector3.class) @QueryParam(value ="pname") String a);

        @Path("/m1") @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(@FormParam(value ="d") String a, @QueryParam(value ="c", defaultValue = "444") int b);

        @Path("/m1") @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(@PathParam("f") String a, @QueryParam(value ="c") int[] b);


        @Path("/m2/1")
        @GET
        @SocketTimeout(11)
        @ConnectionTimeout(12)
        void m2();

        void m2(@QueryParam(value ="fd")float f, @QueryParam(value ="cf")String... a);
    }

    @EndPoint("http://localhost:8080")
    @ContextPath("/my-path")
    @SocketTimeout( 1)
    @ConnectionTimeout( 2)
    @Encoding( "utf-8")
    @Path( "/hello")
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
    @DELETE
    @GlobalInterceptor(Stubs.RequestInterceptor1.class)
    @RequestInterceptor( Stubs.RequestInterceptor1.class)
    @ResponseHandler( Stubs.ResponseHandler1.class)
    @Serializer( Stubs.Serializer1.class)
    @Injector( Stubs.RequestParameterInjector1.class)
    @ErrorHandler( Stubs.ErrorHandler1.class)
    @RetryHandler(Stubs.RetryHandler1.class)
    interface FullyAnnotatedInterface extends Interface {


        @Path("/m1")
        @FormParams({
            @FormParam(value ="form-param", defaultValue ="over-value1"),
            @FormParam(value ="form-param3", defaultValue ="new-value")
        })
        @PUT
        @SocketTimeout(3)
        @ConnectionTimeout(4)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer3.class)
        @Injector ( Stubs.RequestParameterInjector2.class)
        @ErrorHandler ( Stubs.ErrorHandler2.class)
        @RetryHandler(Stubs.RetryHandler2.class)
        Object m1();

        @Path("/m1")
        @PathParam(value="form-param",defaultValue= "over-value1")
        @POST
        @SocketTimeout(5)
        @ConnectionTimeout(6)
        @RequestInterceptor(Stubs.RequestInterceptor2.class)
        @ResponseHandler(Stubs.ResponseHandler2.class)
        @Serializer(Stubs.Serializer2.class)
        @Injector(Stubs.RequestParameterInjector2.class)
        Object m1(
                @HeaderParam(value ="a", defaultValue = "deff")
                @Serializer(Stubs.Serializer3.class)
                @Injector(Stubs.RequestParameterInjector3.class)
                String a
        );

        @Path("/m1")
        @DELETE
        @SocketTimeout(7)
        @ConnectionTimeout(8)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer3.class)
        Object m1(
                @FormParam("b")
                @Serializer(Stubs.Serializer1.class)
                @Injector(Stubs.RequestParameterInjector3.class)
                String a,
                @QueryParam("c")
                @Serializer(Stubs.Serializer2.class)
                int b);

        @Path("/m1")
        @HEAD
        @SocketTimeout(9)
        @ConnectionTimeout(10)
        @RequestInterceptor(Stubs.RequestInterceptor1.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer1.class)
        Object m1(
                @PathParam("d")
                @Serializer(Stubs.Serializer1.class)
                String a,
                @FormParam("e")
                @Serializer(Stubs.Serializer3.class)
                int[] b);


        @Path("/m2/1")
        @GET
        @SocketTimeout(11)
        @ConnectionTimeout(12)
        @RequestInterceptor(Stubs.RequestInterceptor3.class)
        @ResponseHandler(Stubs.ResponseHandler1.class)
        @Serializer(Stubs.Serializer1.class)
        void m2();

        @Path("/m2/2")
        @POST
        @SocketTimeout(13)
        @ConnectionTimeout(14)
        @RequestInterceptor(Stubs.RequestInterceptor2.class)
        @ResponseHandler(Stubs.ResponseHandler2.class)
        @Serializer(Stubs.Serializer2.class)
        void m2(
                @PathParam("f")
                @Serializer(Stubs.Serializer3.class)
                float f,
                @PathParam("g")
                @Serializer(Stubs.Serializer1.class)
                String... a);
    }


    @EndPoint("http://test-server:8080")
    @ContextPath("/path")
    @SocketTimeout(15)
    @ConnectionTimeout(10)
    @Encoding("utf-8")
    public static interface Rest {

        @Path("/aaa")
        void aaa(@QueryParam("a") int a, @QueryParam("b") String[] b);

        Method AAA = TestUtils.getMethod(Rest.class, "aaa", int.class, String[].class);

        @Path("/bbb/{pa}")
        @ConnectionTimeout(55)
        void bbb(@Serializer(Stubs.Serializer2.class) @PathParam("pa")int a, @QueryParam("a")String[] b, @QueryParam("b")String c);

        Method BBB = TestUtils.getMethod(Rest.class, "bbb", int.class, String[].class, String.class);

        @Path("/ccc/{a}")
        @POST
        void ccc(
                @PathParam("a") int a,
                @QueryParam("aa") int d,
                @FormParam("bb") String[] b);

        Method CCC = TestUtils.getMethod(Rest.class, "ccc", int.class, int.class, String[].class);

        @Path("/ddd")
        @POST
        Object ddd(
                @FormParam("dd") Object a,
                @FormParam("bb") String[] b,
                @QueryParam("c") String c);

        Method DDD = TestUtils.getMethod(Rest.class, "ddd", Object.class, String[].class, String.class);

        InterfaceConfig CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Rest.class)
                .setEndPoint("http://test-server:8080")
                .setContextPath("/path")
                .setMethodsSocketTimeout(15l)
                .setMethodsConnectionTimeout(10l)
                .setEncoding("utf-8")
                .startMethodConfig(AAA).setPath("/aaa")
                .startParamConfig(0).setName("a").endParamConfig()
                .startParamConfig(1).setName("b").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(BBB).setPath("/bbb/{pa}")
                .setConnectionTimeout(55l)
                .startParamConfig(0).forPath().setName("pa").setSerializer(new Stubs.Serializer2()).endParamConfig()
                .startParamConfig(1).setName("a").endParamConfig()
                .startParamConfig(2).setName("b").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(CCC).setPath("/ccc/{a}")
                .setHttpMethod("POST")
                .startParamConfig(0).forPath().setName("a").endParamConfig()
                .startParamConfig(1).forQuery().setName("aa").endParamConfig()
                .startParamConfig(2).forForm().setName("bb").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(DDD).setPath("/ddd")
                .setHttpMethod("POST")
                .startParamConfig(0).forForm().setName("dd").endParamConfig()
                .startParamConfig(1).forForm().setName("bb").endParamConfig()
                .startParamConfig(2).forQuery().setName("c").endParamConfig()
                .endMethodConfig()
                .build();
    }
}
