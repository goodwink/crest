package org.codegist.crest.config;

import org.codegist.crest.*;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.codegist.crest.config.InterfaceConfig.DEFAULT_ENCODING;
import static org.codegist.crest.config.MethodConfig.*;
import static org.codegist.crest.config.ParamConfig.*;

public class ConfigBuildersTest {

    @Test
    public void testDefaultOverrides() throws InstantiationException, IllegalAccessException {
        final Map<String, Object> defaultOverrides = new HashMap<String, Object>();
        defaultOverrides.put(InterfaceConfig.DEFAULT_PATH_PROP, "/path");
        defaultOverrides.put(InterfaceConfig.DEFAULT_ENCODING_PROP, "ISO-8859-1");
        defaultOverrides.put(InterfaceConfig.DEFAULT_REQUEST_INTERCEPTOR_PROP, new Stubs.RequestInterceptor1());
        defaultOverrides.put(DEFAULT_PATH_PROP, "/meth-path");
        defaultOverrides.put(DEFAULT_REQUEST_INTERCEPTOR_PROP, new Stubs.RequestInterceptor2());
        defaultOverrides.put(DEFAULT_SO_TIMEOUT_PROP, 120l);
        defaultOverrides.put(DEFAULT_CO_TIMEOUT_PROP, 121l);
        defaultOverrides.put(DEFAULT_ERROR_HANDLER_PROP, new Stubs.ErrorHandler1());
        defaultOverrides.put(DEFAULT_HTTP_METHOD_PROP, HttpMethod.HEAD);
        defaultOverrides.put(DEFAULT_RESPONSE_HANDLER_PROP, new Stubs.ResponseHandler1());
        defaultOverrides.put(DEFAULT_DESTINATION_PROP, Destination.BODY);
        defaultOverrides.put(DEFAULT_INJECTOR_PROP, new Stubs.RequestParameterInjector1());
        defaultOverrides.put(DEFAULT_SERIALIZER_PROP, new Stubs.Serializer1());
        defaultOverrides.put(DEFAULT_NAME_PROP, "name");
        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://server:8080",
                (String) defaultOverrides.get(InterfaceConfig.DEFAULT_PATH_PROP),
                (String) defaultOverrides.get(InterfaceConfig.DEFAULT_ENCODING_PROP),
                (RequestInterceptor) defaultOverrides.get(InterfaceConfig.DEFAULT_REQUEST_INTERCEPTOR_PROP),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            (String) defaultOverrides.get(DEFAULT_PATH_PROP),
                            (HttpMethod) defaultOverrides.get(DEFAULT_HTTP_METHOD_PROP),
                            (Long) defaultOverrides.get(DEFAULT_SO_TIMEOUT_PROP),
                            (Long) defaultOverrides.get(DEFAULT_CO_TIMEOUT_PROP),
                            (RequestInterceptor) defaultOverrides.get(DEFAULT_REQUEST_INTERCEPTOR_PROP),
                            (ResponseHandler) defaultOverrides.get(DEFAULT_RESPONSE_HANDLER_PROP),
                            (ErrorHandler) defaultOverrides.get(DEFAULT_ERROR_HANDLER_PROP),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(DEFAULT_NAME_PROP),
                                            (Destination) defaultOverrides.get(DEFAULT_DESTINATION_PROP),
                                            (Serializer) defaultOverrides.get(DEFAULT_SERIALIZER_PROP),
                                            (RequestInjector) defaultOverrides.get(DEFAULT_INJECTOR_PROP)
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            (String) defaultOverrides.get(DEFAULT_PATH_PROP),
                            (HttpMethod) defaultOverrides.get(DEFAULT_HTTP_METHOD_PROP),
                            (Long) defaultOverrides.get(DEFAULT_SO_TIMEOUT_PROP),
                            (Long) defaultOverrides.get(DEFAULT_CO_TIMEOUT_PROP),
                            (RequestInterceptor) defaultOverrides.get(DEFAULT_REQUEST_INTERCEPTOR_PROP),
                            (ResponseHandler) defaultOverrides.get(DEFAULT_RESPONSE_HANDLER_PROP),
                            (ErrorHandler) defaultOverrides.get(DEFAULT_ERROR_HANDLER_PROP),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(DEFAULT_NAME_PROP),
                                            (Destination) defaultOverrides.get(DEFAULT_DESTINATION_PROP),
                                            (Serializer) defaultOverrides.get(DEFAULT_SERIALIZER_PROP),
                                            (RequestInjector) defaultOverrides.get(DEFAULT_INJECTOR_PROP)
                                    ),
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(DEFAULT_NAME_PROP),
                                            (Destination) defaultOverrides.get(DEFAULT_DESTINATION_PROP),
                                            (Serializer) defaultOverrides.get(DEFAULT_SERIALIZER_PROP),
                                            (RequestInjector) defaultOverrides.get(DEFAULT_INJECTOR_PROP)
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
                InterfaceConfig.DEFAULT_PATH,
                DEFAULT_ENCODING,
                DEFAULT_REQUEST_INTERCEPTOR,
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            DEFAULT_PATH,
                            DEFAULT_HTTP_METHOD,
                            DEFAULT_SO_TIMEOUT,
                            DEFAULT_CO_TIMEOUT,
                            DEFAULT_REQUEST_INTERCEPTOR,
                            DEFAULT_RESPONSE_HANDLER,
                            DEFAULT_ERROR_HANDLER,
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            DEFAULT_SERIALIZER,
                                            DEFAULT_INJECTOR
                                    )
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            DEFAULT_PATH,
                            DEFAULT_HTTP_METHOD,
                            DEFAULT_SO_TIMEOUT,
                            DEFAULT_CO_TIMEOUT,
                            DEFAULT_REQUEST_INTERCEPTOR,
                            DEFAULT_RESPONSE_HANDLER,
                            DEFAULT_ERROR_HANDLER,
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            DEFAULT_SERIALIZER,
                                            DEFAULT_INJECTOR
                                    ),
                                    new DefaultParamConfig(
                                            DEFAULT_NAME,
                                            DEFAULT_DESTINATION,
                                            DEFAULT_SERIALIZER,
                                            DEFAULT_INJECTOR
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
                InterfaceConfig.DEFAULT_PATH,
                DEFAULT_ENCODING,
                DEFAULT_REQUEST_INTERCEPTOR,
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            "/test",
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
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
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
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
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080")
                .setMethodsPath("/test")
                .setMethodsHttpMethod(HttpMethod.DELETE)
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
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
                InterfaceConfig.DEFAULT_PATH,
                DEFAULT_ENCODING,
                DEFAULT_REQUEST_INTERCEPTOR,
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            "/test",
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
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
                            HttpMethod.POST,
                            12l,
                            13l,
                            new Stubs.RequestInterceptor3(),
                            new Stubs.ResponseHandler3(),
                            new Stubs.ErrorHandler3(),
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
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080")
                .setMethodsPath("/test")
                .setMethodsHttpMethod(HttpMethod.DELETE)
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
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
                InterfaceConfig.DEFAULT_PATH,
                DEFAULT_ENCODING,
                DEFAULT_REQUEST_INTERCEPTOR,
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            "/test",
                            HttpMethod.DELETE,
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
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
                            HttpMethod.POST,
                            12l,
                            13l,
                            new Stubs.RequestInterceptor3(),
                            new Stubs.ResponseHandler3(),
                            new Stubs.ErrorHandler3(),
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
                                    )
                            }
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "http://server:8080")
                .setMethodsPath("/test")
                .setMethodsHttpMethod(HttpMethod.DELETE)
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
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
                            null, null, null, null, null, null, null,
                            new ParamConfig[]{
                                    new DefaultParamConfig(null, null, null, null)
                            }
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            null, null, null, null, null, null, null,
                            new ParamConfig[]{
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

        int b(String a, int b);

        Method A = TestUtils.getMethod(Interface.class, "a", String.class);
        Method B = TestUtils.getMethod(Interface.class, "b", String.class, int.class);
    }
}
