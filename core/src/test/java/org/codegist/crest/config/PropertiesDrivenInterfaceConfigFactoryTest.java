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

import org.codegist.common.io.IOs;
import org.codegist.crest.CRestContext;
import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.annotate.Injector;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class PropertiesDrivenInterfaceConfigFactoryTest extends AbstractInterfaceConfigFactoryTest {

    private final CRestContext mockContext = mock(CRestContext.class);

    @Test
    public void testInterfaceOverridesTypeInjector() throws ConfigFactoryException {
        Map<String,String> p = new HashMap<String, String>();
        p.put("service.test.class", "org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactoryTest$InjectorTestInterface");
        p.put("service.test.end-point", "http://localhost:8080");
        p.put("service.test.method.m.pattern", "get.*");
        p.put("service.test.method.m.params.0.name", "hello");
        p.put("service.test.method.m.params.0.injector", "org.codegist.crest.Stubs$RequestParameterInjector3");
        PropertiesDrivenInterfaceConfigFactory factory = newFactory(p);
        InterfaceConfig cfg = factory.newConfig(InjectorTestInterface.class, mockContext);
        assertEquals(Stubs.RequestParameterInjector3.class, cfg.getMethodConfig(InjectorTestInterface.M).getParamConfig(0).getInjector().getClass());
    }

    @Test
    public void testTypeInjectorIsRead() throws ConfigFactoryException {
        Map<String,String> p = new HashMap<String, String>();
        p.put("service.test.class", "org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactoryTest$InjectorTestInterface");
        p.put("service.test.end-point", "http://localhost:8080");
        p.put("service.test.method.m.pattern", "get.*");
        p.put("service.test.method.m.params.0.name", "hello");
        PropertiesDrivenInterfaceConfigFactory factory = newFactory(p);
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
        PropertiesDrivenInterfaceConfigFactory factory = new PropertiesDrivenInterfaceConfigFactory(new HashMap<String, String>());
        factory.newConfig(Interface.class, mockContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigMissingParamName() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException {
        PropertiesDrivenInterfaceConfigFactory factory = new PropertiesDrivenInterfaceConfigFactory(new HashMap<String, String>(){{
            put("service.test.class", "org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactoryTest$InjectorTestInterface");
            put("service.test.end-point","http://localhost:8080");
            put("service.test.context-path","/my-path");    
            put("service.test.method.m.pattern", ".*");
        }});
        factory.newConfig(InjectorTestInterface.class, mockContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigMissingEndpoint() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException {
        PropertiesDrivenInterfaceConfigFactory factory = new PropertiesDrivenInterfaceConfigFactory(new HashMap<String, String>(){{
            put("service.test.class", "org.codegist.crest.config.PropertiesDrivenInterfaceConfigFactoryTest$InjectorTestInterface");
            put("service.test.context-path","/my-path");    
            put("service.test.method.m.pattern", ".*");
            put("service.test.method.m.params.0.name","param");
        }});
        factory.newConfig(InjectorTestInterface.class, mockContext);
    }

    @Test
    public void testMinimalConfig() throws ConfigFactoryException {
        PropertiesDrivenInterfaceConfigFactory factory = newFactory("minimal-config.properties");
        assertMinimalExpected(factory.newConfig(Interface.class, mockContext), Interface.class);
    }

    @Test
    public void testPartialConfig() throws  ConfigFactoryException {
        PropertiesDrivenInterfaceConfigFactory factory = newFactory("partial-config.properties");
        assertPartialExpected(factory.newConfig(Interface.class, mockContext), Interface.class);
    }

    @Test
    public void testFullConfig() throws ConfigFactoryException {
        PropertiesDrivenInterfaceConfigFactory factory = newFactory("full-config.properties");
        assertFullExpected(factory.newConfig(Interface.class, mockContext), Interface.class);
    }


    public PropertiesDrivenInterfaceConfigFactory newFactory(Map<String,String> p) {
        return new PropertiesDrivenInterfaceConfigFactory(p);
    }

    public PropertiesDrivenInterfaceConfigFactory newFactory(String n) {
        Properties prop = new Properties();
        InputStream is = getClass().getResourceAsStream(n);
        try {
            prop.load(is);
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOs.close(is);
        }
        return newFactory((Map)prop);
    }

    @Test
    public void testServerConfigOverride() throws IOException, InstantiationException, IllegalAccessException, ConfigFactoryException {
        Map<String,String> props = new HashMap<String, String>();
        props.put("service.end-point", "hello");
        PropertiesDrivenInterfaceConfigFactory factory = new PropertiesDrivenInterfaceConfigFactory(props, true);
        InterfaceConfig config = factory.newConfig(Interface.class, mockContext);
        InterfaceConfig expected = new ConfigBuilders.InterfaceConfigBuilder(Interface.class).setEndPoint("hello").buildTemplate();
        InterfaceConfigTestHelper.assertExpected(expected, config, Interface.class);
    }

}
