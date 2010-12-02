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

package org.codegist.crest;

import org.codegist.common.reflect.ProxyFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class TestUtils {

    public static <T> T newInstance(Class<T> clazz){
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getClass(Object o) {
        return o != null ? o.getClass() : null;
    }

    public static ProxyFactory mockProxyFactory() {
        return mock(ProxyFactory.class, withSettings().defaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final ProxyFactory.InvocationHandler handler = (ProxyFactory.InvocationHandler) invocationOnMock.getArguments()[1];
                Class[] interfaces = (Class[]) invocationOnMock.getArguments()[2];
                return mock(interfaces[0], withSettings().defaultAnswer(new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return handler.invoke(null, invocationOnMock.getMethod(), invocationOnMock.getArguments());
                    }
                }));
            }
        }));
    }

    public static Method getMethod(Class clazz, String name, Class<?>... args) {
        try {
            return clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
