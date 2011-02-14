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
import org.codegist.crest.HttpRequest;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
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

    static final InterfaceConfig FULL_CONFIG = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class)
            .setEndPoint("server")
            .setPath("path")
            .setEncoding("iso")
            .setGlobalInterceptor(new Stubs.RequestInterceptor1())
            .setMethodsSocketTimeout(1l)
            .setMethodsConnectionTimeout(2l)
            .setParamsSerializer(new Stubs.Serializer1())
            .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
            .setMethodsResponseHandler(new Stubs.ResponseHandler1())
            .startMethodConfig(TestInterface.T1)
            .setPath("path2")
            .setHttpMethod("GET")
            .setConnectionTimeout(3l)
            .setSocketTimeout(4l)
            .setRequestInterceptor(new Stubs.RequestInterceptor2())
            .setResponseHandler(new Stubs.ResponseHandler2())
            .setParamsSerializer(new Stubs.Serializer2())
            .endMethodConfig()
            .startMethodConfig(TestInterface.T2)
            .setPath("path2")
            .setHttpMethod("GET")
            .setConnectionTimeout(5l)
            .setSocketTimeout(6l)
            .setRequestInterceptor(new Stubs.RequestInterceptor3())
            .setParamsSerializer(new Stubs.Serializer3())
            .startParamConfig(0)
            .setDestination("path")
            .setName("name4")
            .setSerializer(new Stubs.Serializer3())
            .endParamConfig()
            .startParamConfig(1)
            .setDestination("form")
            .setName("name5")
            .setSerializer(new Stubs.Serializer2())
            .endParamConfig()
            .endMethodConfig()
            .build();


    @Test
    public void testNullOverride() throws NoSuchMethodException {
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class, new HashMap<String, Object>(){{put(CRestProperty.CONFIG_PARAM_DEFAULT_NAME, "d");}})
                .setEndPoint("server")
                .build();
        assertEquals(config, Configs.override(config, null));
    }

    @Test
    public void testEmptyOverride() throws NoSuchMethodException {
        InterfaceConfig config = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class).setEndPoint("server").setParamsName("n").build();
        InterfaceConfig override = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class).buildTemplate();
        InterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class).setEndPoint("server").setParamsName("n")
                .setGlobalInterceptor(new CompositeRequestInterceptor())
                .setMethodsRequestInterceptor(new CompositeRequestInterceptor())

                .build();
        InterfaceConfig result = Configs.override(config, override);
        InterfaceConfigTestHelper.assertExpected(expected, result, TestInterface.class);
    }

    @Test
    public void testFullOverride() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        InterfaceConfig base = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class).setEndPoint("server").buildTemplate();
        InterfaceConfig result = Configs.override(base, FULL_CONFIG);
        InterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class).setEndPoint("server")
                .setPath("path")
                .setMethodsSocketTimeout(1l)
                .setMethodsConnectionTimeout(2l)
                .setEncoding("iso")
                .setParamsSerializer(new Stubs.Serializer1())
//                .setParamsName("name")
//                .setParamsDestination(Destination.BODY)
                .setGlobalInterceptor(new Stubs.RequestInterceptor1())
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
                .setMethodsResponseHandler(new Stubs.ResponseHandler1())
                .startMethodConfig(TestInterface.T1)
                .setPath("path2")
                .setHttpMethod("GET")
                .setConnectionTimeout(3l)
                .setSocketTimeout(4l)
                .setRequestInterceptor(new Stubs.RequestInterceptor2())
                .setResponseHandler(new Stubs.ResponseHandler2())
                .setParamsSerializer(new Stubs.Serializer2())
                .endMethodConfig()
                .startMethodConfig(TestInterface.T2)
                .setPath("path2")
                .setHttpMethod("GET")
                .setConnectionTimeout(5l)
                .setSocketTimeout(6l)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setParamsSerializer(new Stubs.Serializer3())
                .startParamConfig(0)
                .setDestination("path")
                .setName("name4")
                .setSerializer(new Stubs.Serializer3())
                .endParamConfig()
                .startParamConfig(1)
                .setDestination("form")
                .setName("name5")
                .setSerializer(new Stubs.Serializer2())
                .endParamConfig()
                .endMethodConfig()
                .build();

        InterfaceConfigTestHelper.assertExpected(expected, result, TestInterface.class);
    }

    @Test
    public void testPartialOverride() throws NoSuchMethodException, InstantiationException, IllegalAccessException {

        InterfaceConfig override = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class)
                .setEndPoint("server2")
