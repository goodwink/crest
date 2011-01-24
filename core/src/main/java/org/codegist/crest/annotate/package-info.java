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

/**
 * Contains {@link org.codegist.crest.CRest} rest-binding annotations to be used to configure user interfaces.
 * An annotated user interface could look like the following:
 * <code>
 * <pre>
 * &#64;EndPoint("http://my-server")
 * &#64;ContextPath("/rest")
 * interface FooInterface {
 *    &#64;Path("/model/{0}")
 *    MyModel getModel(long id);
 *
 *    MyModel get();
 * }
 * CRest crest = new CRestBuilder().build();
 * FooInterface fooInstance = crest.build(FooInterface.class);
 * MyModel m = fooInstance.getModel(5); // a GET request is fired to http://my-server/rest/model/5, response is automatically marshalled and returned as an object.
 * MyModel m2 = fooInstance.get(); // a GET request is fired to http://my-server/rest, response is automatically marshalled and returned as an object.
 * </pre>
 * </code>
 * <br/>
 * <p>For more information on how these values are used, please consult {@link org.codegist.crest.config.InterfaceConfig}, {@link org.codegist.crest.config.MethodConfig}, {@link org.codegist.crest.config.ParamConfig} documentation.
 *
 * @see org.codegist.crest.CRest
 * @see org.codegist.crest.CRestBuilder
 * @see org.codegist.crest.config.CRestAnnotationDrivenInterfaceConfigFactory
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
package org.codegist.crest.annotate;