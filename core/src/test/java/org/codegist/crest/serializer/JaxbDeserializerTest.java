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

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author laurent.gilles@codegist.org
 */
public class JaxbDeserializerTest {

    private static Deserializer getDeserializer(final long unmarshalTime, int poolSize, long maxWait) throws JAXBException {
        Unmarshaller unmarshaller = mock(Unmarshaller.class);
        when(unmarshaller.unmarshal(any(Reader.class))).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Thread.sleep(unmarshalTime);
                return "hello";
            }
        });

        JAXBContext ctx = mock(JAXBContext.class);
        when(ctx.createUnmarshaller()).thenReturn(unmarshaller);


        return new JaxbDeserializer(ctx, poolSize, maxWait);
    }

    @Test
    public void testNoTimeout() throws JAXBException, InterruptedException {
        final Deserializer deserializer = getDeserializer(1000, 2, 10000);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        final AtomicBoolean timedOut = new AtomicBoolean(true);
        executorService.submit(new Runnable() {
            public void run() {
                assertEquals("hello", deserializer.deserialize(null, null));
            }
        });
        executorService.submit(new Runnable() {
            public void run() {
                assertEquals("hello", deserializer.deserialize(null, null));
            }
        });
        Thread.sleep(100);
        executorService.submit(new Runnable() {
            public void run() {
                assertEquals("hello", deserializer.deserialize(null, null));
                timedOut.set(false);
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(20l, TimeUnit.SECONDS);

        assertFalse("Shouldn't have timed out !", timedOut.get());
    }
    @Test
    public void testTimeout() throws JAXBException, InterruptedException {
        final Deserializer deserializer = getDeserializer(1000, 2, 10);
        final AtomicBoolean timedOut = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(new Runnable() {
            public void run() {
                assertEquals("hello", deserializer.deserialize(null, null));
            }
        });
        executorService.submit(new Runnable() {
            public void run() {
                assertEquals("hello", deserializer.deserialize(null, null));
            }
        });
        Thread.sleep(100);
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    deserializer.deserialize(null, null);
                    timedOut.set(false);
                } catch (DeserializerException e) {
                    timedOut.set(true);
                }
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(20l, TimeUnit.SECONDS);
        assertTrue("Should have timed out !", timedOut.get());
    }
}
