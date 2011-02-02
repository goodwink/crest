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

import org.codegist.common.lang.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "comment")
public class Comment implements SimplePayload<String> {

    @XmlAttribute
    private String id;
    @XmlAttribute
    private String authorId;
    @XmlAttribute
    private String authorname;
    @XmlAttribute
    private long datecreated;
    @XmlAttribute
    private String permalink;
    @XmlValue
    private String comment;

    public String getValue() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorname() {
        return authorname;
    }

    public Date getDateCreated() {
        return new Date(datecreated);
    }

    public String getPermalink() {
        return permalink;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("authorId", authorId)
                .append("authorName", authorname)
                .append("datecreated", datecreated)
                .append("permalink", permalink)
                .append("comment", comment)
                .toString();
    }
}
