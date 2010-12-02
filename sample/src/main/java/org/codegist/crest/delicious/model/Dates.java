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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dates")
public class Dates {
    @XmlAttribute(name="tag")
    @XmlJavaTypeAdapter(ArrayAdapter.class)
    private String[] tags;
    @XmlAttribute
    private String user;
    @XmlElement(name = "date")
    private Date[] dates;

    public String[] getTags() {
        return tags;
    }

    public String getUser() {
        return user;
    }

    public Date[] getDates() {
        return dates;
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
