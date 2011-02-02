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

import org.codegist.common.lang.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tags")
public class Tags implements Iterable<Tag> {

    @XmlElement(name = "tag")
    private Tag[] tags;

    public Tag[] getTags() {
        return tags;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tags", tags)
                .toString();
    }
    
    public Iterator<Tag> iterator() {
        return new Iterator<Tag>(){
            int i = 0;
            public boolean hasNext() {
                return i < tags.length;
            }
            public Tag next() {
                return tags[i++];
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
