package org.codegist.crest.flickr.serializer;

import org.codegist.crest.ParamContext;
import org.codegist.crest.serializer.DefaultSerializer;
import org.codegist.crest.serializer.Serializer;

import java.util.Date;

public class FlickParamSerializer implements Serializer {

    private final Serializer serializer = new DefaultSerializer(" ");

    public String serialize(ParamContext context) {
        Object o = context.getArgValue();
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return ((Boolean) o) ? "1" : "0";
        } else if (o instanceof Date) {
            return String.valueOf(((Date) o).getTime());
        } else {
            return serializer.serialize(context);
        }
    }
}
