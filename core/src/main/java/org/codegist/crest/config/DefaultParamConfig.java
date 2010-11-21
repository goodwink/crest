package org.codegist.crest.config;

import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.serializer.Serializer;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.DefaultParamConfig}
 */
class DefaultParamConfig implements ParamConfig {

    private final String name;
    private final Destination dest;
    private final Serializer serializer;
    private final RequestInjector injector;

    DefaultParamConfig(String name, Destination dest, Serializer serializer, RequestInjector injector) {
        this.name = name;
        this.dest = dest;
        this.serializer = serializer;
        this.injector = injector;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Destination getDestination() {
        return dest;
    }

    @Override
    public Serializer getSerializer() {
        return serializer;
    }

    @Override
    public RequestInjector getInjector() {
        return injector;
    }


    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("dest", dest)
                .append("serializer", serializer)
                .append("injector", injector)
                .toString();
    }
}
