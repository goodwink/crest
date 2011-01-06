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

class DefaultStaticParam implements StaticParam {

    private final String name;
    private final String value;
    private final Destination destination;

    DefaultStaticParam(String name, String value, Destination destination) {
        this.name = name;
        this.value = value;
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Destination getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultStaticParam that = (DefaultStaticParam) o;

        return new EqualsBuilder()
                .append(destination, that.destination)
                .append(name, that.name)
                .append(value, that.value)
                .equals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(value)
                .append(destination)
                .hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("value", value)
                .append("destination", destination)
                .toString();
    }
}
