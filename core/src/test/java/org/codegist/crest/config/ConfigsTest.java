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
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
import org.codegist.crest.interceptor.EmptyRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.Serializer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class ConfigsTest {

    interface TestInterface {

        void test1();

        void test2(int[] i, Object o);

        Method T1 = TestUtils.getMethod(TestInterface.class, "test1");
        Method T2 = TestUtils.getMethod(TestInterface.class, "test2", int[].class, Object.class);
    }

    static final DefaultInterfaceConfig FULL_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server")
            .setContextPath("path")
            .setEncoding("iso")
            .setGlobalInterceptor(new Stubs.RequestInterceptor1())
            .setMethodsSocketTimeout(1l)
            .setMethodsConnectionTimeout(2l)
            .setParamsSerializer(new Stubs.Serializer1())
            .setParamsName("name")
            .setParamsDestination(Destination.BODY)
            .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
            .setMethodsResponseHandler(new Stubs.ResponseHandler1())
            .startMethodConfig(TestInterface.T1)
            .setPath("path2")
            .setHttpMethod(HttpMethod.GET)
            .setConnectionTimeout(3l)
            .setSocketTimeout(4l)
            .setRequestInterceptor(new Stubs.RequestInterceptor2())
            .setResponseHandler(new Stubs.ResponseHandler2())
            .setParamsSerializer(new Stubs.Serializer2())
            .setParamsName("name2")
            .setParamsDestination(Destination.URL)
            .endMethodConfig()
            .startMethodConfig(TestInterface.T2)
            .setPath("path2")
            .setHttpMethod(HttpMethod.GET)
            .setConnectionTimeout(5l)
            .setSocketTimeout(6l)
            .setRequestInterceptor(new Stubs.RequestInterceptor3())
            .setParamsSerializer(new Stubs.Serializer3())
            .setParamsName("name3")
            .setParamsDestination(Destination.URL)
            .startParamConfig(0)
            .setDestination("URL")
            .setName("name4")
            .setSerializer(new Stubs.Serializer3())
            .endParamConfig()
            .startParamConfig(1)
            .setDestination("BODY")
            .setName("name5")
            .setSerializer(new Stubs.Serializer2())
            .endParamConfig()
            .endMethodConfig()
            .build();


    @Test
    public void testNullOverride() throws NoSuchMethodException {
        DefaultInterfaceConfig override = null;
        DefaultInterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server").build();
        assertEquals(config, Configs.override(config, override));
    }

    @Test
    public void testEmptyOverride() throws NoSuchMethodException {
        DefaultInterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server").build();
        DefaultInterfaceConfig override = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server").build();
        DefaultInterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server")
                .setGlobalInterceptor(new CompositeRequestInterceptor())
                .setMethodsRequestInterceptor(new CompositeRequestInterceptor())
                .build();
        InterfaceConfig result = Configs.override(config, override);
        InterfaceConfigTestHelper.assertExpected(expected, result, TestInterface.class);
    }

    @Test
    public void testFullOverride() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        DefaultInterfaceConfig base = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server").build();
        InterfaceConfig result = Configs.override(base, FULL_CONFIG);
        DefaultInterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server")
                .setContextPath("path")
                .setMethodsSocketTimeout(1l)
                .setMethodsConnectionTimeout(2l)
                .setEncoding("iso")
                .setParamsSerializer(new Stubs.Serializer1())
                .setParamsName("name")
                .setParamsDestination(Destination.BODY)
                .setGlobalInterceptor(new CompositeRequestInterceptor(new Stubs.RequestInterceptor1(), new EmptyRequestInterceptor()))
                .setMethodsRequestInterceptor(new CompositeRequestInterceptor(new Stubs.RequestInterceptor1(), new EmptyRequestInterceptor()))
                .setMethodsResponseHandler(new Stubs.ResponseHandler1())
                .startMethodConfig(TestInterface.T1)
                .setPath("path2")
                .setHttpMethod(HttpMethod.GET)
                .setConnectionTimeout(3l)
                .setSocketTimeout(4l)
                .setRequestInterceptor(new CompositeRequestInterceptor(new Stubs.RequestInterceptor2(), new EmptyRequestInterceptor()))
                .setResponseHandler(new Stubs.ResponseHandler2())
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsName("name2")
                .setParamsDestination(Destination.URL)
                .endMethodConfig()
                .startMethodConfig(TestInterface.T2)
                .setPath("path2")
                .setHttpMethod(HttpMethod.GET)
                .setConnectionTimeout(5l)
                .setSocketTimeout(6l)
                .setRequestInterceptor(new CompositeRequestInterceptor(new Stubs.RequestInterceptor3(), new EmptyRequestInterceptor()))
                .setParamsSerializer(new Stubs.Serializer3())
                .setParamsName("name3")
                .setParamsDestination(Destination.URL)
                .startParamConfig(0)
                .setDestination("URL")
                .setName("name4")
                .setSerializer(new Stubs.Serializer3())
                .endParamConfig()
                .startParamConfig(1)
                .setDestination("BODY")
                .setName("name5")
                .setSerializer(new Stubs.Serializer2())
                .endParamConfig()
                .endMethodConfig()
                .build();

        InterfaceConfigTestHelper.assertExpected(expected, result, TestInterface.class);
    }

    @Test
    public void testPartialOverride() throws NoSuchMethodException, InstantiationException, IllegalAccessException {

        DefaultInterfaceConfig override = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server2")
                .setParamsDestination(Destination.URL)
                .startMethodConfig(TestInterface.T1)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setPath("path2bis")
                .endMethodConfig()
                .startMethodConfig(TestInterface.T2)
                .setRequestInterceptor(new Stubs.RequestInterceptor2())
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsName("name3bis")
                .setParamsDestination(Destination.BODY)
                .startParamConfig(1)
                .setDestination("URL")
                .setName("name6")
                .endParamConfig()
                .endMethodConfig()
                .build(false);
        InterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, "server2")
                .setContextPath("path")
                .setEncoding("iso")
                .setGlobalInterceptor(new Stubs.RequestInterceptor1())
                .setMethodsSocketTimeout(1l)
                .setMethodsConnectionTimeout(2l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
                .setMethodsResponseHandler(new Stubs.ResponseHandler1())
                .startMethodConfig(TestInterface.T1)
                .setPath("path2bis")
                .setHttpMethod(HttpMethod.GET)
                .setConnectionTimeout(3l)
                .setSocketTimeout(4l)
                .setRequestInterceptor(new CompositeRequestInterceptor(new Stubs.RequestInterceptor3(), new Stubs.RequestInterceptor2()))
                .setResponseHandler(new Stubs.ResponseHandler2())
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsName("name2bis")
                .setParamsDestination(Destination.URL)
                .endMethodConfig()
                .startMethodConfig(TestInterface.T2)
                .setPath("path2")
                .setHttpMethod(HttpMethod.GET)
                .setConnectionTimeout(5l)
                .setSocketTimeout(6l)
                .setRequestInterceptor(new CompositeRequestInterceptor(new Stubs.RequestInterceptor2(), new Stubs.RequestInterceptor3()))
                .setParamsSerializer(new Stubs.Serializer2())
                .setParamsName("name3bis")
                .setParamsDestination(Destination.BODY)
                .startParamConfig(0)
                .setName("name3bis")
                .endParamConfig()
                .startParamConfig(1)
                .setDestination("URL")
                .setName("name6")
                .endParamConfig()
                .endMethodConfig()
                .build();
        InterfaceConfig result = Configs.override(FULL_CONFIG, override);
        InterfaceConfigTestHelper.assertExpected(expected, result, TestInterface.class);
    }


    @Test
    public void testOverrideWithCustomMutableConfigs() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        MutableInterfaceConfig mutableBase = new MutableInterfaceConfig();
        mutableBase.setInterface(TestInterface.class);
        mutableBase.setServer("http://server");
        mutableBase.setPath("/path");
        mutableBase.setEncoding("utf-8");
        mutableBase.setCache(new HashMap<Method, MethodConfig>() {{
            for (Method meth : TestInterface.class.getDeclaredMethods()) {
                MutableMethodConfig mutableMethodConfig = new MutableMethodConfig();
                mutableMethodConfig.setPath("/test");
                mutableMethodConfig.setMethod(meth);
                mutableMethodConfig.setHttpMethod(HttpMethod.POST);
                mutableMethodConfig.setSocketTimeout(12l);
                mutableMethodConfig.setConnectionTimeout(13l);
                mutableMethodConfig.setRequestInterceptor(new Stubs.RequestInterceptor1());
                mutableMethodConfig.setResponseHandler(new Stubs.ResponseHandler1());
                mutableMethodConfig.setErrorHandler(new Stubs.ErrorHandler1());

                MutableParamConfig[] paramConfigs = new MutableParamConfig[meth.getParameterTypes().length];
                for (int i = 0; i < paramConfigs.length; i++) {
                    paramConfigs[i] = new MutableParamConfig();
                    paramConfigs[i].setDestination(Destination.BODY);
                    paramConfigs[i].setInjector(new Stubs.RequestParameterInjector3());
                    paramConfigs[i].setName("name" + i);
                    paramConfigs[i].setSerializer(new Stubs.Serializer3());
                }
                mutableMethodConfig.setParamsConfigs(paramConfigs);

                put(meth, mutableMethodConfig);
            }
        }});

        MutableInterfaceConfig mutableOverride = new MutableInterfaceConfig();
        mutableOverride.setCache(new HashMap<Method, MethodConfig>() {{
            for (Method meth : TestInterface.class.getDeclaredMethods()) {
                MutableMethodConfig mutableMethodConfig = new MutableMethodConfig();
                MutableParamConfig[] paramConfigs = new MutableParamConfig[meth.getParameterTypes().length];
                for (int i = 0; i < paramConfigs.length; i++) {
                    paramConfigs[i] = new MutableParamConfig();
                }
                mutableMethodConfig.setParamsConfigs(paramConfigs);
                put(meth, mutableMethodConfig);
            }
        }});

        InterfaceConfig result = Configs.override(mutableBase, mutableOverride);
        InterfaceConfigTestHelper.assertExpected(mutableBase, result, TestInterface.class);


        assertEquals("/path", result.getContextPath());
        mutableOverride.setPath("hello");
        assertEquals("hello", result.getContextPath());

        MutableMethodConfig m = ((MutableMethodConfig) mutableBase.getMethodConfig(TestInterface.T1));
        assertEquals(HttpMethod.POST, result.getMethodConfig(TestInterface.T1).getHttpMethod());
        m.setHttpMethod(HttpMethod.PUT);
        assertEquals(HttpMethod.PUT, result.getMethodConfig(TestInterface.T1).getHttpMethod());

        MutableParamConfig p = (MutableParamConfig) ((MutableMethodConfig) mutableBase.getMethodConfig(TestInterface.T2)).getParamConfig(0);
        assertEquals("name0", result.getMethodConfig(TestInterface.T2).getParamConfig(0).getName());
        p.setName("hhhhhh");
        assertEquals("hhhhhh", result.getMethodConfig(TestInterface.T2).getParamConfig(0).getName());


    }

    private static class MutableInterfaceConfig implements InterfaceConfig {
        private Class<?> interfaze;
        private String server;
        private String path;
        private String encoding;
        private RequestInterceptor requestInterceptor;

        private Map<Method, MethodConfig> cache;


        @Override
        public Class<?> getInterface() {
            return interfaze;
        }

        public void setInterface(Class<?> interfaze) {
            this.interfaze = interfaze;
        }

        @Override
        public String getEndPoint() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        @Override
        public String getContextPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        @Override
        public RequestInterceptor getGlobalInterceptor() {
            return requestInterceptor;
        }

        public void setRequestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptor = requestInterceptor;
        }

        public Map<Method, MethodConfig> getCache() {
            return cache;
        }

        public void setCache(Map<Method, MethodConfig> cache) {
            this.cache = cache;
        }

        @Override
        public Method[] getMethods() {
            return interfaze.getDeclaredMethods();
        }

        @Override
        public MethodConfig getMethodConfig(Method meth) {
            return cache != null ? cache.get(meth) : null;
        }
    }

    private static class MutableMethodConfig implements MethodConfig {
        private Method method;
        private String path;
        private StaticParam[] defaultParams;
        private HttpMethod httpMethod;
        private Long socketTimeout;
        private Long connectionTimeout;
        private RequestInterceptor requestInterceptor;
        private ResponseHandler responseHandler;
        private ErrorHandler errorHandler;
        private RetryHandler retryHandler;

        private ParamConfig[] paramConfigs;

        @Override
        public ParamConfig getParamConfig(int index) {
            return paramConfigs != null && index < paramConfigs.length ? paramConfigs[index] : null;
        }

        @Override
        public StaticParam[] getStaticParams() {
            return defaultParams;
        }

        public void setDefaultParams(StaticParam[] defaultParams) {
            this.defaultParams = defaultParams;
        }

        @Override
        public Integer getParamCount() {
            return paramConfigs.length;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        @Override
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
        }

        @Override
        public Long getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(Long socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        @Override
        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        @Override
        public RequestInterceptor getRequestInterceptor() {
            return requestInterceptor;
        }

        public void setRequestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptor = requestInterceptor;
        }

        @Override
        public ResponseHandler getResponseHandler() {
            return responseHandler;
        }

        public void setResponseHandler(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }

        public void setErrorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
        }

        public RetryHandler getRetryHandler() {
            return retryHandler;
        }

        public void setRetryHandler(RetryHandler retryHandler) {
            this.retryHandler = retryHandler;
        }

        public ParamConfig[] getParamConfigs() {
            return paramConfigs;
        }

        public void setParamsConfigs(ParamConfig[] paramConfigs) {
            this.paramConfigs = paramConfigs;
        }
    }

    private static class MutableParamConfig implements ParamConfig {
        private String name;
        private Destination dest;
        private Serializer serializer;
        private Injector injector;


        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Destination getDestination() {
            return dest;
        }

        public void setDestination(Destination dest) {
            this.dest = dest;
        }

        @Override
        public Serializer getSerializer() {
            return serializer;
        }

        public void setSerializer(Serializer serializer) {
            this.serializer = serializer;
        }

        @Override
        public Injector getInjector() {
            return injector;
        }

        public void setInjector(Injector injector) {
            this.injector = injector;
        }
    }
}
