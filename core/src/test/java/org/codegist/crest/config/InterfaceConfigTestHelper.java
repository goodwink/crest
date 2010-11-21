package org.codegist.crest.config;

import org.codegist.crest.TestUtils;
import org.codegist.crest.interceptor.CompositeRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class InterfaceConfigTestHelper {


    public static void assertExpected(InterfaceConfig expected, InterfaceConfig test, Class<?> clazz) {
        assertEquals(clazz.toString(), expected.getPath(), test.getPath());
        assertEquals(clazz.toString(), expected.getServer(), test.getServer());
        assertEquals(clazz.toString(), expected.getEncoding(), test.getEncoding());

        if (expected.getRequestInterceptor() instanceof CompositeRequestInterceptor) {
            int max = ((CompositeRequestInterceptor) expected.getRequestInterceptor()).getInterceptors().length;
            for (int i = 0; i < max; i++) {
                RequestInterceptor expectedRI = ((CompositeRequestInterceptor) expected.getRequestInterceptor()).getInterceptors()[i];
                RequestInterceptor testRI = ((CompositeRequestInterceptor) test.getRequestInterceptor()).getInterceptors()[i];
                assertEquals(clazz.toString(), TestUtils.getClass(expectedRI), TestUtils.getClass(testRI));
            }
        } else {
            assertEquals(clazz.toString(), TestUtils.getClass(expected.getRequestInterceptor()), TestUtils.getClass(test.getRequestInterceptor()));
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
