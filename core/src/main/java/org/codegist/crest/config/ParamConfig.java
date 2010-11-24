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

import org.codegist.crest.injector.DefaultRequestInjector;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.serializer.Serializer;

/**
 * Method's argument configuration holder object.
 * <p>Implementors must respect the following contract :
 * <p>- No method return null except for the ones documented or when used as an override template (see {@link Configs#override(ParamConfig, ParamConfig)})
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.CRestContext#getProperties()}'s defaults overrides.
 *
 * @see org.codegist.crest.config.MethodConfig
 * @see org.codegist.crest.config.ParamConfig
 * @see org.codegist.crest.config.InterfaceConfigFactory
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface ParamConfig {

    /**
     * Default destination applied when non specified.
     *
     * @see ParamConfig#getDestination()
     */
    Destination DEFAULT_DESTINATION = Destination.URL;

    /**
     * Default injector applied when non specified.
     *
     * @see org.codegist.crest.config.ParamConfig#getInjector()
     */
    RequestInjector DEFAULT_INJECTOR = new DefaultRequestInjector();

    /**
     * Default serializer applied when non specified.
     *
     * @see org.codegist.crest.config.ParamConfig#getSerializer()
     */
    Serializer DEFAULT_SERIALIZER = null;

    /**
     * Default name applied when non specified.
     *
     * @see org.codegist.crest.config.ParamConfig#getName()
     */
    String DEFAULT_NAME = "";

    /**
     * The serializer used to transform this argument value in a string.
     * <p>This serializer is meant to be used by the {@link org.codegist.crest.injector.RequestInjector} set for this parameter.
     * <p>{@link org.codegist.crest.injector.DefaultRequestInjector} will merge the serialized value in the URL or Body.
     * <p>If the object could not be serialized to a String, then a custom {@link org.codegist.crest.injector.RequestInjector} can be specified.
     *
     * @return The serializer used to transform this argument value in a string
     * @see org.codegist.crest.serializer.Serializer
     * @see org.codegist.crest.injector.RequestInjector
     */
    Serializer getSerializer();

    /**
     * <p>Defines where the parameter value should used, either in the query string or in the request body.
     *
     * @return Destination of the argument value.
     */
    Destination getDestination();

    /**
     * <p>For parameters with {@link org.codegist.crest.config.Destination#URL} destination :
     * <p>- if not blank, is used to add a new queryString parameter not specified in the {@link MethodConfig#getPath()}.
     * <p>- otherwise ignored and the parameter will be merged in the relative {@link MethodConfig#getPath()} placeholders.
     * <br/><br/>
     * <p>For parameters with {@link org.codegist.crest.config.Destination#BODY} destination :
     * <p>- as a body key, if empty the body will just contains the parameter value.
     *
     * @return Parameter name to be used.
     */
    String getName();

    /**
     * <p>Should be used when the user wish to inject a parameter that is not serializable to a single String or when user specific rules applies (eg: parameter must be exploded in multiple values accross the request queryString and/or body content).
     *
     * @return The parameter request injector.
     */
    RequestInjector getInjector();
}
