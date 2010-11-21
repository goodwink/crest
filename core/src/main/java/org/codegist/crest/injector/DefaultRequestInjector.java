package org.codegist.crest.injector;

import org.codegist.common.lang.Strings;
import org.codegist.crest.HttpRequest;
import org.codegist.crest.ParamContext;
import org.codegist.crest.Params;

/**
 * Default request injector used by CRest.
 */
public class DefaultRequestInjector implements RequestInjector {


    /**
     * <p> Serialize the given parameter using its preconfigured serializer and inject the result either :
     * <p> * as a new query string parameter if no name is provided and the parameter is meant to be used as a {@link org.codegist.crest.config.Destination#URL} parameter
     * <p> * merged in the request placeholder if a name is provided and the parameter is meant to be used as a {@link org.codegist.crest.config.Destination#URL} parameter
     * <p> * as a body parameter with or without name. No more than one body parameter can be added without name.
     * <p> If no serialized has been specified for the current param then see the default serializer documentation {@link org.codegist.crest.config.ParamConfig#DEFAULT_SERIALIZER}
     *
     * @param builder The current request beeing build
     * @param context The current method parameter being injected.
     * @see org.codegist.crest.config.ParamConfig#DEFAULT_SERIALIZER
     */
    @Override
    public void inject(HttpRequest.Builder builder, ParamContext context) {
        if (Params.isForUpload(context.getArgValue())) {
            // add it raw
            builder.addBodyParam(context.getParamConfig().getName(), context.getArgValue());
        } else {
            String paramValue = context.getArgSerialized();
            if (Strings.isBlank(paramValue)) return;

            if (context.isForUrl()) {
                if (Strings.isBlank(context.getParamConfig().getName())) {
                    builder.replacePlaceholderInUri(context.getIndex(), paramValue);
                } else {
                    builder.addQueryParam(context.getParamConfig().getName(), paramValue);
                }
            } else {
                // Can safely add it
                builder.addBodyParam(context.getParamConfig().getName(), paramValue);
            }
        }
    }
}
