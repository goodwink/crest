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

import static org.junit.Assert.assertEquals;

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
            if (expected.getMethodConfig(originalMethods[m]) == null) {
                assertEquals(meth.toGenericString(), null, test.getMethodConfig(meth));
                continue;
            }
            assertEquals(meth.toGenericString(), expected.getMethodConfig(originalMethods[m]).getPath(), test.getMethodConfig(meth).getPath());
            assertEquals(meth.toGenericString(), expected.getMethodConfig(originalMethods[m]).getHttpMethod(), test.getMethodConfig(meth).getHttpMethod());
            assertEquals(meth.toGenericString(), expected.getMethodConfig(originalMethods[m]).getConnectionTimeout(), test.getMethodConfig(meth).getConnectionTimeout());
            assertEquals(meth.toGenericString(), expected.getMethodConfig(originalMethods[m]).getSocketTimeout(), test.getMethodConfig(meth).getSocketTimeout());
            assertEquals(meth.toGenericString(), TestUtils.getClass(expected.getMethodConfig(originalMethods[m]).getErrorHandler()), TestUtils.getClass(test.getMethodConfig(meth).getErrorHandler()));
            assertEquals(meth.toGenericString(), TestUtils.getClass(expected.getMethodConfig(originalMethods[m]).getRetryHandler()), TestUtils.getClass(test.getMethodConfig(meth).getRetryHandler()));
            assertEquals(meth.toGenericString(), TestUtils.getClass(expected.getMethodConfig(originalMethods[m]).getResponseHandler()), TestUtils.getClass(test.getMethodConfig(meth).getResponseHandler()));
            if (expected.getMethodConfig(originalMethods[m]).getRequestInterceptor() instanceof CompositeRequestInterceptor) {
                int max = ((CompositeRequestInterceptor) expected.getMethodConfig(originalMethods[m]).getRequestInterceptor()).getInterceptors().length;
                for (int i = 0; i < max; i++) {
                    RequestInterceptor expectedRI = ((CompositeRequestInterceptor) expected.getMethodConfig(originalMethods[m]).getRequestInterceptor()).getInterceptors()[i];
                    RequestInterceptor testRI = ((CompositeRequestInterceptor) test.getMethodConfig(meth).getRequestInterceptor()).getInterceptors()[i];
                    assertEquals(meth.toGenericString(), TestUtils.getClass(expectedRI), TestUtils.getClass(testRI));
                }
            } else {
                assertEquals(meth.toGenericString(), TestUtils.getClass(expected.getMethodConfig(originalMethods[m]).getRequestInterceptor()), TestUtils.getClass(test.getMethodConfig(meth).getRequestInterceptor()));
            }


            assertEquals(expected.getMethodConfig(originalMethods[m]).getParamCount(), test.getMethodConfig(meth).getParamCount());
            for (int i = 0; i < expected.getMethodConfig(originalMethods[m]).getParamCount(); i++) {
                String tag = meth.toGenericString() + "(" + i + ")";
                if (expected.getMethodConfig(originalMethods[m]).getParamConfig(i) == null) {
                    assertEquals(tag, null, test.getMethodConfig(meth).getParamConfig(i));
                    continue;
                }
                assertEquals(tag, expected.getMethodConfig(originalMethods[m]).getParamConfig(i).getDestination(), test.getMethodConfig(meth).getParamConfig(i).getDestination());
                assertEquals(tag, expected.getMethodConfig(originalMethods[m]).getParamConfig(i).getName(), test.getMethodConfig(meth).getParamConfig(i).getName());
                assertEquals(tag, TestUtils.getClass(expected.getMethodConfig(originalMethods[m]).getParamConfig(i).getSerializer()), TestUtils.getClass(test.getMethodConfig(meth).getParamConfig(i).getSerializer()));
                assertEquals(tag, TestUtils.getClass(expected.getMethodConfig(originalMethods[m]).getParamConfig(i).getInjector()), TestUtils.getClass(test.getMethodConfig(meth).getParamConfig(i).getInjector()));
            }
            m++;
        }
    }
}