//                .setParamsDestination(Destination.URL)
                .startMethodConfig(TestInterface.T1)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setPath("path2bis")
                .endMethodConfig()
                .startMethodConfig(TestInterface.T2)
                .setRequestInterceptor(new Stubs.RequestInterceptor2())
                .setParamsSerializer(new Stubs.Serializer2())
                .startParamConfig(0)
                .setName("name3bis")
                .endParamConfig()
                .startParamConfig(1)
                .setDestination("path")
                .setName("name6")
                .endParamConfig()
                .endMethodConfig()
                .buildTemplate();
        InterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(TestInterface.class)
                .setEndPoint("server2")
                .setPath("path")
                .setEncoding("iso")
                .setGlobalInterceptor(new Stubs.RequestInterceptor1())
                .setMethodsSocketTimeout(1l)
                .setMethodsConnectionTimeout(2l)
                .setMethodsRequestInterceptor(new Stubs.RequestInterceptor1())
                .setMethodsResponseHandler(new Stubs.ResponseHandler1())
                .startMethodConfig(TestInterface.T1)
                .setPath("path2bis")
                .setHttpMethod("GET")
                .setConnectionTimeout(3l)
                .setSocketTimeout(4l)
                .setRequestInterceptor(new Stubs.RequestInterceptor3())
                .setResponseHandler(new Stubs.ResponseHandler2())
                .setParamsSerializer(new Stubs.Serializer2())
//                .setParamsName("name2bis")
//                .setParamsDestination(Destination.URL)
                .endMethodConfig()
                .startMethodConfig(TestInterface.T2)
                .setPath("path2")
                .setHttpMethod("GET")
                .setConnectionTimeout(5l)
                .setSocketTimeout(6l)
                .setRequestInterceptor(new Stubs.RequestInterceptor2())
                .setParamsSerializer(new Stubs.Serializer2())
//                .setParamsName("name3bis")
//                .setParamsDestination(Destination.BODY)
                .startParamConfig(0)
                .forPath()
                .setName("name3bis")
                .endParamConfig()
                .startParamConfig(1)
                .forPath()
                .setName("name6")
                .endParamConfig()
                .endMethodConfig()
                .build();
        InterfaceConfig result = Configs.override(FULL_CONFIG, override);
        InterfaceConfigTestHelper.assertExpected(expected, result, TestInterface.class);
    }


    @Test
    public void testStaticOverrideWithCustomMutableConfigs() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        testOverrideWithMutableConfig(false);
    }
