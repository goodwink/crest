package org.codegist.crest.serializer;

import org.codegist.crest.ParamContext;

/**
 * Simple serializer that returns the toString() value of the given value or empty string if null.
 */
public class ToStringParamSerializer implements Serializer {
    /**
     * Simple serializer that returns the toString() value of the given value or empty string if null.
     *
     * @param context Context of the serialization
     * @return serialized value of param
     */
    @Override
    public String serialize(ParamContext context) {
        return context.getArgValue() != null ? context.getArgValue().toString() : "";
    }
}
