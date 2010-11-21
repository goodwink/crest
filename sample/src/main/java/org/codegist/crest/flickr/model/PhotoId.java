package org.codegist.crest.flickr.model;


import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "photoid")
public class PhotoId implements SimplePayload<Long> {

    @XmlValue()
    private long value;

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
