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

package org.codegist.crest.flickr.service;

import org.codegist.crest.annotate.*;
import org.codegist.crest.flickr.handler.FlickrResponseHandler;
import org.codegist.crest.flickr.interceptor.FlickrAuthInterceptor;
import org.codegist.crest.flickr.model.Comment;
import org.codegist.crest.flickr.model.Gallery;
import org.codegist.crest.flickr.model.Uploader;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * see http://www.flickr.com/services/api/
 *
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@EndPoint("http://flickr.com")
@ContextPath("/services")
@GlobalInterceptor(FlickrAuthInterceptor.class)
@ResponseHandler(FlickrResponseHandler.class)
@POST
public interface Flickr {

    @GET
    @Path("/rest?method=flickr.blogs.getList")
    String getList();

    @Path("/rest?method=flickr.galleries.create")
    Gallery newGallery(
            @FormParam(name="title") String title,
            @FormParam(name="description") String description);

    @Path("/rest?method=flickr.galleries.create")
    Gallery newGallery(
            @FormParam(name="title") String title,
            @FormParam(name="description") String description,
            @FormParam(name="primary_photo_id") long primaryPhotoId);

    @Path("/rest?method=flickr.galleries.addPhoto&gallery_id={0}&photo_id={1}&comment={2}")
    void addPhotoToGallery(String galleryId, long photoId, String comment);

    @Path("/rest?method=flickr.photos.comments.addComment&photo_id={0}&comment_text={1}")
    String comment(long photoId, String comment);

    @Path("/rest?method=flickr.photos.comments.editComment&comment_id={0}&comment_text={1}")
    void editComment(String commentId, String comment);

    @Path("/rest?method=flickr.photos.comments.getList&photo_id={0}")
    Comment[] getComments(long photoId);

    @Path("/rest?method=flickr.photos.comments.getList&photo_id={0}&min_comment_date={1}&max_comment_date={2}")
    Comment[] getComments(long photoId, Date from, Date to);

    @Path("/rest?method=flickr.photos.comments.deleteComment&comment_id={0}")
    void deleteComment(String commentId);


    @Path("/upload")
    long uploadPhoto(@FormParam(name="photo") File photo);

    @Path("/upload")
    long uploadPhoto(@FormParam(name="photo") InputStream photo);

    @Path("/upload")
    long uploadPhoto(
            @FormParam(name="photo") InputStream photo,
            @FormParam(name="title") String title,
            @FormParam(name="description") String description,
            @FormParam(name="tags") String[] tags,
            @FormParam(name="is_public") boolean isPublic,
            @FormParam(name="is_friend") boolean isFriend,
            @FormParam(name="is_family") boolean isFamily,
            @FormParam(name="safety_level") SafetyLevel safetyLevel,
            @FormParam(name="content_type") ContentType contentLype,
            @FormParam(name="hidden") Visibility searchVisibility
    );


    @Path("/upload")
    @FormParam(name = "async", value = "1")
    String asyncUploadPhoto(@FormParam(name="photo") File photo);

    @Path("/upload")
    @FormParam(name = "async", value = "1")
    String asyncUploadPhoto(@FormParam(name="photo") InputStream photo);

    @Path("/upload")
    @FormParam(name = "async", value = "1")
    String asyncUploadPhoto(
            @FormParam(name="photo") InputStream photo,
            @FormParam(name="title") String title,
            @FormParam(name="description") String description,
            @FormParam(name="tags") String[] tags,
            @FormParam(name="is_public") boolean isPublic,
            @FormParam(name="is_friend") boolean isFriend,
            @FormParam(name="is_family") boolean isFamily,
            @FormParam(name="safety_level") SafetyLevel safetyLevel,
            @FormParam(name="content_type") ContentType contentLype,
            @FormParam(name="hidden") Visibility searchVisibility
    );

    @Path("/rest?method=flickr.photos.upload.checkTickets&tickets={0}")
    Uploader checkUploads(String... tickets);


    @Path("/replace")
    long replacePhoto(
            @FormParam(name="photo") InputStream photo,
            @FormParam(name="photo_id") long photoId
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
