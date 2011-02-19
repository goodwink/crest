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
import org.codegist.crest.serializer.DeserializerFactory;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public abstract class AbstractInterfaceConfigFactoryTest {


    public void assertMinimalExpected(InterfaceConfig test, Class clazz) {
        InterfaceConfigTestHelper.assertExpected(MINIMAL_EXPECTED_CONFIG, test, clazz);
    }

    public void assertPartialExpected(InterfaceConfig test, Class clazz) {
        InterfaceConfigTestHelper.assertExpected(PARTIAL_EXPECTED_CONFIG, test, clazz);
    }

    public void assertFullExpected(InterfaceConfig test, Class clazz) {
        InterfaceConfigTestHelper.assertExpected(FULLY_EXPECTED_CONFIG, test, clazz);
    }

    @Test
    public abstract void testMinimalConfig() throws ConfigFactoryException;

    @Test
    public abstract void testPartialConfig() throws ConfigFactoryException;

    @Test
    public abstract void testFullConfig() throws ConfigFactoryException;

    @Test(expected = RuntimeException.class)
    public abstract void testInvalidConfig() throws Exception;

    @Test(expected = IllegalArgumentException.class)
    public abstract void testConfigMissingParamName() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException;

    @Test(expected = IllegalArgumentException.class)
    public abstract void testConfigMissingEndpoint() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException;

    interface Interface {

        Object m1();

        Object m1(String a);

        Object m1(String a, int b);

        Object m1(String a, int[] b);

        void m2();

        void m2(float f, String... a);

        Method METH_m1 = TestUtils.getMethod(Interface.class, "m1");
        Method METH_m1S = TestUtils.getMethod(Interface.class, "m1", String.class);
        Method METH_m1SI = TestUtils.getMethod(Interface.class, "m1", String.class, int.class);
        Method METH_m1SIs = TestUtils.getMethod(Interface.class, "m1", String.class, int[].class);
        Method METH_m2 = TestUtils.getMethod(Interface.class, "m2");
        Method METH_m2FSs = TestUtils.getMethod(Interface.class, "m2", float.class, String[].class);
    }

    public static final DeserializerFactory DESERIALIZER_FACTORY = new DeserializerFactory.Builder()
                .register(new Stubs.Deserializer1(), "mime1", "mime1bis")
                .register(new Stubs.Deserializer2(), "mime2")
                .register(new Stubs.Deserializer3(), "mime3")
                .build();

    public final CRestContext MOCK_CONTEXT = mock(CRestContext.class);{
        Map<String,Object> mockProperties = mock(Map.class);
        when(mockProperties.get(DeserializerFactory.class.getName())).thenReturn(DESERIALIZER_FACTORY);
        when(MOCK_CONTEXT.getProperties()).thenReturn(mockProperties);
    }

    public static final InterfaceConfig MINIMAL_EXPECTED_CONFIG;

    static {
        try {
            MINIMAL_EXPECTED_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                    .setEndPoint("http://localhost:8080")
                    .setPath("/my-path")
                    .startMethodConfig(Interface.METH_m1).setPath("/m1").endMethodConfig()
                    .startMethodConfig(Interface.METH_m1S)
                    .startParamConfig(0).setName("param").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SI)
                    .setPath("/m1")
                    .startParamConfig(0).forPath().setName("param1").endParamConfig()
                    .startParamConfig(1).setName("param2").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SIs)
                    .startParamConfig(0).setName("param1").endParamConfig()
                    .startParamConfig(1).setName("param2").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2).setPath("/m2/1").endMethodConfig()
                    .startMethodConfig(Interface.METH_m2FSs)
                    .startParamConfig(0).setName("param1").endParamConfig()
                    .startParamConfig(1).setName("param2").endParamConfig()
                    .endMethodConfig()
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static final InterfaceConfig PARTIAL_EXPECTED_CONFIG;

    static {
        try {
            PARTIAL_EXPECTED_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                    .setEndPoint("http://localhost:8080")
                    .setPath("/my-path")
                    .setMethodsDeserializer(new Stubs.Deserializer1())
                    .addMethodsExtraHeaderParam("Accept", "mime1")
                    .setParamsSerializer(new Stubs.Serializer1())
                    .setParamsInjector(new Stubs.RequestParameterInjector1())
                    .startMethodConfig(Interface.METH_m1)
                    .addExtraHeaderParam("Accept", "mime2")
                    .setDeserializer(new Stubs.Deserializer2()).setPath("/m1").setResponseHandler(new Stubs.ResponseHandler1()).endMethodConfig()
                    .startMethodConfig(Interface.METH_m1S)
                    .setPath("/m1").setHttpMethod("POST")
                    .setParamsSerializer(new Stubs.Serializer2())
                    .startParamConfig(0).setName("pname").setInjector(new Stubs.RequestParameterInjector3()).setSerializer(new Stubs.Serializer3()).endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SI)
                    .setPath("/m1")
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .startParamConfig(0).forForm().setName("d").endParamConfig()
                    .startParamConfig(1).setName("c").setDefaultValue("444").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SIs)
                    .setPath("/m1")
                    .setDeserializer(new Stubs.Deserializer2())
                    .addExtraHeaderParam("Accept", "mime2")
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .startParamConfig(0).forPath().setName("f").endParamConfig()
                    .startParamConfig(1).setName("c").endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2).setPath("/m2/1")
                    .setHttpMethod("GET")
                    .setSocketTimeout(11l)
                    .setConnectionTimeout(12l)
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2FSs)
                    .startParamConfig(0).setName("fd").endParamConfig()
                    .startParamConfig(1).setName("cf").endParamConfig()
                    .endMethodConfig()
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    public static final InterfaceConfig FULLY_EXPECTED_CONFIG;

    static {
        try {
            FULLY_EXPECTED_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                    .setEndPoint("http://localhost:8080")
                    .setPath("/my-path")
                    .addMethodsExtraFormParam("form-param", "form-value")
                    .addMethodsExtraFormParam("form-param1", "form-value1")
                    .addMethodsExtraFormParam("form-param2", "form-value2")
                    .addMethodsExtraHeaderParam("header-param", "header-value")
                    .addMethodsExtraHeaderParam("header-param1", "header-value1")
                    .addMethodsExtraHeaderParam("header-param2", "header-value2")
                    .addMethodsExtraQueryParam("query-param", "query-value")
                    .addMethodsExtraQueryParam("query-param1", "query-value1")
                    .addMethodsExtraQueryParam("query-param2", "query-value2")
                    .addMethodsExtraPathParam("path-param", "path-value")
                    .addMethodsExtraPathParam("path-param1", "path-value1")
                    .addMethodsExtraPathParam("path-param2", "path-value2")
                    .addMethodsExtraHeaderParam("Accept", "mime1")
                    .setMethodsSocketTimeout(1l)
                    .setMethodsConnectionTimeout(2l)
                    .setMethodsDeserializer(Stubs.Deserializer1.class)
                    .setEncoding("utf-8")
                    .setGlobalInterceptor(new Stubs.RequestInterceptor1())
                    .setParamsSerializer(new Stubs.Serializer1())
                    .setMethodsPath("/hello")
                    .setMethodsHttpMethod("DELETE")
                    .setMethodsResponseHandler(new Stubs.ResponseHandler1())
                    .setMethodsRetryHandler(new Stubs.RetryHandler1())
                    .setMethodsErrorHandler(new Stubs.ErrorHandler1())
                    .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
                    .setParamsInjector(new Stubs.RequestParameterInjector1())
                    .startMethodConfig(Interface.METH_m1)
                    .addExtraHeaderParam("Accept", "mime2")
                    .setDeserializer(Stubs.Deserializer2.class)
                    .setPath("/m1")
                    .addExtraFormParam("form-param","over-value1")
                    .addExtraFormParam("form-param3","new-value")
                    .setHttpMethod("PUT")
                    .setRetryHandler(new Stubs.RetryHandler2())
                    .setSocketTimeout(3l)
                    .setConnectionTimeout(4l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor3())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setErrorHandler(new Stubs.ErrorHandler2())
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .setParamsSerializer(new Stubs.Serializer3())
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1S)
                    .addExtraHeaderParam("Accept", "mime3")
                    .setDeserializer(Stubs.Deserializer3.class)
                    .addExtraPathParam("form-param","over-value1")
                    .setPath("/m1")
                    .setHttpMethod("POST")
                    .setSocketTimeout(5l)
                    .setConnectionTimeout(6l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor2())
                    .setResponseHandler(new Stubs.ResponseHandler2())
                    .setParamsSerializer(new Stubs.Serializer2())
                    .setParamsInjector(new Stubs.RequestParameterInjector2())
                    .startParamConfig(0).forHeader()
                    .setName("a")
                    .setDefaultValue("deff")
                    .setSerializer(new Stubs.Serializer3())
                    .setInjector(new Stubs.RequestParameterInjector3())
                    .endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SI)
                    .setPath("/m1")
                    .setHttpMethod("DELETE")
                    .setSocketTimeout(7l)
                    .setConnectionTimeout(8l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor3())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setParamsSerializer(new Stubs.Serializer3())
                    .startParamConfig(0).forForm()
                    .setName("b")
                    .setSerializer(new Stubs.Serializer1())
                    .setInjector(new Stubs.RequestParameterInjector3())
                    .endParamConfig()
                    .startParamConfig(1).forQuery()
                    .setName("c")
                    .setSerializer(new Stubs.Serializer2())
                    .endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m1SIs)
                    .setPath("/m1")
                    .setHttpMethod("HEAD")
                    .setSocketTimeout(9l)
                    .setConnectionTimeout(10l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor1())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setParamsSerializer(new Stubs.Serializer1())
                    .startParamConfig(0).forPath()
                    .setName("d")
                    .setSerializer(new Stubs.Serializer1())
                    .endParamConfig()
                    .startParamConfig(1).forForm()
                    .setName("e")
                    .setSerializer(new Stubs.Serializer3())
                    .endParamConfig()
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2)
                    .setPath("/m2/1")
                    .setHttpMethod("GET")
                    .setSocketTimeout(11l)
                    .setConnectionTimeout(12l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor3())
                    .setResponseHandler(new Stubs.ResponseHandler1())
                    .setParamsSerializer(new Stubs.Serializer1())
                    .endMethodConfig()
                    .startMethodConfig(Interface.METH_m2FSs)
                    .setPath("/m2/2")
                    .setHttpMethod("POST")
                    .setSocketTimeout(13l)
                    .setConnectionTimeout(14l)
                    .setRequestInterceptor(new Stubs.RequestInterceptor2())
                    .setResponseHandler(new Stubs.ResponseHandler2())
                    .setParamsSerializer(new Stubs.Serializer2())
                    .startParamConfig(0).forPath()
                    .setName("f")
                    .setSerializer(new Stubs.Serializer3())
                    .endParamConfig()
                    .startParamConfig(1).forPath()
                    .setName("g")
                    .setSerializer(new Stubs.Serializer1())
                    .endParamConfig()
                    .endMethodConfig()
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
