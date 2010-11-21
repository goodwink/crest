package org.codegist.crest.flickr;

import org.codegist.crest.CRest;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.flickr.interceptor.FlickrAuthInterceptor;
import org.codegist.crest.flickr.model.Gallery;
import org.codegist.crest.flickr.model.TwitterModelFactory;
import org.codegist.crest.flickr.service.Flickr;

public class FlickrSample {

    public static void main(String[] args) {
        String apiKey = args[0];
        String appSecret = args[1];
        String authToken = args[2];

        /* Get the factory */
        CRest crest = new CRestBuilder()
                .expectsXml(TwitterModelFactory.class)
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
