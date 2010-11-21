package org.codegist.crest.config;

import org.codegist.crest.CRestContext;

/**
 * Creates instances of {@link org.codegist.crest.config.InterfaceConfig} for the given interfaces.
 * <p>Implementors must apply to the following contract :
 * <p>- No method of the {@link org.codegist.crest.config.InterfaceConfig} instance and sub-config objects return null values expects the one documented.
 * <p>- Defaults values must either be taken from interface's defaults constant or from {@link org.codegist.crest.CRestContext#getCustomProperties()}'s defaults overrides.
 * <p>- All methods in the interface must have it's {@link MethodConfig} configured in the {@link org.codegist.crest.config.InterfaceConfig}.
 * <p>- All parameters of all methods in the interface must have it's {@link org.codegist.crest.config.ParamConfig} configured for each {@link MethodConfig}.
 * <p>- If any method's parameter in the is annotated with {@link org.codegist.crest.annotate.RestInjector}, the injector is used unless explicitly specified at the factory level.
 *
 * @see org.codegist.crest.config.InterfaceConfig
 * @see org.codegist.crest.config.MethodConfig
 * @see org.codegist.crest.config.ParamConfig
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
