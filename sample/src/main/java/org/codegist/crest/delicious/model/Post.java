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

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "post")
public class Post {
    @XmlAttribute
    private String href;
    @XmlAttribute
    private String hash;
    @XmlAttribute
    private String meta;
    @XmlAttribute
    private String description;
    @XmlAttribute(name="tag")
    @XmlJavaTypeAdapter(ArrayAdapter.class)
    private String[] tags;
    @XmlAttribute
    private Date time;
    @XmlAttribute
    private String extended;
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    private Boolean shared;

    public String getHref() {
        return href;
    }

    public String getHash() {
        return hash;
    }

    public String getMeta() {
        return meta;
    }

    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    public Date getTime() {
        return time;
    }

    public String getExtended() {
        return extended;
    }

    public boolean isShared() {
        return shared;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
