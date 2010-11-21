package org.codegist.crest.flickr.model;

import javax.xml.bind.annotation.XmlRegistry;
import java.lang.*;
import java.lang.Error;

@XmlRegistry
public class TwitterModelFactory {
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
