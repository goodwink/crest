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

import org.codegist.crest.TestUtils;
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class InterfaceConfigTestHelper {


    public static void assertExpected(InterfaceConfig expected, InterfaceConfig test, Class<?> clazz) {
        assertEquals(clazz.toString(), expected.getContextPath(), test.getContextPath());
        assertEquals(clazz.toString(), expected.getEndPoint(), test.getEndPoint());
        assertEquals(clazz.toString(), expected.getEncoding(), test.getEncoding());

        if (expected.getGlobalInterceptor() instanceof CompositeRequestInterceptor) {
            int max = ((CompositeRequestInterceptor) expected.getGlobalInterceptor()).getInterceptors().length;
            for (int i = 0; i < max; i++) {
                RequestInterceptor expectedRI = ((CompositeRequestInterceptor) expected.getGlobalInterceptor()).getInterceptors()[i];
                RequestInterceptor testRI = ((CompositeRequestInterceptor) test.getGlobalInterceptor()).getInterceptors()[i];
                assertEquals(clazz.toString(), TestUtils.getClass(expectedRI), TestUtils.getClass(testRI));
            }
        } else {
            assertEquals(clazz.toString(), TestUtils.getClass(expected.getGlobalInterceptor()), TestUtils.getClass(test.getGlobalInterceptor()));
        }

        int m = 0;
        Method[] originalMethods = expected.getInterface().getDeclaredMethods();
        for (Method meth : clazz.getDeclaredMethods()) {
            String testMsg = meth.toGenericString();
            MethodConfig expMethCfg = expected.getMethodConfig(originalMethods[m]);
            MethodConfig testMethCfg = test.getMethodConfig(meth);
            if (expected.getMethodConfig(originalMethods[m]) == null) {
                assertEquals(testMsg, null, test.getMethodConfig(meth));
                continue;
            }
            assertEquals(testMsg, expMethCfg.getPath(), testMethCfg.getPath());
            assertEquals(testMsg, expMethCfg.getHttpMethod(), testMethCfg.getHttpMethod());
            assertEquals(testMsg, expMethCfg.getConnectionTimeout(), testMethCfg.getConnectionTimeout());
            assertEquals(testMsg, expMethCfg.getSocketTimeout(), testMethCfg.getSocketTimeout());
            assertEquals(testMsg, TestUtils.getClass(expMethCfg.getErrorHandler()), TestUtils.getClass(testMethCfg.getErrorHandler()));
            assertEquals(testMsg, TestUtils.getClass(expMethCfg.getRetryHandler()), TestUtils.getClass(testMethCfg.getRetryHandler()));
            assertEquals(testMsg, TestUtils.getClass(expMethCfg.getResponseHandler()), TestUtils.getClass(testMethCfg.getResponseHandler()));
            assertTrue((expMethCfg.getStaticParams() == null && testMethCfg.getStaticParams() == null) || (expMethCfg.getStaticParams() != null && testMethCfg.getStaticParams() != null));
            if(expMethCfg.getStaticParams() != null && testMethCfg.getStaticParams() != null) {
                assertEquals(testMsg, new HashSet<StaticParam>(java.util.Arrays.asList(expMethCfg.getStaticParams())), new HashSet<StaticParam>(java.util.Arrays.asList(testMethCfg.getStaticParams())));
            }

            if (expMethCfg.getRequestInterceptor() instanceof CompositeRequestInterceptor) {
                int max = ((CompositeRequestInterceptor) expMethCfg.getRequestInterceptor()).getInterceptors().length;
                for (int i = 0; i < max; i++) {
                    RequestInterceptor expectedRI = ((CompositeRequestInterceptor) expMethCfg.getRequestInterceptor()).getInterceptors()[i];
                    RequestInterceptor testRI = ((CompositeRequestInterceptor) testMethCfg.getRequestInterceptor()).getInterceptors()[i];
                    assertEquals(testMsg, TestUtils.getClass(expectedRI), TestUtils.getClass(testRI));
                }
            } else {
                assertEquals(testMsg, TestUtils.getClass(expMethCfg.getRequestInterceptor()), TestUtils.getClass(testMethCfg.getRequestInterceptor()));
            }


            assertEquals(expMethCfg.getParamCount(), testMethCfg.getParamCount());
            for (int i = 0; i < expMethCfg.getParamCount(); i++) {
                String tag = testMsg + "(" + i + ")";
                if (expMethCfg.getParamConfig(i) == null) {
                    assertEquals(tag, null, testMethCfg.getParamConfig(i));
                    continue;
                }
                assertEquals(tag, expMethCfg.getParamConfig(i).getDestination(), testMethCfg.getParamConfig(i).getDestination());
                assertEquals(tag, expMethCfg.getParamConfig(i).getName(), testMethCfg.getParamConfig(i).getName());
                assertEquals(tag, TestUtils.getClass(expMethCfg.getParamConfig(i).getSerializer()), TestUtils.getClass(testMethCfg.getParamConfig(i).getSerializer()));
                assertEquals(tag, TestUtils.getClass(expMethCfg.getParamConfig(i).getInjector()), TestUtils.getClass(testMethCfg.getParamConfig(i).getInjector()));
            }
            m++;
        }
    }
}
