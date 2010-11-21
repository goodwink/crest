package org.codegist.crest.flickr.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "comments")
public class Comments implements SimplePayload<Comment[]> {

    @XmlElement(name = "comment")
    private Comment[] comments;

    public Comment[] getValue() {
        return comments;
    }
}
