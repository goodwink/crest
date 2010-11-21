package org.codegist.crest;

import org.codegist.common.reflect.ProxyFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public class TestUtils {


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
