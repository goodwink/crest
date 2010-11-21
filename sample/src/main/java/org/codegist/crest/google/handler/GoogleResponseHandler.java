package org.codegist.crest.google.handler;

import org.codegist.common.marshal.Marshaller;
import org.codegist.common.reflect.Types;
import org.codegist.crest.CRestException;
import org.codegist.crest.ResponseContext;
import org.codegist.crest.ResponseHandler;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class GoogleResponseHandler implements ResponseHandler {

    private Marshaller marshaller;

    public final Object handle(ResponseContext context) {
        try {
            /* Get the marshaller, save the ref to avoid accessing the map each time (since custom properties map could get quite big!) */
            if (marshaller == null) {
                marshaller = context.getRequestContext().getCustomProperty(Marshaller.class.getName());
            }

            /* Marshall the response */
            Response<?> res = marshaller.marshall(context.getResponse().asStream(), Types.newType(Response.class, context.getExpectedGenericType()));
            /* Check for google OK status */
            if (res.status == 200) {
                return res.data; /* Returns the nested payload */
            } else {
                throw new CRestException(res.details + " (status=" + res.status + ")"); /* Throw exception with google error details */
            }
        } finally {
            context.getResponse().close();
        }
    }

    static class Response<T> {
        final T data;
        final String details;
        final int status;

        @JsonCreator
        Response(
                @JsonProperty("responseData") T data,
                @JsonProperty("responseDetails") String details,
                @JsonProperty("responseStatus") int status) {
            this.data = data;
            this.details = details;
            this.status = status;
        }
    }
}
