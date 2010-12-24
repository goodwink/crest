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

import org.codegist.crest.HttpMethod;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.ArraySerializer;
import org.codegist.crest.serializer.DateSerializer;
import org.codegist.crest.serializer.Serializer;
import org.codegist.crest.serializer.ToStringSerializer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.codegist.crest.CRestProperty.*;
import static org.codegist.crest.config.InterfaceConfig.DEFAULT_ENCODING;
import static org.codegist.crest.config.MethodConfig.*;
import static org.codegist.crest.config.ParamConfig.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ConfigBuildersTest {

    private static final StaticParam[] PARAMS = new StaticParam[]{new DefaultStaticParam("1","2", Destination.BODY)};

    @Test
    public void testDefaultOverrides() throws InstantiationException, IllegalAccessException {
        final Map<String, Object> defaultOverrides = new HashMap<String, Object>();
        defaultOverrides.put(CONFIG_INTERFACE_DEFAULT_CONTEXT_PATH, "/path");
        defaultOverrides.put(CONFIG_INTERFACE_DEFAULT_ENCODING, "ISO-8859-1");
        defaultOverrides.put(CONFIG_INTERFACE_DEFAULT_GLOBAL_INTERCEPTOR, new Stubs.RequestInterceptor1());
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_PATH, "/meth-path");
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_REQUEST_INTERCEPTOR, new Stubs.RequestInterceptor2());
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_SO_TIMEOUT, 120l);
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_CO_TIMEOUT, 121l);
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_ERROR_HANDLER, new Stubs.ErrorHandler1());
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_RETRY_HANDLER, new Stubs.RetryHandler1());
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_HTTP_METHOD, HttpMethod.HEAD);
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER, new Stubs.ResponseHandler1());
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_PARAMS, PARAMS);
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_DESTINATION, Destination.BODY);
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_INJECTOR, new Stubs.RequestParameterInjector1());
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_SERIALIZER, new Stubs.Serializer1());
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_NAME, "name");
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                (String) defaultOverrides.get(CONFIG_INTERFACE_DEFAULT_CONTEXT_PATH),
                (String) defaultOverrides.get(CONFIG_INTERFACE_DEFAULT_ENCODING),
                (RequestInterceptor) defaultOverrides.get(CONFIG_INTERFACE_DEFAULT_GLOBAL_INTERCEPTOR),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            (String) defaultOverrides.get(CONFIG_METHOD_DEFAULT_PATH),
                            (StaticParam[]) defaultOverrides.get(CONFIG_METHOD_DEFAULT_PARAMS),
                            (HttpMethod) defaultOverrides.get(CONFIG_METHOD_DEFAULT_HTTP_METHOD),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_SO_TIMEOUT),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_CO_TIMEOUT),
                            (RequestInterceptor) defaultOverrides.get(CONFIG_METHOD_DEFAULT_REQUEST_INTERCEPTOR),
                            (ResponseHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER),
                            (ErrorHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_ERROR_HANDLER),
                            (RetryHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            (String) defaultOverrides.get(CONFIG_METHOD_DEFAULT_PATH),
                            (StaticParam[]) defaultOverrides.get(CONFIG_METHOD_DEFAULT_PARAMS),
                            (HttpMethod) defaultOverrides.get(CONFIG_METHOD_DEFAULT_HTTP_METHOD),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_SO_TIMEOUT),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_CO_TIMEOUT),
                            (RequestInterceptor) defaultOverrides.get(CONFIG_METHOD_DEFAULT_REQUEST_INTERCEPTOR),
                            (ResponseHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER),
                            (ErrorHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_ERROR_HANDLER),
                            (RetryHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080", defaultOverrides).build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

    @Test
    public void minTest() {
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                InterfaceConfig.DEFAULT_CONTEXT_PATH,
                DEFAULT_ENCODING,
                TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            DEFAULT_PATH,
                            DEFAULT_PARAMS,
                            DEFAULT_HTTP_METHOD,
                            DEFAULT_SO_TIMEOUT,
                            DEFAULT_CO_TIMEOUT,
                            TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                            TestUtils.newInstance(DEFAULT_RESPONSE_HANDLER),
                            TestUtils.newInstance(DEFAULT_ERROR_HANDLER),
                            TestUtils.newInstance(DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            new ToStringSerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            DEFAULT_PATH,
                            DEFAULT_PARAMS,
                            DEFAULT_HTTP_METHOD,
                            DEFAULT_SO_TIMEOUT,
                            DEFAULT_CO_TIMEOUT,
                            TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                            TestUtils.newInstance(DEFAULT_RESPONSE_HANDLER),
                            TestUtils.newInstance(DEFAULT_ERROR_HANDLER),
                            TestUtils.newInstance(DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            new ToStringSerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            new ArraySerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            new DateSerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080").build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

    @Test
    public void testFallbackTest() throws InstantiationException, IllegalAccessException {
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                InterfaceConfig.DEFAULT_CONTEXT_PATH,
                DEFAULT_ENCODING,
                TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            "/test",
                            PARAMS,
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name",
                                            Destination.BODY,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            "/test",
                            PARAMS,
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name",
                                            Destination.BODY,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    ),
                                    new DefaultParamConfig(
                                            "name",
                                            Destination.BODY,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    ),
                                    new DefaultParamConfig(
                                            "name",
                                            Destination.BODY,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080")
                .setMethodsPath("/test")
                .addMethodsStaticParam("1", "2", Destination.BODY)
                .setMethodsHttpMethod(HttpMethod.DELETE)
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
                .setMethodsRetryHandler(new Stubs.RetryHandler2())
                .setParamsName("name")
                .setParamsDestination("BODY")
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsInjector(new Stubs.RequestParameterInjector2())
                .build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

    @Test
    public void testMethodOverrideTest() throws InstantiationException, IllegalAccessException {
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                InterfaceConfig.DEFAULT_CONTEXT_PATH,
                DEFAULT_ENCODING,
                TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            "/test",
                            PARAMS,
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler1(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name",
                                            Destination.BODY,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            "/test2",
                            PARAMS,
                            HttpMethod.POST,
                            12l,
                            13l,
                            new Stubs.RequestInterceptor3(),
                            new Stubs.ResponseHandler3(),
                            new Stubs.ErrorHandler3(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name2",
                                            Destination.URL,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    ),
                                    new DefaultParamConfig(
                                            "name2",
                                            Destination.URL,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    ),
                                    new DefaultParamConfig(
                                            "name2",
                                            Destination.URL,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080")
                .setMethodsPath("/test")
                .addMethodsStaticParam("1", "2", Destination.BODY)
                .setMethodsHttpMethod(HttpMethod.DELETE)
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
                .setMethodsRetryHandler(new Stubs.RetryHandler1())
                .setParamsName("name")
                .setParamsDestination("BODY")
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsInjector(new Stubs.RequestParameterInjector2())
                .startMethodConfig(Interface.B)
                .setPath("/test2")
                .setHttpMethod(HttpMethod.POST)
                .setSocketTimeout(12l)
                .setConnectionTimeout(13l)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setResponseHandler(new Stubs.ResponseHandler3())
                .setErrorHandler(new Stubs.ErrorHandler3())
                .setRetryHandler(new Stubs.RetryHandler2())
                .setParamsName("name2")
                .setParamsDestination("URL")
                .setParamsSerializer(new Stubs.Serializer3())
                .setParamsInjector(new Stubs.RequestParameterInjector3())
                .endMethodConfig()
                .build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

    @Test
    public void testParamOverrideTest() {
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                InterfaceConfig.DEFAULT_CONTEXT_PATH,
                DEFAULT_ENCODING,
                TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            "/test",
                            PARAMS,
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name4",
                                            Destination.URL,
                                            new Stubs.Serializer1(),
                                            new Stubs.RequestParameterInjector1()
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            "/test2",
                            PARAMS,
                            HttpMethod.POST,
                            12l,
                            13l,
                            new Stubs.RequestInterceptor3(),
                            new Stubs.ResponseHandler3(),
                            new Stubs.ErrorHandler3(),
                            new Stubs.RetryHandler1(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name3",
                                            Destination.BODY,
                                            new Stubs.Serializer1(),
                                            new Stubs.RequestParameterInjector1()
                                    ),
                                    new DefaultParamConfig(
                                            "name2",
                                            Destination.URL,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    ),
                                    new DefaultParamConfig(
                                            "name2",
                                            Destination.URL,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080")
                .setMethodsPath("/test")
                .addMethodsStaticParam("1", "2", Destination.BODY)
                .setMethodsHttpMethod(HttpMethod.DELETE)
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
                .setMethodsRetryHandler(new Stubs.RetryHandler2())
                .setParamsName("name")
                .setParamsDestination("BODY")
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsInjector(new Stubs.RequestParameterInjector2())
                .startMethodConfig(Interface.A)
                .startParamConfig(0)
                .setName("name4")
                .setDestination("URL")
                .setSerializer(new Stubs.Serializer1())
                .setInjector(new Stubs.RequestParameterInjector1())
                .endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Interface.B)
                .setPath("/test2")
                .setHttpMethod(HttpMethod.POST)
                .setSocketTimeout(12l)
                .setConnectionTimeout(13l)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setResponseHandler(new Stubs.ResponseHandler3())
                .setErrorHandler(new Stubs.ErrorHandler3())
                .setRetryHandler(new Stubs.RetryHandler1())
                .setParamsName("name2")
                .setParamsDestination("URL")
                .setParamsSerializer(new Stubs.Serializer3())
                .setParamsInjector(new Stubs.RequestParameterInjector3())
                .startParamConfig(0)
                .setName("name3")
                .setDestination("BODY")
                .setSerializer(new Stubs.Serializer1())
                .setInjector(new Stubs.RequestParameterInjector1())
                .endParamConfig()
                .endMethodConfig()
                .build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

    @Test
    public void testNoDefaults() throws InstantiationException, IllegalAccessException {
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                null, null, null,
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            null, null, null, null, null, null, null, null, null,
                            new ParamConfig[]{
                                    new DefaultParamConfig(null, null, null, null)
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            null, null, null, null, null, null, null, null,null,
                            new ParamConfig[]{
                                    new DefaultParamConfig(null, null, null, null),
                                    new DefaultParamConfig(null, null, null, null),
                                    new DefaultParamConfig(null, null, null, null)
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080").build(false);
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }


    public static interface Interface {
        void a(String a);

        int b(String a, int[] b, Date date);

        Method A = TestUtils.getMethod(Interface.class, "a", String.class);
        Method B = TestUtils.getMethod(Interface.class, "b", String.class, int[].class, Date.class);
    }
}
