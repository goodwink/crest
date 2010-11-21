package org.codegist.crest.twitter.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

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
        return ToStringBuilder.reflectionToString(this);
    }
}
