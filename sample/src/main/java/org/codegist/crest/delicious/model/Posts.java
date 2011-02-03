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

package org.codegist.crest.delicious.model;

import org.codegist.common.lang.EqualsBuilder;
import org.codegist.common.lang.ToStringBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "posts")
public class Posts {
    @XmlAttribute
    private String user;
    @XmlJavaTypeAdapter(ArrayAdapter.class)
    @XmlAttribute(name = "tag")
    private String[] tags;
    @XmlElement(name = "post")
    private Post[] posts;

    public String getUser() {
        return user;
    }

    public String[] getTags() {
        return tags;
    }

    public Post[] getPosts() {
        return posts;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != Posts.class) return false;
        Posts p = (Posts) o;
        return new EqualsBuilder()
                .append(user, p.user)
                .append(tags, p.tags)
                .append(posts, p.posts)
                .equals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("user", user)
                .append("tags", tags)
                .append("posts", posts)
                .toString();
    }
}
