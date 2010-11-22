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

package org.codegist.crest;

/**
 * CRest main interface.
 *
 * @see CRest#build(Class)
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public interface CRest {

    /**
     * Returns an instance of the given interface where all methods point to a REST service.
     * <p>Returned interface instances will behave as follow :
     * <p>- methods with a java.io.InputStream or java.io.Reader return type are always considered as expecting the raw server response. Server InputStream/Reader is then return. It is of the responsability of the client to properly call close() on the given Stream in order to free network resources.
     * <p>- for all other methods, response handling is delegated to the preconfigured response handler.
     *
     * @param interfaze Interface class to get the instance from
     * @param <T>       Interface class to get the instance from
     * @return An instance of the given interface
     * @throws CRestException if anything goes wrong
     * @see org.codegist.crest.ResponseHandler
     * @see org.codegist.crest.DefaultResponseHandler
     */
    <T> T build(Class<T> interfaze) throws CRestException;

}
