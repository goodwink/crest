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

package org.codegist.crest.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Optional method level annotation, sets the method specific path. If not specified, defaults to the method config default value.
 * <p>Can be set at interface level to define the service base path used by all methods (method's specific paths gets concatenated to it).
 * <p>Can also contain placeholder that will be replace by any value found in the given placeholder config, see {@link org.codegist.crest.CRestBuilder#setConfigPlaceholder(String, String)}
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 * @see org.codegist.crest.CRestBuilder#setConfigPlaceholder(String, String)
 * @see org.codegist.crest.config.MethodConfig#DEFAULT_PATH
 * @see org.codegist.crest.config.MethodConfig#getPath()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Path {

    /**
     * Final path is the result of the concatenation of Interface.{@link EndPoint#value()} + Interface.{@link Path#value()} + Method.{@link Path#value()}
     * <p>Path fragment, can't contains any query string.
     * <p>A path can contains placeholder templates that will be merged with their relative {@link org.codegist.crest.annotate.PathParam}.
     * <p>A simple use case:
     * <code><pre>
     * // given
     * &#64;Path("/{resource-type}/{resource-id}")
     * void meth(
     *      &#64;PathParam("resource-type") String type,
     *      &#64;PathParam("resource-id") long id
     * );
     * <p/>
     * // then a call as
     * myInterface.meth("article", 234l);
     * <p/>
     * // will fire a GET request at  (...)/article/234
     * </pre></code>
     * <p>Can also contain placeholder that will be replace by any value found in the given placeholder config, see {@link org.codegist.crest.CRestBuilder#setConfigPlaceholder(String, String)}
     * @return path
     */
    String value();
}
