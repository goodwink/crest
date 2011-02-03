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

package org.codegist.crest.flickr;

import org.codegist.common.lang.Randoms;
import org.codegist.crest.CRestBuilder;
import org.codegist.crest.flickr.interceptor.FlickrAuthInterceptor;
import org.codegist.crest.flickr.model.Comment;
import org.codegist.crest.flickr.model.FlickrModelFactory;
import org.codegist.crest.flickr.model.Gallery;
import org.codegist.crest.flickr.model.Uploader;
import org.codegist.crest.flickr.service.Flickr;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author laurent.gilles@codegist.org
 */
@Ignore
public abstract class AbstractFlickrIntegrationTest {

    private static final File SAMPLE_1;
    static {
        try {
            SAMPLE_1 = new File(AbstractFlickrIntegrationTest.class.getResource("photo1.jpg").toURI());
        } catch (URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    private final Flickr flickr;

    protected AbstractFlickrIntegrationTest(Flickr flickr) {
        this.flickr = flickr;
    }

    @Test
    public void testGalleries(){
        long id = flickr.uploadPhoto(SAMPLE_1);
        assertTrue(id > 0);

        String title = Randoms.randomAlphaNumeric(5);
        String desc = "that's my desc!";
        Gallery gallery = flickr.newGallery(title, desc, id);
        assertNotNull(gallery);

        title = Randoms.randomAlphaNumeric(5);
        desc = Randoms.randomAlphaNumeric(5);
        gallery = flickr.newGallery(title, desc);
        assertNotNull(gallery);

        flickr.deletePhoto(id);
    }

    @Test
    public void testComments() throws InterruptedException {
        long photoId = flickr.uploadPhoto(SAMPLE_1);
        assertTrue(photoId > 0);

        String commentId = flickr.comment(photoId, "That's my comment! ");
        assertNotNull(commentId);

        Thread.sleep(2000);// let it propagate...
        Comment[] comments = flickr.getComments(photoId);
        assertNotNull(comments);
        assertEquals(1, comments.length);
        assertEquals("That's my comment!", comments[0].getComment());

        flickr.editComment(comments[0].getId(), "my comment edited");

        Thread.sleep(2000);// let it propagate...
        comments = flickr.getComments(photoId);
        assertNotNull(comments);
        assertEquals(1, comments.length);
        assertEquals("my comment edited", comments[0].getComment());


        commentId = flickr.comment(photoId, "That's another comment!");
        assertNotNull(commentId);

        Thread.sleep(2000);// let it propagate...
        comments = flickr.getComments(photoId, new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24), new Date());
        assertNotNull(comments);
        assertEquals(2, comments.length);

        for(Comment comment : comments){
            flickr.deleteComment(comment.getId());
        }

        Thread.sleep(2000);// let it propagate...
        assertNull(flickr.getComments(photoId));

        flickr.deletePhoto(photoId);
    }

    @Test
    public void testPhotos() throws InterruptedException, FileNotFoundException {
        long photoId = flickr.uploadPhoto(SAMPLE_1);
        assertTrue(photoId > 0);
        flickr.deletePhoto(photoId);

        photoId = flickr.uploadPhoto(new FileInputStream(SAMPLE_1));
        assertTrue(photoId > 0);
        flickr.deletePhoto(photoId);

        photoId = flickr.uploadPhoto(new FileInputStream(SAMPLE_1),
                "my title",
                "my dest",
                new String[]{"tag","tag"},
                true,
                false,
                false,
                Flickr.SafetyLevel.Moderate,
                Flickr.ContentType.Photo,
                Flickr.Visibility.Public);
        assertTrue(photoId > 0);
        flickr.deletePhoto(photoId);      //todo should map getPhotoInfo and compare results...

        String[] tickets = {
                flickr.asyncUploadPhoto(SAMPLE_1),
                flickr.asyncUploadPhoto(new FileInputStream(SAMPLE_1)),
                flickr.asyncUploadPhoto(new FileInputStream(SAMPLE_1),
                    "my title",
                    "my dest",
                    new String[]{"tag","tag"},
                    true,
                    false,
                    false,
                    Flickr.SafetyLevel.Moderate,
                    Flickr.ContentType.Photo,
                    Flickr.Visibility.Public)
        };
        List<Uploader.Ticket> completes = new ArrayList<Uploader.Ticket>();
        List<Uploader.Ticket> failed = new ArrayList<Uploader.Ticket>();

        int max = 10, attempt=0;
        do {
            List ticketsList = new ArrayList(Arrays.asList(tickets));
            Uploader uploader = flickr.checkUploads(tickets);
            for(Uploader.Ticket ticket: uploader.getTickets()){
                if(ticket.isComplete()){
                    completes.add(ticket);
                    ticketsList.remove(ticket.getId());
                }else if(ticket.hasFailed()){
                    failed.add(ticket);
                    ticketsList.remove(ticket.getId());
                }
            }
            tickets = (String[]) ticketsList.toArray(new String[ticketsList.size()]);
            Thread.sleep(2000);
        }while(tickets.length > 0 && attempt++ < max);

        assertEquals(3, completes.size() + failed.size());

        for(Uploader.Ticket tick : completes){
            flickr.deletePhoto(tick.getPhotoId());
        }
        for(Uploader.Ticket tick : failed){
            flickr.deletePhoto(tick.getPhotoId());
        }
        


    }

    protected static CRestBuilder getBaseCRestBuilder() {
        final String apiKey = System.getProperty("crest.sample.flickr.key");
        final String appSecret = System.getProperty("crest.sample.flickr.secret");
        final String authToken = System.getProperty("crest.sample.flickr.token");

        return new CRestBuilder()
                .useHttpClientRestService()
                .expectsXml(FlickrModelFactory.class)
                .setDateSerializerFormat("Seconds")
                .setBooleanSerializer("1", "0")
                .setProperty(FlickrAuthInterceptor.API_KEY_PROP, apiKey)
                .setProperty(FlickrAuthInterceptor.APP_SECRET_PROP, appSecret)
                .setProperty(FlickrAuthInterceptor.AUTH_TOKEN_PROP, authToken);
    }
}
