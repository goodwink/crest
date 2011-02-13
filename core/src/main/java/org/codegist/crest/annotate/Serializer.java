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
 * <p>Optional method argument level annotation, sets the parameter serializer to use. If not specified, defaults to the parameter config default value.
 * <p>Can be set at method level to default all method argument values if not specified at argument level.
 * <p>Can be set at interface level to default all method argument values if not specified at method level.
 * <p>Additionally, this annotation (with other parameter-specific annotation) can be used for any user class used as a method argument of a rest-binded interface, eg :
 * <code>
 * <pre>
 * &#64;EndPoint("http://my-server")
 * interface FooInterface {
 *    MyModel getModel(long id, MyBean arg);
 * }
 * &#64;Serializer(MyBeanSerializer.class)
 * class MyBean {}
 * </pre>
 * </code>
 * <p>For any call to FooInterface.getModel(long,MyBean), MyBean will be serialized using MyBeanSerializer class.
 * @see org.codegist.crest.config.MethodParamConfig#DEFAULT_SERIALIZER
 * @see org.codegist.crest.config.MethodParamConfig#getSerializer()
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.PARAMETER})
public @interface Serializer {
    Class<? extends org.codegist.crest.serializer.Serializer> value();
}
