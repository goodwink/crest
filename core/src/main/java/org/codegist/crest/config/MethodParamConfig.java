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

import org.codegist.crest.injector.DefaultInjector;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.serializer.Serializer;

/**
 * Method's argument configuration holder object.
 * <p>Implementors must respect the following contract :
 * <p>- No method return null except for the ones documented or when used as an override template (see {@link Configs#override(MethodParamConfig , MethodParamConfig)})
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.InterfaceContext#getProperties()}'s defaults overrides.
 *
 * @see org.codegist.crest.config.MethodConfig
 * @see MethodParamConfig
 * @see org.codegist.crest.config.InterfaceConfigFactory
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface MethodParamConfig extends ParamConfig {

    /**
     * Default injector applied when non specified.
     *
     * @see MethodParamConfig#getInjector()
     */
    Class<? extends Injector> DEFAULT_INJECTOR = DefaultInjector.class;

    /**
     * Default serializer applied when non specified.
     *
     * @see MethodParamConfig#getSerializer()
     */
    Class<? extends Serializer> DEFAULT_SERIALIZER = null;

    /**
     * The serializer used to transform this argument value in a string.
     * <p>This serializer is meant to be used by the {@link org.codegist.crest.injector.Injector} set for this parameter.
     * <p>{@link org.codegist.crest.injector.DefaultInjector} will merge the serialized value in the URL or Body.
     * <p>If the object could not be serialized to a String, then a custom {@link org.codegist.crest.injector.Injector} can be specified.
     *
     * @return The serializer used to transform this argument value in a string
     * @see org.codegist.crest.serializer.Serializer
     * @see org.codegist.crest.injector.Injector
     */
    Serializer getSerializer();

    /**
     * <p>Should be used when the user wish to inject a parameter that is not serializable to a single String or when user specific rules applies (eg: parameter must be exploded in multiple values accross the request queryString and/or body content).
     *
     * @return The parameter request injector.
     */
    Injector getInjector();
}
