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

import org.codegist.crest.CRestContext;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.annotate.Injector;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class PropertiesDrivenInterfaceFactoryTest extends AbstractInterfaceConfigFactoryTest {

    private final CRestContext mockContext = mock(CRestContext.class);

    @Test
    public void testInterfaceOverridesTypeInjector() throws ConfigFactoryException {
        Properties p = new Properties();
        p.setProperty("service.test.class", "org.codegist.crest.config.PropertiesDrivenInterfaceFactoryTest$InjectorTestInterface");
        p.setProperty("service.test.end-point", "http://localhost:8080");
        p.setProperty("service.test.method.m.pattern", "get.*");
        p.setProperty("service.test.method.m.params.0.injector", "org.codegist.crest.Stubs$RequestParameterInjector3");
        PropertiesDrivenInterfaceFactory factory = newFactory(p);
        InterfaceConfig cfg = factory.newConfig(InjectorTestInterface.class, mockContext);
        assertEquals(Stubs.RequestParameterInjector3.class, cfg.getMethodConfig(InjectorTestInterface.M).getParamConfig(0).getInjector().getClass());
    }

    @Test
    public void testTypeInjectorIsRead() throws ConfigFactoryException {
        Properties p = new Properties();
//        p.setProperty("service.end-point", "hello");

        p.setProperty("service.test.class", "org.codegist.crest.config.PropertiesDrivenInterfaceFactoryTest$InjectorTestInterface");
        p.setProperty("service.test.end-point", "http://localhost:8080");
        PropertiesDrivenInterfaceFactory factory = newFactory(p);
        InterfaceConfig cfg = factory.newConfig(InjectorTestInterface.class, mockContext);
        assertEquals(Stubs.RequestParameterInjector1.class, cfg.getMethodConfig(InjectorTestInterface.M).getParamConfig(0).getInjector().getClass());
    }

    @Injector(Stubs.RequestParameterInjector1.class)
    static class Model {
    }

    static interface InjectorTestInterface {
        void get(Model m);

        Method M = TestUtils.getMethod(InjectorTestInterface.class, "get", Model.class);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidConfig() throws ConfigFactoryException {
        PropertiesDrivenInterfaceFactory factory = new PropertiesDrivenInterfaceFactory(new Properties());
        factory.newConfig(Interface.class, mockContext);
    }

    @Test
    public void testMinimalConfig() throws IOException, ConfigFactoryException {
        PropertiesDrivenInterfaceFactory factory = newFactory("minimal-config.properties");
        assertMinimalExpected(factory.newConfig(Interface.class, mockContext), Interface.class);
    }

    @Test
    public void testPartialConfig() throws IOException, ConfigFactoryException {
        PropertiesDrivenInterfaceFactory factory = newFactory("partial-config.properties");
        assertPartialExpected(factory.newConfig(Interface.class, mockContext), Interface.class);
    }

    @Test
    public void testFullConfig() throws IOException, ConfigFactoryException {
        PropertiesDrivenInterfaceFactory factory = newFactory("full-config.properties");
        assertFullExpected(factory.newConfig(Interface.class, mockContext), Interface.class);
    }


    public PropertiesDrivenInterfaceFactory newFactory(Properties p) {
        return new PropertiesDrivenInterfaceFactory(p);
    }

    public PropertiesDrivenInterfaceFactory newFactory(String n) throws IOException {
        Properties prop = new Properties();
        InputStream is = getClass().getResourceAsStream(n);
        try {
            prop.load(is);
        } finally {
            is.close();
        }
        return newFactory(prop);
    }

    @Test
    public void testServerConfig() throws IOException, InstantiationException, IllegalAccessException, ConfigFactoryException {
        Properties props = new Properties();
        props.setProperty("service.end-point", "hello");
        PropertiesDrivenInterfaceFactory factory = new PropertiesDrivenInterfaceFactory(props);
        InterfaceConfig config = factory.newConfig(Interface.class, mockContext);
        InterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(Interface.class, "hello").build();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

}