//    @Test
//    public void testDynamicOverrideWithCustomMutableConfigs() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
//        testOverrideWithMutableConfig(true);
//    }

    private static void testOverrideWithMutableConfig(boolean dynamic){
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
                mutableMethodConfig.setHttpMethod("POST");
                mutableMethodConfig.setSocketTimeout(12l);
                mutableMethodConfig.setConnectionTimeout(13l);
                mutableMethodConfig.setRequestInterceptor(new Stubs.RequestInterceptor1());
                mutableMethodConfig.setResponseHandler(new Stubs.ResponseHandler1());
                mutableMethodConfig.setErrorHandler(new Stubs.ErrorHandler1());

                MutableMethodParamConfig[] paramConfigs = new MutableMethodParamConfig[meth.getParameterTypes().length];
                for (int i = 0; i < paramConfigs.length; i++) {
                    paramConfigs[i] = new MutableMethodParamConfig();
                    paramConfigs[i].setDestination(HttpRequest.DEST_FORM);
                    paramConfigs[i].setInjector(new Stubs.RequestParameterInjector3());
                    paramConfigs[i].setName("name" + i);
                    paramConfigs[i].setSerializer(new Stubs.Serializer3());
                }
                mutableMethodConfig.setParamsConfigs(paramConfigs);

                put(meth, mutableMethodConfig);
            }
        }});

        MutableInterfaceConfig mutableOverride = new MutableInterfaceConfig();
        mutableOverride.setInterface(TestInterface.class);
        mutableOverride.setCache(new HashMap<Method, MethodConfig>() {{
            for (Method meth : TestInterface.class.getDeclaredMethods()) {
                MutableMethodConfig mutableMethodConfig = new MutableMethodConfig();
                MutableMethodParamConfig[] paramConfigs = new MutableMethodParamConfig[meth.getParameterTypes().length];
                for (int i = 0; i < paramConfigs.length; i++) {
                    paramConfigs[i] = new MutableMethodParamConfig();
                }
                mutableMethodConfig.setParamsConfigs(paramConfigs);
                put(meth, mutableMethodConfig);
            }
        }});

        InterfaceConfig result = Configs.override(mutableBase, mutableOverride);
        InterfaceConfigTestHelper.assertExpected(mutableBase, result, TestInterface.class);


        assertEquals("/path", result.getPath());
        mutableOverride.setPath("hello");
        if(dynamic) {
            assertEquals("hello", result.getPath());
        }else{
            assertEquals("/path", result.getPath());
        }

        MutableMethodConfig m = ((MutableMethodConfig) mutableBase.getMethodConfig(TestInterface.T1));
        assertEquals("POST", result.getMethodConfig(TestInterface.T1).getHttpMethod());
        m.setHttpMethod("PUT");
        if(dynamic) {
            assertEquals("PUT", result.getMethodConfig(TestInterface.T1).getHttpMethod());
        }else{
            assertEquals("POST", result.getMethodConfig(TestInterface.T1).getHttpMethod());
        }


        MutableMethodParamConfig p = (MutableMethodParamConfig) ((MutableMethodConfig) mutableBase.getMethodConfig(TestInterface.T2)).getParamConfig(0);
        assertEquals("name0", result.getMethodConfig(TestInterface.T2).getParamConfig(0).getName());
        p.setName("hhhhhh");
        if(dynamic) {
            assertEquals("hhhhhh", result.getMethodConfig(TestInterface.T2).getParamConfig(0).getName());
        }else{
            assertEquals("name0", result.getMethodConfig(TestInterface.T2).getParamConfig(0).getName());
        }
    }

    private static class MutableInterfaceConfig implements InterfaceConfig {
        private Class<?> interfaze;
        private String server;
        private String path;
        private String encoding;
        private RequestInterceptor requestInterceptor;

        private Map<Method, MethodConfig> cache;


        
        public Class<?> getInterface() {
            return interfaze;
        }

        public void setInterface(Class<?> interfaze) {
            this.interfaze = interfaze;
        }

        
        public String getEndPoint() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        
        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        
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

        
        public Method[] getMethods() {
            return interfaze.getDeclaredMethods();
        }

        
        public MethodConfig getMethodConfig(Method meth) {
            return cache != null ? cache.get(meth) : null;
        }
    }

    private static class MutableMethodConfig implements MethodConfig {
        private Method method;
        private String path;
        private String httpMethod;
        private Long socketTimeout;
        private Long connectionTimeout;
        private RequestInterceptor requestInterceptor;
        private ResponseHandler responseHandler;
        private ErrorHandler errorHandler;
        private RetryHandler retryHandler;

        private MethodParamConfig[] methodParamConfigs;
        private ParamConfig[] extraParams;

        
        public MethodParamConfig getParamConfig(int index) {
            return methodParamConfigs != null && index < methodParamConfigs.length ? methodParamConfigs[index] : null;
        }

        
        public ParamConfig[] getExtraParams() {
            return extraParams;
        }

        public void setExtraParams(ParamConfig[] extraParams) {
            this.extraParams = extraParams;
        }

        
        public Integer getParamCount() {
            return methodParamConfigs.length;
        }

        
        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        
        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        
        public Long getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(Long socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        
        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        
        public RequestInterceptor getRequestInterceptor() {
            return requestInterceptor;
        }

        public void setRequestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptor = requestInterceptor;
        }

        
        public ResponseHandler getResponseHandler() {
            return responseHandler;
        }

        public void setResponseHandler(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        
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

        public MethodParamConfig[] getParamConfigs() {
            return methodParamConfigs;
        }

        public void setParamsConfigs(MethodParamConfig[] methodParamConfigs) {
            this.methodParamConfigs = methodParamConfigs;
        }
    }

    private static class MutableMethodParamConfig implements MethodParamConfig {
        private String name;
        private String defaultValue;
        private String dest;
        private Serializer serializer;
        private Injector injector;


        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDestination() {
            return dest;
        }

        public void setDestination(String dest) {
            this.dest = dest;
        }

        
        public Serializer getSerializer() {
            return serializer;
        }

        public void setSerializer(Serializer serializer) {
            this.serializer = serializer;
        }

        
        public Injector getInjector() {
            return injector;
        }

        public void setInjector(Injector injector) {
            this.injector = injector;
        }
    }
}
