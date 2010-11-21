package org.codegist.crest.flickr.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rsp")
public class Response<T extends Payload> {

    @XmlAttribute(name = "stat")
    private String status;

    @XmlAnyElement(lax = true)
    private T payload;

    public String getStatus() {
        return status;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
