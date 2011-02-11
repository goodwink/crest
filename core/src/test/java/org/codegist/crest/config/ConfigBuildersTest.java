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

import org.codegist.crest.CRestProperty;
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ConfigBuildersTest {

    private static final BasicParamConfig[] PARAMS = new BasicParamConfig[]{new DefaultBasicParamConfig("1","2", Destination.FORM)};

    @Test
    public void testConfigBuildersPlaceholdersEmpty(){
        ConfigBuilders cb = new ConfigBuilders(new HashMap<String, Object>()) {};
        assertEquals("http://my.end-point.server:my.end-point.port/bla/bla/my.end-point.server/bla",
                cb.replacePlaceholders("http://my.end-point.server:my.end-point.port/bla/bla/my.end-point.server/bla"));
        assertEquals("http://{my.end-point.server}:{my.end-point.port}/bla/bla/{my.end-point.server}/bla",
                cb.replacePlaceholders("http://{my.end-point.server}:{my.end-point.port}/bla/bla/{my.end-point.server}/bla"));

        cb = new ConfigBuilders(null) {};
        assertEquals("http://my.end-point.server:my.end-point.port/bla/bla/my.end-point.server/bla",
                cb.replacePlaceholders("http://my.end-point.server:my.end-point.port/bla/bla/my.end-point.server/bla"));
        assertEquals("http://{my.end-point.server}:{my.end-point.port}/bla/bla/{my.end-point.server}/bla",
                cb.replacePlaceholders("http://{my.end-point.server}:{my.end-point.port}/bla/bla/{my.end-point.server}/bla"));
    }
    @Test
    public void testConfigBuildersPlaceholders(){
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("my.end-point.server", "127.0.0.1");
        placeholders.put("my.end-point.port", "8080");
        placeholders.put("bla", "blo"); // this one should not be replaced
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(CRestProperty.CONFIG_PLACEHOLDERS_MAP, placeholders);

        ConfigBuilders cb = new ConfigBuilders(props) {};
        assertEquals("http://my.end-point.server:my.end-point.port/bla/bla/my.end-point.server/bla",
                cb.replacePlaceholders("http://my.end-point.server:my.end-point.port/bla/bla/my.end-point.server/bla"));

        assertEquals("http://127.0.0.1:8080/{bla}/bla/127.0.0.1/bla",
                cb.replacePlaceholders("http://{my.end-point.server}:{my.end-point.port}/\\{bla\\}/bla/{my.end-point.server}/bla"));

    }

    @Test
    public void testParamsNameKey() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        InterfaceConfig b = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                .setEndPoint("d")
                .addMethodsExtraFormParam("ddd", "aaa")
                .addMethodsExtraFormParam("ddd", "bbb")
                .startMethodConfig(Interface.B)
                .startParamConfig(0).setName("a").endParamConfig()
                .startParamConfig(1).setName("a").endParamConfig()
                .startParamConfig(2).setName("a").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Interface.A)
                .startParamConfig(0).setName("a").endParamConfig()
                .addExtraHeaderParam("aaa", "bbb")
                .addExtraHeaderParam("aaa", "ccc")
                .endMethodConfig()
                .build();
        assertArrayEquals(
                new BasicParamConfig[]{new DefaultBasicParamConfig("ddd", "bbb", Destination.FORM)},
                b.getMethodConfig(Interface.B).getExtraParams()
        );
        assertArrayEquals(
                new BasicParamConfig[]{new DefaultBasicParamConfig("ddd", "bbb", Destination.FORM), new DefaultBasicParamConfig("aaa", "ccc", Destination.HEADER)},
                b.getMethodConfig(Interface.A).getExtraParams()
        );
    }
    @Test
    public void testParamsNameKey2() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        InterfaceConfig b = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, new HashMap<String, Object>(){{
            put(CRestProperty.CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, new BasicParamConfig[]{
                    new DefaultBasicParamConfig("ddd", "aaa", Destination.FORM),

            });
        }})
                .setEndPoint("d")
                .addMethodsExtraFormParam("ddd", "bbb")
                .startMethodConfig(Interface.B)
                .startParamConfig(0).setName("a").endParamConfig()
                .startParamConfig(1).setName("a").endParamConfig()
                .startParamConfig(2).setName("a").endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Interface.A)
                .startParamConfig(0).setName("a").endParamConfig()
                .addExtraHeaderParam("aaa", "ccc")
                .endMethodConfig()
                .build();
        assertArrayEquals(
                new BasicParamConfig[]{new DefaultBasicParamConfig("ddd", "bbb", Destination.FORM)},
                b.getMethodConfig(Interface.B).getExtraParams()
        );
        assertArrayEquals(
                new BasicParamConfig[]{new DefaultBasicParamConfig("ddd", "bbb", Destination.FORM), new DefaultBasicParamConfig("aaa", "ccc", Destination.HEADER)},
                b.getMethodConfig(Interface.A).getExtraParams()
        );
    }


    @Test
    public void testPlaceholderReplacement() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        final Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("my.place.holder.server", "127.0.0.1");
        placeholders.put("my.place.holder.port", "8080");
        placeholders.put("my.place.holder.path", "/path");
        placeholders.put("my.place.holder.enc", "ISO-8859-1");
        placeholders.put("my.place.holder.glob-inter", Stubs.RequestInterceptor1.class.getName());
        placeholders.put("my.place.holder.meth-path", "/meth-path");
        placeholders.put("my.place.holder.meth-req-inter", Stubs.RequestInterceptor2.class.getName());
        placeholders.put("my.place.holder.meth-so-to", "120");
        placeholders.put("my.place.holder.meth-co-to", "121");
        placeholders.put("my.place.holder.meth-error", Stubs.ErrorHandler1.class.getName());
        placeholders.put("my.place.holder.meth-retry", Stubs.RetryHandler1.class.getName());
        placeholders.put("my.place.holder.meth-http", "HEAD");
        placeholders.put("my.place.holder.meth-resp", Stubs.ResponseHandler1.class.getName());
        placeholders.put("my.place.holder.param-dest", "FORM");
        placeholders.put("my.place.holder.param-def", "def");
        placeholders.put("my.place.holder.param-req-inject", Stubs.RequestParameterInjector1.class.getName());
        placeholders.put("my.place.holder.param-seri", Stubs.Serializer1.class.getName());
        placeholders.put("my.place.holder.param-name", "name");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(CRestProperty.CONFIG_PLACEHOLDERS_MAP, placeholders);

        InterfaceConfig expected = new DefaultInterfaceConfig(
                Interface.class,
                "http://" + placeholders.get("my.place.holder.server") + ":" + placeholders.get("my.place.holder.port"),
                placeholders.get("my.place.holder.path") + "/cpath",
                placeholders.get("my.place.holder.enc"),
                (RequestInterceptor) Class.forName(placeholders.get("my.place.holder.glob-inter")).newInstance(),
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            placeholders.get("my.place.holder.meth-path") + "/path",
                            placeholders.get("my.place.holder.meth-http"),
                            Long.valueOf(placeholders.get("my.place.holder.meth-so-to")),
                            Long.valueOf(placeholders.get("my.place.holder.meth-co-to")),
                            (RequestInterceptor) Class.forName(placeholders.get("my.place.holder.meth-req-inter")).newInstance(),
                            (ResponseHandler) Class.forName(placeholders.get("my.place.holder.meth-resp")).newInstance(),
                            (ErrorHandler) Class.forName(placeholders.get("my.place.holder.meth-error")).newInstance(),
                            (RetryHandler) Class.forName(placeholders.get("my.place.holder.meth-retry")).newInstance(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            placeholders.get("my.place.holder.param-name"),
                                            placeholders.get("my.place.holder.param-def"),
                                            Destination.valueOf(placeholders.get("my.place.holder.param-dest")),
                                            (Serializer) Class.forName(placeholders.get("my.place.holder.param-seri")).newInstance(),
                                            (Injector) Class.forName(placeholders.get("my.place.holder.param-req-inject")).newInstance()
                                    )
                            },
                            new BasicParamConfig[0]
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            placeholders.get("my.place.holder.meth-path") + "/path",
                            placeholders.get("my.place.holder.meth-http"),
                            Long.valueOf(placeholders.get("my.place.holder.meth-so-to")),
                            Long.valueOf(placeholders.get("my.place.holder.meth-co-to")),
                            (RequestInterceptor) Class.forName(placeholders.get("my.place.holder.meth-req-inter")).newInstance(),
                            (ResponseHandler) Class.forName(placeholders.get("my.place.holder.meth-resp")).newInstance(),
                            (ErrorHandler) Class.forName(placeholders.get("my.place.holder.meth-error")).newInstance(),
                            (RetryHandler) Class.forName(placeholders.get("my.place.holder.meth-retry")).newInstance(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            placeholders.get("my.place.holder.param-name"),
                                            placeholders.get("my.place.holder.param-def"),
                                            Destination.valueOf(placeholders.get("my.place.holder.param-dest")),
                                            (Serializer) Class.forName(placeholders.get("my.place.holder.param-seri")).newInstance(),
                                            (Injector) Class.forName(placeholders.get("my.place.holder.param-req-inject")).newInstance()
                                    ),
                                    new DefaultParamConfig(
                                            placeholders.get("my.place.holder.param-name"),
                                            placeholders.get("my.place.holder.param-def"),
                                            Destination.valueOf(placeholders.get("my.place.holder.param-dest")),
                                            (Serializer) Class.forName(placeholders.get("my.place.holder.param-seri")).newInstance(),
                                            (Injector) Class.forName(placeholders.get("my.place.holder.param-req-inject")).newInstance()
                                    ),
                                    new DefaultParamConfig(
                                            placeholders.get("my.place.holder.param-name"),
                                            placeholders.get("my.place.holder.param-def"),
                                            Destination.valueOf(placeholders.get("my.place.holder.param-dest")),
                                            (Serializer) Class.forName(placeholders.get("my.place.holder.param-seri")).newInstance(),
                                            (Injector) Class.forName(placeholders.get("my.place.holder.param-req-inject")).newInstance()
                                    )
                            },
                            new BasicParamConfig[0]
                    ));
                }}
        );

        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, props)
                .setEndPoint("http://{my.place.holder.server}:{my.place.holder.port}")
                .setContextPath("{my.place.holder.path}/cpath")
                .setEncoding("{my.place.holder.enc}")
                .setGlobalInterceptor("{my.place.holder.glob-inter}")
                .setMethodsPath("{my.place.holder.meth-path}/path")
                .setMethodsRequestInterceptor("{my.place.holder.meth-req-inter}")
                .setMethodsSocketTimeout("{my.place.holder.meth-so-to}")
                .setMethodsConnectionTimeout("{my.place.holder.meth-co-to}")
                .setMethodsErrorHandler("{my.place.holder.meth-error}")
                .setMethodsRetryHandler("{my.place.holder.meth-retry}")
                .setMethodsHttpMethod("{my.place.holder.meth-http}")
                .setMethodsResponseHandler("{my.place.holder.meth-resp}")
                .setParamsInjector("{my.place.holder.param-req-inject}")
                .setParamsSerializer("{my.place.holder.param-seri}")
                .startMethodConfig(Interface.A)
                .startParamConfig(0)
                .setDestination("{my.place.holder.param-dest}")
                .setName("{my.place.holder.param-name}")
                .setDefaultValue("{my.place.holder.param-def}")
                .endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Interface.B)
                .startParamConfig(0)
                .setDestination("{my.place.holder.param-dest}")
                .setName("{my.place.holder.param-name}")
                .setDefaultValue("{my.place.holder.param-def}")
                .endParamConfig()
                .startParamConfig(1)
                .setDestination("{my.place.holder.param-dest}")
                .setName("{my.place.holder.param-name}")
                .setDefaultValue("{my.place.holder.param-def}")
                .endParamConfig()
                .startParamConfig(2)
                .setDestination("{my.place.holder.param-dest}")
                .setName("{my.place.holder.param-name}")
                .setDefaultValue("{my.place.holder.param-def}")
                .endParamConfig()
                .endMethodConfig()
                .build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }


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
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_HTTP_METHOD, "HEAD");
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER, new Stubs.ResponseHandler1());
        defaultOverrides.put(CONFIG_METHOD_DEFAULT_EXTRA_PARAMS, PARAMS);
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_DESTINATION, Destination.FORM);
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_INJECTOR, new Stubs.RequestParameterInjector1());
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_SERIALIZER, new Stubs.Serializer1());
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_NAME, "name");
        defaultOverrides.put(CONFIG_PARAM_DEFAULT_VALUE, "def");
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
                            (String) defaultOverrides.get(CONFIG_METHOD_DEFAULT_HTTP_METHOD),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_SO_TIMEOUT),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_CO_TIMEOUT),
                            (RequestInterceptor) defaultOverrides.get(CONFIG_METHOD_DEFAULT_REQUEST_INTERCEPTOR),
                            (ResponseHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER),
                            (ErrorHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_ERROR_HANDLER),
                            (RetryHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_VALUE),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    )
                            },
                            (BasicParamConfig[]) defaultOverrides.get(CONFIG_METHOD_DEFAULT_EXTRA_PARAMS)
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            (String) defaultOverrides.get(CONFIG_METHOD_DEFAULT_PATH),
                            (String) defaultOverrides.get(CONFIG_METHOD_DEFAULT_HTTP_METHOD),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_SO_TIMEOUT),
                            (Long) defaultOverrides.get(CONFIG_METHOD_DEFAULT_CO_TIMEOUT),
                            (RequestInterceptor) defaultOverrides.get(CONFIG_METHOD_DEFAULT_REQUEST_INTERCEPTOR),
                            (ResponseHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RESPONSE_HANDLER),
                            (ErrorHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_ERROR_HANDLER),
                            (RetryHandler) defaultOverrides.get(CONFIG_METHOD_DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_VALUE),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_VALUE),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_NAME),
                                            (String) defaultOverrides.get(CONFIG_PARAM_DEFAULT_VALUE),
                                            (Destination) defaultOverrides.get(CONFIG_PARAM_DEFAULT_DESTINATION),
                                            (Serializer) defaultOverrides.get(CONFIG_PARAM_DEFAULT_SERIALIZER),
                                            (Injector) defaultOverrides.get(CONFIG_PARAM_DEFAULT_INJECTOR)
                                    )
                            },
                            (BasicParamConfig[]) defaultOverrides.get(CONFIG_METHOD_DEFAULT_EXTRA_PARAMS)
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, defaultOverrides).setEndPoint("http://server:8080").build();
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
                            DEFAULT_HTTP_METHOD,
                            DEFAULT_SO_TIMEOUT,
                            DEFAULT_CO_TIMEOUT,
                            TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                            TestUtils.newInstance(DEFAULT_RESPONSE_HANDLER),
                            TestUtils.newInstance(DEFAULT_ERROR_HANDLER),
                            TestUtils.newInstance(DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new ToStringSerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    )
                            },
                            DEFAULT_EXTRA_PARAMS
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            DEFAULT_PATH,
                            DEFAULT_HTTP_METHOD,
                            DEFAULT_SO_TIMEOUT,
                            DEFAULT_CO_TIMEOUT,
                            TestUtils.newInstance(DEFAULT_REQUEST_INTERCEPTOR),
                            TestUtils.newInstance(DEFAULT_RESPONSE_HANDLER),
                            TestUtils.newInstance(DEFAULT_ERROR_HANDLER),
                            TestUtils.newInstance(DEFAULT_RETRY_HANDLER),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new ToStringSerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new ArraySerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new DateSerializer(),
                                            TestUtils.newInstance(DEFAULT_INJECTOR)
                                    )
                            },
                            DEFAULT_EXTRA_PARAMS
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class).setEndPoint("http://server:8080").setParamsName("n").build();
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
                            "DELETE",
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    )
                            },
                            PARAMS
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            "/test",
                            "DELETE",
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    )
                            },
                            PARAMS
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                .setEndPoint("http://server:8080")
                .setParamsName("n")
                .setMethodsPath("/test")
                .addMethodsExtraParam("1", "2", Destination.FORM)
                .setMethodsHttpMethod("DELETE")
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
                .setMethodsRetryHandler(new Stubs.RetryHandler2())
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
                            "DELETE",
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler1(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer2(),
                                            new Stubs.RequestParameterInjector2()
                                    )
                            },
                            PARAMS
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            "/test2",
                            "POST",
                            12l,
                            13l,
                            new Stubs.RequestInterceptor3(),
                            new Stubs.ResponseHandler3(),
                            new Stubs.ErrorHandler3(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    )
                            },
                            PARAMS
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class).setEndPoint("http://server:8080").setParamsName("n")
                .setMethodsPath("/test")
                .addMethodsExtraParam("1", "2", Destination.FORM)
                .setMethodsHttpMethod("DELETE")
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
                .setMethodsRetryHandler(new Stubs.RetryHandler1())
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsInjector(new Stubs.RequestParameterInjector2())
                .startMethodConfig(Interface.B)
                .setPath("/test2")
                .setHttpMethod("POST")
                .setSocketTimeout(12l)
                .setConnectionTimeout(13l)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setResponseHandler(new Stubs.ResponseHandler3())
                .setErrorHandler(new Stubs.ErrorHandler3())
                .setRetryHandler(new Stubs.RetryHandler2())
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
                            "DELETE",
                            10l,
                            11l,
                            new Stubs.RequestInterceptor2(),
                            new Stubs.ResponseHandler2(),
                            new Stubs.ErrorHandler2(),
                            new Stubs.RetryHandler2(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "name4",
                                            "def",
                                            Destination.QUERY,
                                            new Stubs.Serializer1(),
                                            new Stubs.RequestParameterInjector1()
                                    )
                            },
                            PARAMS
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            "/test2",
                            "POST",
                            12l,
                            13l,
                            new Stubs.RequestInterceptor3(),
                            new Stubs.ResponseHandler3(),
                            new Stubs.ErrorHandler3(),
                            new Stubs.RetryHandler1(),
                            new ParamConfig[]{
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer1(),
                                            new Stubs.RequestParameterInjector1()
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    ),
                                    new DefaultParamConfig(
                                            "n",
                                            DEFAULT_VALUE,
                                            DEFAULT_DESTINATION,
                                            new Stubs.Serializer3(),
                                            new Stubs.RequestParameterInjector3()
                                    )
                            },
                            PARAMS
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
                .setEndPoint("http://server:8080")
                .setParamsName("n")
                .setMethodsPath("/test")
                .addMethodsExtraParam("1", "2", Destination.FORM)
                .setMethodsHttpMethod("DELETE")
                .setMethodsSocketTimeout(10l)
                .setMethodsConnectionTimeout(11l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor2())
                .setMethodsResponseHandler(new Stubs.ResponseHandler2())
                .setMethodsErrorHandler(new Stubs.ErrorHandler2())
                .setMethodsRetryHandler(new Stubs.RetryHandler2())
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsInjector(new Stubs.RequestParameterInjector2())
                .startMethodConfig(Interface.A)
                .startParamConfig(0)
                .setName("name4")
                .setDefaultValue("def")
                .setDestination("QUERY")
                .setSerializer(new Stubs.Serializer1())
                .setInjector(new Stubs.RequestParameterInjector1())
                .endParamConfig()
                .endMethodConfig()
                .startMethodConfig(Interface.B)
                .setPath("/test2")
                .setHttpMethod("POST")
                .setSocketTimeout(12l)
                .setConnectionTimeout(13l)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setResponseHandler(new Stubs.ResponseHandler3())
                .setErrorHandler(new Stubs.ErrorHandler3())
                .setRetryHandler(new Stubs.RetryHandler1())
                .setParamsSerializer(new Stubs.Serializer3())
                .setParamsInjector(new Stubs.RequestParameterInjector3())
                .startParamConfig(0)
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
                null,
                null, null, null,
                new HashMap<Method, MethodConfig>() {{
                    put(Interface.A, new DefaultMethodConfig(
                            Interface.A,
                            null, null, null, null, null, null, null, null,
                            new ParamConfig[]{
                                    new DefaultParamConfig(null, null, null, null, null)
                            },new BasicParamConfig[0]
                    ));
                    put(Interface.B, new DefaultMethodConfig(
                            Interface.B,
                            null, null, null, null, null, null, null, null,
                            new ParamConfig[]{
                                    new DefaultParamConfig(null, null, null, null, null),
                                    new DefaultParamConfig(null, null, null, null, null),
                                    new DefaultParamConfig(null, null, null, null, null)
                            },new BasicParamConfig[0]
                    ));
                }}
        );
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(Interface.class).buildTemplate();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }


    public static interface Interface {
        void a(String a);

        int b(String a, int[] b, Date date);

        Method A = TestUtils.getMethod(Interface.class, "a", String.class);
        Method B = TestUtils.getMethod(Interface.class, "b", String.class, int[].class, Date.class);
    }
}
