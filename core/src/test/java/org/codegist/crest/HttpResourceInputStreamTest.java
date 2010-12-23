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

package org.codegist.crest;

import org.codegist.common.io.IOs;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class HttpResourceInputStreamTest {
    @Test
    public void testReadDelegateAndReleaseWrappedInReader() throws IOException {
        InputStream spy = spy(new ByteArrayInputStream("data\n".getBytes()));
        HttpResource resource = mock(HttpResource.class);
        when(resource.getContent()).thenReturn(spy);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new HttpResourceInputStream(resource)));
        String data = reader.readLine();
        reader.close();// close it
        reader.close();// close it again!
        assertEquals("data", data);
        verify(resource).release(); // check release has been called just once
        verify(spy).close(); // check that underlying inputstream.close has been called just once
    }

    @Test
    public void testReadDelegateAndNoRelease() throws IOException {
        InputStream spy = spy(new ByteArrayInputStream("data".getBytes()));
        HttpResource resource = mock(HttpResource.class);
        when(resource.getContent()).thenReturn(spy);

        HttpResourceInputStream stream = new HttpResourceInputStream(resource);
        String data = IOs.toString(stream); // read all
        assertEquals("data", data);
        verify(resource, never()).release();
        verify(spy, never()).close();
    }

    @Test
    public void testReadDelegateAndRelease() throws IOException {
        InputStream spy = spy(new ByteArrayInputStream("data".getBytes()));
        HttpResource resource = mock(HttpResource.class);
        when(resource.getContent()).thenReturn(spy);

        HttpResourceInputStream stream = new HttpResourceInputStream(resource);
        String data = IOs.toString(stream); // read all
        stream.close();// close it
        stream.close();// close it again!
        assertEquals("data", data);
        verify(resource).release(); // check release has been called just once
        verify(spy).close(); // check that underlying inputstream.close has been called just once
    }
}
