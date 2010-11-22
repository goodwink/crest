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

package org.codegist.crest.flickr.model;

import javax.xml.bind.annotation.XmlRegistry;
import java.lang.*;
import java.lang.Error;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@XmlRegistry
public class FlickrModelFactory {
    public <T extends Payload> Response<T> createResponse() {
        return new Response<T>();
    }

    public TicketId createTicketId() {
        return new TicketId();
    }

    public PhotoId createPhotoId() {
        return new PhotoId();
    }

    public java.lang.Error createError() {
        return new Error();
    }

    public Gallery createGallery() {
        return new Gallery();
    }

    public Comment createComment() {
        return new Comment();
    }

    public Comments createComments() {
        return new Comments();
    }
}
