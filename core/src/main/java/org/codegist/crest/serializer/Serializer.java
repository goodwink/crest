package org.codegist.crest.serializer;

import org.codegist.crest.ParamContext;

/**
 * Parameter serialize can be used to serialize a object to a single String.
 */
public interface Serializer {
    /**
     * Serialize the current arg context into a single string
     *
     * @param context Context of the argument
     * @return serialized version of the argument
     */
    String serialize(ParamContext context);
}
