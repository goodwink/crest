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

package org.codegist.crest.twitter.model;

import org.codegist.common.lang.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class Message {
    @JsonProperty("id")
    private long id;
    //    private Date createdAt;
    @JsonProperty("text")
    private String text;
    @JsonProperty("sender")
    private User sender;
    @JsonProperty("recipient_id")
    private long recipientId;
    @JsonProperty("recipient_screen_name")
    private String recipientScreenName;
    @JsonProperty("recipient")
    private User recipient;
    @JsonProperty("sender_id")
    private long senderId;
    @JsonProperty("sender_screen_name")
    private String senderScreenName;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientScreenName() {
        return recipientScreenName;
    }

    public void setRecipientScreenName(String recipientScreenName) {
        this.recipientScreenName = recipientScreenName;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getSenderScreenName() {
        return senderScreenName;
    }

    public void setSenderScreenName(String senderScreenName) {
        this.senderScreenName = senderScreenName;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("text", text)
                .append("sender", sender)
                .append("recipientId", recipientId)
                .append("recipientScreenName", recipientScreenName)
                .append("recipient", recipient)
                .append("senderId", senderId)
                .append("senderScreenName", senderScreenName)
                .toString();
    }
}
