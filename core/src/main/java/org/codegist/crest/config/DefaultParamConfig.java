/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.serializer.Serializer;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.DefaultParamConfig}
 * @author Laurent Gilles (laurent.gilles@codegist.org)
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
