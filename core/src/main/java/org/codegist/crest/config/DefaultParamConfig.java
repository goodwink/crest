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

import org.codegist.common.lang.EqualsBuilder;
import org.codegist.common.lang.HashCodeBuilder;
import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.serializer.Serializer;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.DefaultParamConfig}
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
class DefaultParamConfig extends DefaultBasicParamConfig implements ParamConfig {

    private final Serializer serializer;
    private final Injector injector;

    DefaultParamConfig(BasicParamConfig base, Serializer serializer, Injector injector) {
        this(base.getName(), base.getDefaultValue(), base.getDestination(), serializer, injector);
    }
    DefaultParamConfig(String name, String defaultValue, Destination destination, Serializer serializer, Injector injector) {
        super(name, defaultValue, destination);
        this.serializer = serializer;
        this.injector = injector;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public Injector getInjector() {
        return injector;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultParamConfig that = (DefaultParamConfig) o;

        return new EqualsBuilder()
                .appendSuper(true)
                .append(serializer, that.serializer)
                .append(injector, that.injector)
                .equals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(serializer)
                .append(injector)
                .hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .append("defaultValue", getDefaultValue())
                .append("destination", getDestination())
                .append("serializer", serializer)
                .append("injector", injector)
                .toString();
    }
}
