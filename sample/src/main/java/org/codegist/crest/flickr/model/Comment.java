package org.codegist.crest.flickr.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "comment")
public class Comment implements SimplePayload<String> {

    @XmlAttribute
    private String id;
    @XmlAttribute
    private String authorId;
    @XmlAttribute
    private String authorname;
    @XmlAttribute
    private long datecreated;
    @XmlAttribute
    private String permalink;
    @XmlValue
    private String comment;

    public String getValue() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorname() {
        return authorname;
    }

    public Date getDateCreated() {
        return new Date(datecreated);
    }

    public String getPermalink() {
        return permalink;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
