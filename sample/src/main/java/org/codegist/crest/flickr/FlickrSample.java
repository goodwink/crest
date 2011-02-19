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

import org.codegist.common.log.Logger;
import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.flickr.interceptor.FlickrAuthInterceptor;
import org.codegist.crest.flickr.model.Gallery;
import org.codegist.crest.flickr.model.Uploader;
import org.codegist.crest.flickr.service.Flickr;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class FlickrSample implements Runnable {

    private static final Logger LOG = Logger.getLogger(FlickrSample.class);

    private final String apiKey;
    private final String appSecret;
    private final String authToken;

    public FlickrSample(String apiKey, String appSecret, String authToken) {
        this.apiKey = apiKey;
        this.appSecret = appSecret;
        this.authToken = authToken;
    }

    public void run() {
        /* Get the factory */
        CRest crest = new CRestBuilder()
                .useHttpClientRestService()
                .consumesXml().handledByJaxb()
                .setDateSerializerFormat("Seconds")
                .setBooleanSerializer("1", "0")
                .setProperty(FlickrAuthInterceptor.API_KEY_PROP, apiKey)
                .setProperty(FlickrAuthInterceptor.APP_SECRET_PROP, appSecret)
                .setProperty(FlickrAuthInterceptor.AUTH_TOKEN_PROP, authToken)
                .build();

        /* Build service instance */
        Flickr flickr = crest.build(Flickr.class);

        /* Use it! */
        long photoId = flickr.uploadPhoto(FlickrSample.class.getResourceAsStream("photo1.jpg"));
        String ticketId = flickr.asyncUploadPhoto(FlickrSample.class.getResourceAsStream("photo1.jpg"));
        Uploader upload = flickr.checkUploads(ticketId);
        Gallery gallery = flickr.newGallery("My Gallery Title", "My Gallery Desc", photoId);

        LOG.info("photoId=" + photoId);
        LOG.info("ticketId=" + ticketId);
        LOG.info("upload=" + upload);
        LOG.info("gallery=" + gallery);
    }

    public static void main(String[] args) {
        new FlickrSample(args[0], args[1], args[2]).run();
    }

}
