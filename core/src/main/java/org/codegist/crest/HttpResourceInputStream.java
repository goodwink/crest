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

import org.codegist.common.io.InputStreamWrapper;
import org.codegist.common.log.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * InputStream backed by a HttpResource object.
 * <p>On close, this input stream release underlying http network resources
 * @see HttpResource
 * @see org.codegist.crest.HttpResource#release()
 */
public class HttpResourceInputStream extends InputStreamWrapper {

    private static final Logger LOGGER = Logger.getLogger(HttpResourceInputStream.class);
    private final HttpResource resource;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public HttpResourceInputStream(HttpResource resource) throws HttpException {
        super(resource.getContent());
        this.resource = resource;
    }

    @Override
    public void close() throws IOException {
        if(!closed.compareAndSet(false, true)) {
            LOGGER.trace("This http stream has already been closed, ignoring request.");
            return;
        }
        try {
            super.close();
        } finally {
            LOGGER.debug("Releasing underlying network resources.");
            resource.release();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            LOGGER.trace("Finalizing...");
            close();
        } finally {
            super.finalize();
        }
    }
}
