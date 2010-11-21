package org.codegist.crest;

import org.codegist.crest.config.MethodConfig;
import org.codegist.crest.config.ParamConfig;

import java.lang.reflect.Method;

/**
 * Default internal immutable implementation of RequestContext
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

    public Object getArgValue(int index) {
        return args[index];
    }

    public String getArgSerialized(int index) {
        return getParamConfig(index).getSerializer().serialize(new DefaultParamContext(this, index));
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
