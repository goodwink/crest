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
import org.codegist.common.lang.Objects;
import org.codegist.crest.CRestProperty;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author laurent.gilles@codegist.org
 */
public class JaxbDeserializer implements Deserializer {
    public static final String JAXB_UNMARSHALLER_POOL_RETRIEVAL_MAX_WAIT_PROP = JaxbDeserializer.class.getName() + "#jaxb-unmarshaller-pool.retrieval-max-wait";
    public static final String MODEL_CONTEXT_PATH_PROP = JaxbDeserializer.class.getName() + "#model-context-path";
    public static final String MODEL_CLASSES_BOUND_PROP = JaxbDeserializer.class.getName() + "#model-classes-bound";
    public static final String USER_JAXB_CONTEXT_PROP = JaxbDeserializer.class.getName() + "#jaxb-context";

    public static final Long DEFAULT_MAX_WAIT = 30000l;
    public static final Integer DEFAULT_POOL_SIZE = 1;

    private final JaxbUnmarshaller unmarshaller;

    public <T> T deserialize(Reader reader, Type type) throws DeserializerException {
        return unmarshaller.<T>unmarshal(reader);
    }

    public JaxbDeserializer(String modelPackage) throws DeserializerException {
        this(createJAXB(modelPackage));
    }

    public JaxbDeserializer(Class<?>... classesToBeBound) throws DeserializerException {
        this(createJAXB(classesToBeBound));
    }

    public JaxbDeserializer(JAXBContext jaxbContext) {
        this(jaxbContext, DEFAULT_POOL_SIZE, DEFAULT_MAX_WAIT);
    }

    public JaxbDeserializer(JAXBContext jaxbContext, int poolSize, long maxWait) {
        this.unmarshaller = JaxbUnmarshaller.newInstance(jaxbContext, poolSize, maxWait);
    }

    public JaxbDeserializer(Map<String, Object> config) {
        config = Maps.defaultsIfNull(config);

        JAXBContext jaxb;
        if (config.containsKey(MODEL_CLASSES_BOUND_PROP)) {
            jaxb = createJAXB((Class<?>[]) config.get(MODEL_CLASSES_BOUND_PROP));
        } else if (config.containsKey(MODEL_CONTEXT_PATH_PROP)) {
            jaxb = createJAXB((String) config.get(MODEL_CONTEXT_PATH_PROP));
        } else if (config.containsKey(USER_JAXB_CONTEXT_PROP)) {
            jaxb = (JAXBContext) config.get(USER_JAXB_CONTEXT_PROP);
        } else {
            throw new IllegalArgumentException("Illegal jaxb config");
        }
        int poolSize = Objects.defaultIfNull((Integer) config.get(CRestProperty.CREST_CONCURRENCY_LEVEL), DEFAULT_POOL_SIZE);
        long maxWait = Objects.defaultIfNull((Long) config.get(JAXB_UNMARSHALLER_POOL_RETRIEVAL_MAX_WAIT_PROP), DEFAULT_MAX_WAIT);

        this.unmarshaller = JaxbUnmarshaller.newInstance(jaxb, poolSize, maxWait);
    }

    private static JAXBContext createJAXB(String contextPath) {
        try {
            return JAXBContext.newInstance(contextPath);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }

    private static JAXBContext createJAXB(Class<?>... classToBeBound) {
        try {
            return JAXBContext.newInstance(classToBeBound);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }


}


abstract class JaxbUnmarshaller {
    abstract <T> T unmarshal(Reader reader);

    static JaxbUnmarshaller newInstance(JAXBContext jaxb, int poolSize, long maxWait){
        if(poolSize == 1) {
            return new DefaultJaxbUnmarshaller(jaxb);
        }else{
            return new PooledJaxbUnmarshaller(jaxb, poolSize, maxWait);
        }
    }

}
class DefaultJaxbUnmarshaller extends JaxbUnmarshaller {

    private final Unmarshaller unmarshaller;

    public DefaultJaxbUnmarshaller(JAXBContext jaxbContext) {
        try {
            this.unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }
    public <T> T unmarshal(Reader reader) {
        try {
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        }
    }
}
class PooledJaxbUnmarshaller extends JaxbUnmarshaller {
    private final BlockingQueue<Unmarshaller> unmarshallerPool;
    private final long maxWait;

    public PooledJaxbUnmarshaller(JAXBContext jaxbContext, int poolSize, long maxWait) {
        this.maxWait = maxWait;
        this.unmarshallerPool = new ArrayBlockingQueue<Unmarshaller>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            try {
                this.unmarshallerPool.add(jaxbContext.createUnmarshaller());
            } catch (JAXBException e) {
                throw new DeserializerException(e);
            }
        }
    }

    public <T> T unmarshal(Reader reader) {
        Unmarshaller unmarshaller = get();
        try {
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new DeserializerException(e);
        } finally {
            put(unmarshaller);
        }
    }

    private void put(Unmarshaller unmarshaller) {
        unmarshallerPool.offer(unmarshaller);
    }

    private Unmarshaller get() {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = unmarshallerPool.poll(maxWait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DeserializerException(e);
        }
        if (unmarshaller == null)
            throw new DeserializerException("No unmarshaller could have been retrieved in the allowed time window");
        return unmarshaller;
    }
}