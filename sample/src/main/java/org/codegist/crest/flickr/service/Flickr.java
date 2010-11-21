package org.codegist.crest.flickr.service;

import org.codegist.crest.HttpMethod;
import org.codegist.crest.annotate.RestApi;
import org.codegist.crest.annotate.RestMethod;
import org.codegist.crest.annotate.RestParam;
import org.codegist.crest.flickr.handler.FlickrResponseHandler;
import org.codegist.crest.flickr.interceptor.FlickrAuthInterceptor;
import org.codegist.crest.flickr.model.Comment;
import org.codegist.crest.flickr.model.Gallery;
import org.codegist.crest.flickr.serializer.FlickParamSerializer;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * see http://www.flickr.com/services/api/
 */
@RestApi(
        endPoint = "http://flickr.com", path = "/services",
        requestInterceptor = FlickrAuthInterceptor.class,
        paramsSerializer = FlickParamSerializer.class,
        methodsResponseHandler = FlickrResponseHandler.class,
        methodsHttpMethod = HttpMethod.POST
)
public interface Flickr {

    @RestMethod(method = "GET", path = "/rest?method=flickr.blogs.getList")
    String getList();

    @RestMethod(path = "/rest?method=flickr.galleries.create")
    Gallery newGallery(
            @RestParam(name = "title") String title,
            @RestParam(name = "description") String description);

    @RestMethod(path = "/rest?method=flickr.galleries.create")
    Gallery newGallery(
            @RestParam(name = "title") String title,
            @RestParam(name = "description") String description,
            @RestParam(name = "primary_photo_id") long primaryPhotoId);

    @RestMethod(path = "/rest?method=flickr.galleries.addPhoto&gallery_id={0}&photo_id={1}&comment={2}")
    void addPhotoToGallery(String galleryId, long photoId, String comment);

    @RestMethod(path = "/rest?method=flickr.photos.comments.addComment&photo_id={0}&comment_text={1}")
    String comment(long photoId, String comment);

    @RestMethod(path = "/rest?method=flickr.photos.comments.editComment&comment_id={0}&comment_text={1}")
    void editComment(String commentId, String comment);

    @RestMethod(path = "/rest?method=flickr.photos.comments.getList&photo_id={0}")
    Comment[] getComments(long photoId);

    @RestMethod(path = "/rest?method=flickr.photos.comments.getList&photo_id={0}&min_comment_date={1}&max_comment_date={2}")
    Comment[] getComments(long photoId, Date from, Date to);

    @RestMethod(path = "/rest?method=flickr.photos.comments.deleteComment&comment_id={0}")
    void deleteComment(String commentId);


    @RestMethod(path = "/upload", paramsDestination = "BODY")
    long uploadPhoto(@RestParam(name = "photo") File photo);

    @RestMethod(path = "/upload", paramsDestination = "BODY")
    long uploadPhoto(@RestParam(name = "photo") InputStream photo);

    @RestMethod(path = "/upload", paramsDestination = "BODY")
    long uploadPhoto(
            @RestParam(name = "photo") InputStream photo,
            @RestParam(name = "title") String title,
            @RestParam(name = "description") String description,
            @RestParam(name = "tags") String[] tags,
            @RestParam(name = "is_public") boolean isPublic,
            @RestParam(name = "is_friend") boolean isFriend,
            @RestParam(name = "is_family") boolean isFamily,
            @RestParam(name = "safety_level") SafetyLevel safetyLevel,
            @RestParam(name = "content_type") ContentType contentLype,
            @RestParam(name = "hidden") Visibility searchVisibility
    );


    @RestMethod(path = "/replace", paramsDestination = "BODY")
    long replacePhoto(
            @RestParam(name = "photo") InputStream photo,
            @RestParam(name = "photo_id") long photoId
    );


    enum Visibility {
        Public(1), Private(2);
        private final int lvl;

        Visibility(int lvl) {
            this.lvl = lvl;
        }

        public String toString() {
            return String.valueOf(lvl);
        }
    }

    enum SafetyLevel {
        Safe(1), Moderate(2), Restricted(3);
        private final int lvl;

        SafetyLevel(int lvl) {
            this.lvl = lvl;
        }

        public String toString() {
            return String.valueOf(lvl);
        }
    }

    enum ContentType {
        Photo(1), Screenshot(2), Other(3);
        private final int lvl;

        ContentType(int lvl) {
            this.lvl = lvl;
        }

        public String toString() {
            return String.valueOf(lvl);
        }
    }
}
