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
import org.codegist.crest.HttpMethod;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestInjector;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.annotate.RestParam;
import org.codegist.crest.injector.DefaultRequestInjector;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class AnnotationDrivenInterfaceConfigFactoryTest extends AbstractInterfaceConfigFactoryTest {

    private final InterfaceConfigFactory configFactory = new AnnotationDrivenInterfaceConfigFactory();
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

    @RestApi(endPoint = "http://dd")
    static interface RestInjectorOverrideInterface {
        void get(@RestParam(injector = Stubs.RequestParameterInjector2.class) Model m);

        Method M = TestUtils.getMethod(RestInjectorOverrideInterface.class, "get", Model.class);
    }

    @RestApi(endPoint = "http://dd")
    static interface TypeInjectorInterface {
        void get(Model m, Model[] ms);

        Method M = TestUtils.getMethod(TypeInjectorInterface.class, "get", Model.class, Model[].class);
    }

    @RestApi(endPoint = "http://localhost:8080", path = "/my-path")
    interface MinimallyAnnotatedInterface extends Interface {
        @Override
        @RestMethod(path = "/m1")
        Object m1();

        @Override
        Object m1(String a);

        @Override
        @RestMethod(path = "/m1")
        Object m1(String a, int b);

        @Override
        @RestMethod()
        Object m1(String a, int[] b);

        @Override
        @RestMethod(path = "/m2/1")
        void m2();

        @Override
        void m2(float f, String... a);
    }

    @RestApi(
            endPoint = "http://localhost:8080",
            path = "/my-path",
            paramsSerializer = Stubs.Serializer1.class,
            paramsInjector = Stubs.RequestParameterInjector1.class
    )
    interface PartiallyAnnotatedInterface extends Interface {

        @Override
        @RestMethod(
                path = "/m1",
                responseHandler = Stubs.ResponseHandler1.class
        )
        Object m1();

        @Override
        @RestMethod(
                path = "/m1",
                method = "POST",
                paramsSerializer = Stubs.Serializer2.class
        )
        Object m1(@RestParam(serializer = Stubs.Serializer3.class, injector = Stubs.RequestParameterInjector3.class) String a);

        @Override
        @RestMethod(path = "/m1", paramsInjector = Stubs.RequestParameterInjector2.class)
        Object m1(String a, @RestParam(name = "c") int b);

        @Override
        @RestMethod(path = "/m1", paramsInjector = Stubs.RequestParameterInjector2.class)
        Object m1(String a, @RestParam(name = "c") int[] b);


        @Override
        @RestMethod(
                path = "/m2/1",
                method = "GET",
                socketTimeout = 11,
                connectionTimeout = 12)
        void m2();

        @Override
        void m2(float f, String... a);
    }

    @RestApi(
            endPoint = "http://localhost:8080",
            path = "/my-path",
            methodsSocketTimeout = 1,
            methodsConnectionTimeout = 2,
            encoding = "utf-8",
            methodsPath = "/hello",
            methodsHttpMethod = HttpMethod.DELETE,
            requestInterceptor = Stubs.RequestInterceptor1.class,
            methodsRequestInterceptor = Stubs.RequestInterceptor1.class,
            methodsResponseHandler = Stubs.ResponseHandler1.class,
            paramsSerializer = Stubs.Serializer1.class,
            paramsName = "name",
            paramsDestination = Destination.BODY,
            paramsInjector = Stubs.RequestParameterInjector1.class,
            methodsErrorHandler = Stubs.ErrorHandler1.class
    )
    interface FullyAnnotatedInterface extends Interface {


        @RestMethod(
                path = "/m1",
                method = "PUT",
                socketTimeout = 3,
                connectionTimeout = 4,
                paramsName = "name1",
                paramsDestination = "URL",
                requestInterceptor = Stubs.RequestInterceptor3.class,
                responseHandler = Stubs.ResponseHandler1.class,
                paramsSerializer = Stubs.Serializer3.class,
                paramsInjector = Stubs.RequestParameterInjector2.class,
                errorHandler = Stubs.ErrorHandler2.class
        )
        @Override
        Object m1();

        @RestMethod(
                path = "/m1",
                method = "POST",
                socketTimeout = 5,
                connectionTimeout = 6,
                paramsName = "name1",
                paramsDestination = "URL",
                requestInterceptor = Stubs.RequestInterceptor2.class,
                responseHandler = Stubs.ResponseHandler2.class,
                paramsSerializer = Stubs.Serializer2.class,
                paramsInjector = Stubs.RequestParameterInjector2.class
        )
        @Override
        Object m1(
                @RestParam(
                        name = "a",
                        destination = "URL",
                        serializer = Stubs.Serializer3.class,
                        injector = Stubs.RequestParameterInjector3.class
                ) String a
        );

        @RestMethod(
                path = "/m1",
                method = "DELETE",
                socketTimeout = 7,
                connectionTimeout = 8,
                paramsName = "name2",
                paramsDestination = "URL",
                requestInterceptor = Stubs.RequestInterceptor3.class,
                responseHandler = Stubs.ResponseHandler1.class,
                paramsSerializer = Stubs.Serializer3.class
        )
        @Override
        Object m1(
                @RestParam(
                        name = "b",
                        destination = "BODY",
                        serializer = Stubs.Serializer1.class,
                        injector = Stubs.RequestParameterInjector3.class
                ) String a,
                @RestParam(
                        name = "c",
                        destination = "URL",
                        serializer = Stubs.Serializer2.class
                ) int b);

        @RestMethod(
                path = "/m1",
                method = "HEAD",
                socketTimeout = 9,
                connectionTimeout = 10,
                paramsName = "name2",
                paramsDestination = "URL",
                requestInterceptor = Stubs.RequestInterceptor1.class,
                responseHandler = Stubs.ResponseHandler1.class,
                paramsSerializer = Stubs.Serializer1.class
        )
        @Override
        Object m1(
                @RestParam(
                        name = "d",
                        destination = "URL",
                        serializer = Stubs.Serializer1.class
                ) String a,
                @RestParam(
                        name = "e",
                        destination = "BODY",
                        serializer = Stubs.Serializer3.class
                ) int[] b);


        @RestMethod(
                path = "/m2/1",
                method = "GET",
                socketTimeout = 11,
                connectionTimeout = 12,
                paramsName = "name2",
                paramsDestination = "URL",
                requestInterceptor = Stubs.RequestInterceptor3.class,
                responseHandler = Stubs.ResponseHandler1.class,
                paramsSerializer = Stubs.Serializer1.class
        )
        @Override
        void m2();

        @RestMethod(
                path = "/m2/2",
                method = "POST",
                socketTimeout = 13,
                connectionTimeout = 14,
                paramsName = "name2",
                paramsDestination = "URL",
                requestInterceptor = Stubs.RequestInterceptor2.class,
                responseHandler = Stubs.ResponseHandler2.class,
                paramsSerializer = Stubs.Serializer2.class
        )
        @Override
        void m2(
                @RestParam(
                        name = "f",
                        destination = "URL",
                        serializer = Stubs.Serializer3.class
                ) float f,
                @RestParam(
                        name = "g",
                        destination = "URL",
                        serializer = Stubs.Serializer1.class
                ) String... a);
    }


    @RestApi(
            endPoint = "http://test-server:8080",
            path = "/path",
            methodsSocketTimeout = 15,
            methodsConnectionTimeout = 10,
            encoding = "utf-8"
    )
    public static interface Rest {

        @RestMethod(path = "/aaa?b={1}&a={0}")
        void aaa(int a, String[] b);

        Method AAA = TestUtils.getMethod(Rest.class, "aaa", int.class, String[].class);

        @RestMethod(path = "/bbb/{2}?b={1}&a={0}", connectionTimeout = 55)
        void bbb(@RestParam(serializer = Stubs.Serializer2.class) int a, String[] b, @RestParam String c);

        Method BBB = TestUtils.getMethod(Rest.class, "bbb", int.class, String[].class, String.class);

        @RestMethod(path = "/ccc/{0}?aa={1}", method = "POST")
        void ccc(
                @RestParam(destination = "URL") int a,
                @RestParam(destination = "URL") int d,
                @RestParam(destination = "BODY", name = "bb") String[] b);

        Method CCC = TestUtils.getMethod(Rest.class, "ccc", int.class, int.class, String[].class);

        @RestMethod(path = "/ddd?c={2}", method = "POST")
        Object ddd(
                @RestParam(destination = "BODY"/*, requestInterceptor = SimpleAnnotatedBeanAssembler.class*/) Object a,
                @RestParam(destination = "BODY", name = "bb") String[] b,
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
                .setHttpMethod(HttpMethod.POST)
                .startParamConfig(0).setDestination("URL").endParamConfig()
                .startParamConfig(1).setDestination("URL").endParamConfig()
                .startParamConfig(2).setDestination("BODY").setName("bb").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(DDD).setPath("/ddd?c={2}")
                .setHttpMethod(HttpMethod.POST)
                .startParamConfig(0).setDestination("BODY")/*.setAssembler(new SimpleAnnotatedBeanAssembler())*/.endParamConfig()
                .startParamConfig(1).setDestination("BODY").setName("bb").endParamConfig()
                .endMethodConfig()
                .build();
    }
}
