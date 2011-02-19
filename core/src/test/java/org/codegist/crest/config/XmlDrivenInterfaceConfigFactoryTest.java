/*
 * Copyright 2010 CodeGist.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.crest.Stubs;
import org.codegist.crest.TestUtils;
import org.codegist.crest.annotate.Injector;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class XmlDrivenInterfaceConfigFactoryTest extends AbstractInterfaceConfigFactoryTest {

    @Test
    public void testInterfaceOverridesTypeInjector() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException {
        String xml = "<crest-config end-point=\"http://localhost:8080\">\n" +
                "\t<service class=\"org.codegist.crest.config.XmlDrivenInterfaceConfigFactoryTest$InjectorTestInterface\">\n" +
                "\t\t<end-point>http://localhost:8080</end-point>\n" +
                "\t\t<methods>\n" +
                "\t\t\t<method match=\"get.*\">\n" +
                "\t\t\t\t<params>\n" +
                "\t\t\t\t\t<query index=\"0\" name=\"hello\">\n" +
                "\t\t\t\t\t\t<injector>org.codegist.crest.Stubs$RequestParameterInjector3</injector>\n" +
                "\t\t\t\t\t</query>\n" +
                "\t\t\t\t</params>\n" +
                "\t\t\t</method>\n" +
                "\t\t</methods>\n" +
                "\t</service>\n" +
                "</crest-config>";
        XmlDrivenInterfaceConfigFactory factory = newFactory(new ByteArrayInputStream(xml.getBytes()), false);
        InterfaceConfig cfg = factory.newConfig(InjectorTestInterface.class, MOCK_CONTEXT);
        assertEquals(Stubs.RequestParameterInjector3.class, cfg.getMethodConfig(InjectorTestInterface.M).getParamConfig(0).getInjector().getClass());
    }

    @Test
    public void testTypeInjectorIsRead() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException {
        String xml = "<crest-config>\n" +
                "    <service class=\"org.codegist.crest.config.XmlDrivenInterfaceConfigFactoryTest$InjectorTestInterface\">\n" +
                "        <end-point>http://localhost:8080</end-point>    \n" +
                "\t\t<methods>\n" +
                "\t\t\t<method match=\"get.*\">\n" +
                "\t\t\t\t<params>\n" +
                "\t\t\t\t\t<query index=\"0\" name=\"hello\"/>\n" +
                "\t\t\t\t</params>\n" +
                "\t\t\t</method>\n" +
                "\t\t</methods>\n" +
                "\t</service>\n" +
                "</crest-config>";
        XmlDrivenInterfaceConfigFactory factory = newFactory(new ByteArrayInputStream(xml.getBytes()), false);
        InterfaceConfig cfg = factory.newConfig(InjectorTestInterface.class, MOCK_CONTEXT);
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
    public void testInvalidConfig() throws Exception {
        XmlDrivenInterfaceConfigFactory factory = newFactory("invalid-config.xml", false);
        factory.newConfig(Interface.class, MOCK_CONTEXT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConfigMissingParamName() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException {
        String xml = "<crest-config>\n" +
                "    <service class=\"org.codegist.crest.config.XmlDrivenInterfaceConfigFactoryTest$InjectorTestInterface\">\n" +
                "        <end-point>http://localhost:8080</end-point>    \n" +
                "\t\t<methods>\n" +
                "\t\t\t<method match=\"get.*\">\n" +
                "\t\t\t\t<params>\n" +
                "\t\t\t\t\t<query index=\"0\" />\n" +
                "\t\t\t\t</params>\n" +
                "\t\t\t</method>\n" +
                "\t\t</methods>\n" +
                "\t</service>\n" +
                "</crest-config>";
        XmlDrivenInterfaceConfigFactory factory = newFactory(new ByteArrayInputStream(xml.getBytes()), false);
        factory.newConfig(Interface.class, MOCK_CONTEXT);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConfigMissingEndpoint() throws ConfigFactoryException, IOException, SAXException, ParserConfigurationException {
        String xml = "<crest-config>\n" +
                "    <service class=\"org.codegist.crest.config.XmlDrivenInterfaceConfigFactoryTest$InjectorTestInterface\">\n" +
                "\t\t<methods>\n" +
                "\t\t\t<method match=\"get.*\">\n" +
                "\t\t\t\t<params>\n" +
                "\t\t\t\t\t<query index=\"0\" name=\"hello\"/>\n" +
                "\t\t\t\t</params>\n" +
                "\t\t\t</method>\n" +
                "\t\t</methods>\n" +
                "\t</service>\n" +
                "</crest-config>";
        XmlDrivenInterfaceConfigFactory factory = newFactory(new ByteArrayInputStream(xml.getBytes()), false);
        factory.newConfig(Interface.class, MOCK_CONTEXT);
    }

    @Test
    public void testMinimalConfig() throws ConfigFactoryException {
        XmlDrivenInterfaceConfigFactory factory = newFactory("minimal-config.xml", false);
        assertMinimalExpected(factory.newConfig(Interface.class, MOCK_CONTEXT), Interface.class);
    }

    @Test
    public void testPartialConfig() throws ConfigFactoryException {
        XmlDrivenInterfaceConfigFactory factory = newFactory("partial-config.xml", false);
        assertPartialExpected(factory.newConfig(Interface.class, MOCK_CONTEXT), Interface.class);
    }

    @Test
    public void testFullConfig() throws ConfigFactoryException {
        XmlDrivenInterfaceConfigFactory factory = newFactory("full-config.xml", false);
        assertFullExpected(factory.newConfig(Interface.class, MOCK_CONTEXT), Interface.class);
    }


    public XmlDrivenInterfaceConfigFactory newFactory(Document doc, boolean templates) {
        return new XmlDrivenInterfaceConfigFactory(doc, templates);
    }

    public XmlDrivenInterfaceConfigFactory newFactory(InputStream is, boolean templates) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            is.close();
            return newFactory(doc, templates);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlDrivenInterfaceConfigFactory newFactory(String n, boolean templates)  {
        return newFactory(getClass().getResourceAsStream(n), templates);
    }

    @Test
    public void testServerConfig() throws IOException, InstantiationException, IllegalAccessException, ConfigFactoryException, SAXException, ParserConfigurationException {
        String serverConfig = "<crest-config end-point=\"hello\">" +
                "    <service class=\"org.codegist.crest.config.XmlDrivenInterfaceConfigFactoryTest$InjectorTestInterface\">\n" +
                "\t\t<methods>\n" +
                "\t\t\t<method match=\"get.*\">\n" +
                "\t\t\t\t<params>\n" +
                "\t\t\t\t\t<query index=\"0\" name=\"hello\"/>\n" +
                "\t\t\t\t</params>\n" +
                "\t\t\t</method>\n" +
                "\t\t</methods>\n" +
                "\t</service>\n" +
                "</crest-config>";
        XmlDrivenInterfaceConfigFactory factory = newFactory(new ByteArrayInputStream(serverConfig.getBytes()), false);
        InterfaceConfig config = factory.newConfig(InjectorTestInterface.class, MOCK_CONTEXT);
        assertEquals("hello", config.getEndPoint());
    }

}
