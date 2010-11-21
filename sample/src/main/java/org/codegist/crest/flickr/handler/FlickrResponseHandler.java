package org.codegist.crest.flickr.handler;

import org.codegist.common.marshal.Marshaller;
import org.codegist.common.reflect.Types;
import org.codegist.crest.CRestException;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.ResponseHandler;
import org.codegist.crest.flickr.model.Error;
import org.codegist.crest.flickr.model.Payload;
import org.codegist.crest.flickr.model.Response;
import org.codegist.crest.flickr.model.SimplePayload;

public class FlickrResponseHandler implements ResponseHandler {

    private Marshaller marshaller;

    public final Object handle(ResponseContext context) {
        try {
            /* Get the marshaller, save the ref to avoid accessing the map each time (since custom properties map could get quite big!) */
            if (marshaller == null) {
                marshaller = context.getRequestContext().getCustomProperty(Marshaller.class.getName());
            }
            /* Marshall the response */
            Response res = marshaller.marshall(context.getResponse().asStream(), Types.newType(Response.class, context.getExpectedGenericType()));
            /* Check for twitter OK status */
            if ("ok".equals(res.getStatus())) {
                /* Get the nested payload and returns it */
                Payload payload = res.getPayload();
                if (payload instanceof SimplePayload) {
                    return ((SimplePayload) payload).getValue();
                } else {
                    return payload;
                }
            } else {
                if (res.getPayload() instanceof Error) {
                    /* Status is not OK, try to get the error cause */
                    Error error = ((Error) res.getPayload());
                    throw new CRestException(error.getMsg() + " (code=" + error.getCode() + ")");
                } else {
                    /* Response format is not the one expected. */
                    throw new CRestException("Unkown error");
                }
            }
        } finally {
            context.getResponse().close();
        }
    }


}
