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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "uploader")
public class Uploader implements Payload {
    @XmlElement(name="ticket")
    private Ticket[] tickets;

    public Ticket[] getTickets() {
        return tickets;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tickets", tickets)
                .toString();
    }

    @XmlRootElement(name = "ticket")
    public static class Ticket {
        @XmlAttribute
        private String id;
        @XmlAttribute
        private int complete;
        @XmlAttribute
        private int invalid;
        @XmlAttribute
        private long photoId;

        public String getId() {
            return id;
        }

        public int getComplete() {
            return complete;
        }

        public int getInvalid() {
            return invalid;
        }

        public long getPhotoId() {
            return photoId;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("id", id)
                .append("complete", complete)
                .append("invalid", invalid)
                .append("photoId", photoId)
                .toString();
        }
    }
}
