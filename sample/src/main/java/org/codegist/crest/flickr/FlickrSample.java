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

package org.codegist.crest.flickr;

import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.flickr.interceptor.FlickrAuthInterceptor;
import org.codegist.crest.flickr.model.FlickrModelFactory;
import org.codegist.crest.flickr.model.Gallery;
import org.codegist.crest.flickr.service.Flickr;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class FlickrSample {

    public static void main(String[] args) {
        String apiKey = args[0];
        String appSecret = args[1];
        String authToken = args[2];

        /* Get the factory */
        CRest crest = new CRestBuilder()
                .expectsXml(FlickrModelFactory.class)
                .addCustomProperty(FlickrAuthInterceptor.API_KEY_PROP, apiKey)
                .addCustomProperty(FlickrAuthInterceptor.APP_SECRET_PROP, appSecret)
                .addCustomProperty(FlickrAuthInterceptor.AUTH_TOKEN_PROP, authToken)
                .build();

        /* Build service instance */
        Flickr flickr = crest.build(Flickr.class);

        /* Use it! */
        long photoId = flickr.uploadPhoto(FlickrSample.class.getResourceAsStream("photo1.jpg"));
        Gallery gallery = flickr.newGallery("My Gallery Title", "My Gallery Desc", photoId);

        System.out.println("photoId=" + photoId);
        System.out.println("gallery=" + gallery);
    }

}
