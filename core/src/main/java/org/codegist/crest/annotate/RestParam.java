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

import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.serializer.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.codegist.crest.config.Fallbacks.*;

/**
 * Use this to annotate any argument of a {@link org.codegist.crest.annotate.RestApi} annotated interface methods.
 * <p>This annotation is optional, if not specified, configuration will fallback to the {@link org.codegist.crest.annotate.RestMethod} defaults for params, and if not present either, to {@link org.codegist.crest.annotate.RestApi} defaults.
 * <p>If used on a method argument, any configuration will override the {@link org.codegist.crest.annotate.RestApi} and {@link org.codegist.crest.annotate.RestMethod} defaults, and any non specified values will fallback following the same rules as stated before.
 * <p>For more information on how these values are used, please consult {@link org.codegist.crest.config.ParamConfig} documentation.
 *
 * @see org.codegist.crest.annotate.RestApi
 * @see org.codegist.crest.annotate.RestMethod
 * @see org.codegist.crest.config.ParamConfig
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RestParam {

    /**
     * <p>See documentation for {@link org.codegist.crest.config.ParamConfig#getName()}.
     * <p>Overrides the {@link org.codegist.crest.annotate.RestMethod#paramsName()} configuration.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestMethod#paramsName()}.
     *
     * @return the parameter specific name use as a key for query string or body content.
     * @see org.codegist.crest.config.ParamConfig#getName()
     * @see org.codegist.crest.annotate.RestMethod#path()
     * @see org.codegist.crest.annotate.RestMethod#paramsName()
     */
    String name() default FALLBACK_STRING;

    /**
     * Param specific destination, overrides the {@link org.codegist.crest.annotate.RestMethod#paramsDestination()} configuration.
     * <p>Defines where the parameter value should used, either in the query string or in the request body.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestMethod#paramsDestination()}.
     *
     * @return param specific destination
     * @see org.codegist.crest.config.ParamConfig#getDestination()
     * @see org.codegist.crest.annotate.RestApi#paramsDestination()
     * @see org.codegist.crest.annotate.RestMethod#paramsDestination()
     */
    String destination() default FALLBACK_STRING;

    /**
     * Param specific serializer, overrides the {@link org.codegist.crest.annotate.RestMethod#paramsSerializer()} configuration.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestMethod#paramsSerializer()}.
     *
     * @return param specific serializer
     * @see org.codegist.crest.config.ParamConfig#getSerializer()
     * @see org.codegist.crest.serializer.Serializer
     * @see org.codegist.crest.annotate.RestApi#paramsSerializer()
     * @see org.codegist.crest.annotate.RestMethod#paramsSerializer()
     */
    Class<? extends Serializer> serializer() default FallbackSerializer.class;

    /**
     * Param specific request injector, overrides the {@link org.codegist.crest.annotate.RestMethod#paramsInjector()} configuration.
     * <p>See documentation for {@link org.codegist.crest.config.ParamConfig#getInjector()}.
     * <p>Defaults to {@link org.codegist.crest.annotate.RestMethod#paramsInjector()}.
     *
     * @return param specific injector
     * @see org.codegist.crest.config.ParamConfig#getInjector()
     * @see org.codegist.crest.injector.RequestInjector
     * @see org.codegist.crest.annotate.RestApi#paramsInjector()
     * @see org.codegist.crest.annotate.RestMethod#paramsInjector()
     */
    Class<? extends RequestInjector> injector() default FallbackRequestParameterInjector.class;
}

