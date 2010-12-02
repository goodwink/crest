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

import org.codegist.crest.CRestContext;

/**
 * Creates instances of {@link org.codegist.crest.config.InterfaceConfig} for the given interfaces.
 * <p>Implementors must apply to the following contract :
 * <p>- No method of the {@link org.codegist.crest.config.InterfaceConfig} instance and sub-config objects return null values expects the one documented.
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.InterfaceContext#getProperties()}'s defaults overrides.
 * <p>- All methods in the interface must have it's {@link MethodConfig} configured in the {@link org.codegist.crest.config.InterfaceConfig}.
 * <p>- All parameters of all methods in the interface must have it's {@link org.codegist.crest.config.ParamConfig} configured for each {@link MethodConfig}.
 * <p>- If any method's parameter type is annotated with any parameter specifics annotation, the type specific annotation configs are used unless explicitly specified at the interface or factory configuration level.
 * <p>-
 * @see org.codegist.crest.config.InterfaceConfig
 * @see org.codegist.crest.config.MethodConfig
 * @see org.codegist.crest.config.ParamConfig
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface InterfaceConfigFactory {

    /**
     * @param context   Current CRest context
     * @param interfaze Interface to build the configuration from
     * @return The interface config object.
     * @throws ConfigFactoryException for any problem occuring during the configuration construction
     * @see org.codegist.crest.config.InterfaceConfigFactory
     */
    InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException;

}
