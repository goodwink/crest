/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.serializer;

import org.codegist.common.collect.Maps;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public class JaxbDeserializer implements Deserializer {
    public static final String MODEL_PACKAGE_PROP = JaxbDeserializer.class.getName() + "#model-package";
    public static final String MODEL_FACTORY_PROP = JaxbDeserializer.class.getName() + "#model-factory";
    public static final String USER_JAXB_CONTEXT_PROP = JaxbDeserializer.class.getName() + "#jaxb-context";

    private final JAXBContext jaxbContext;

    public JaxbDeserializer(String modelPackage) throws DeserializerException {
        try {
            this.jaxbContext = JAXBContext.newInstance(modelPackage);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }

    public JaxbDeserializer(Class<?>... factory) throws DeserializerException {
        try {
            this.jaxbContext = JAXBContext.newInstance(factory);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }

    public JaxbDeserializer(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public JaxbDeserializer(Map<String,Object> config) {
        config = Maps.defaultsIfNull(config);
        try {
            if(config.containsKey(MODEL_FACTORY_PROP)) {
                this.jaxbContext = JAXBContext.newInstance((Class) config.get(MODEL_FACTORY_PROP));
            }else if(config.containsKey(MODEL_PACKAGE_PROP)) {
                this.jaxbContext = JAXBContext.newInstance((String) config.get(MODEL_PACKAGE_PROP));
            }else if(config.containsKey(USER_JAXB_CONTEXT_PROP)) {
                this.jaxbContext = (JAXBContext) config.get(USER_JAXB_CONTEXT_PROP);
            }else{
                throw new IllegalArgumentException("Illegal jaxb config");
            }
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }
    
    public <T> T deserialize(Reader reader, Type type) throws DeserializerException {
        try {
            return (T) jaxbContext.createUnmarshaller().unmarshal(reader);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }
}
