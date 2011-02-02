/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.crest.injector.DefaultInjector;
import org.codegist.crest.injector.Injector;
import org.codegist.crest.serializer.Serializer;

/**
 * Method's argument configuration holder object.
 * <p>Implementors must respect the following contract :
 * <p>- No method return null except for the ones documented or when used as an override template (see {@link org.codegist.crest.config.Configs#override(org.codegist.crest.config.BasicParamConfig , org.codegist.crest.config.BasicParamConfig)})
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.InterfaceContext#getProperties()}'s defaults overrides.
 *
 * @see MethodConfig
 * @see org.codegist.crest.config.BasicParamConfig
 * @see InterfaceConfigFactory
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface BasicParamConfig {

    /**
     * Default destination applied when non specified.
     *
     * @see org.codegist.crest.config.BasicParamConfig#getDestination()
     */
    Destination DEFAULT_DESTINATION = Destination.QUERY;

    /**
     * Default parameter value if not specified.
     *
     * @see org.codegist.crest.config.BasicParamConfig#getDefaultValue()
     */
    String DEFAULT_VALUE = "";

    /**
     * Default name applied when non specified.
     *
     * @see org.codegist.crest.config.BasicParamConfig#getName()
     */
    String DEFAULT_NAME = "";

    /**
     * <p>For parameters with {@link Destination#URL} destination :
     * <p>- if not blank, is used to add a new queryString parameter not specified in the {@link org.codegist.crest.config.MethodConfig#getPath()}.
     * <p>- otherwise ignored and the parameter will be merged in the relative {@link org.codegist.crest.config.MethodConfig#getPath()} placeholders.
     * <br/><br/>
     * <p>For parameters with {@link Destination#BODY} destination :
     * <p>- as a body key, if empty the body will just contains the parameter value.
     *
     * @return Parameter name to be used.
     */
    String getName();

    /**
     * <p> Param default value if not specified
     * @return param default value
     */
    String getDefaultValue();

    /**
     * <p>Defines where the parameter value should used, either in the query string or in the request body.
     *
     * @return Destination of the argument value.
     */
    Destination getDestination();
}
