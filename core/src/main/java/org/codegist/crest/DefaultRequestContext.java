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

import org.codegist.crest.config.MethodConfig;
import org.codegist.crest.config.ParamConfig;
import org.codegist.crest.serializer.Serializer;

import java.lang.reflect.Method;

/**
 * Default internal immutable implementation of RequestContext
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
class DefaultRequestContext extends DefaultInterfaceContext implements RequestContext {

    private final Method method;
    private final Object[] args;

    public DefaultRequestContext(RequestContext context) {
        this(context, context.getMethod(), context.getArgs());
    }

    public DefaultRequestContext(InterfaceContext context, Method method, Object[] args) {
        super(context);
        this.method = method;
        this.args = args != null ? args.clone() : new Object[0];
    }

    public MethodConfig getMethodConfig() {
        return getConfig().getMethodConfig(method);
    }

    public ParamConfig getParamConfig(int index) {
        return getMethodConfig().getParamConfig(index);
    }

    public Object getRawValue(int index) {
        return args[index];
    }

    public String getSerializedValue(int index) {
        Object value = getRawValue(index);
        Serializer serializer = getParamConfig(index).getSerializer();
        return serializer.serialize(value);
    }

    public int getArgCount() {
        return args.length;
    }

    /**
     * @return Interface method being called
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return Method's call arguments.
     */
    public Object[] getArgs() {
        return args != null ? args.clone() : new Object[0];
    }
}
