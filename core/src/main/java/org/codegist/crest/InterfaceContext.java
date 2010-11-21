package org.codegist.crest;

import org.codegist.crest.config.InterfaceConfig;

import java.util.Map;

/**
 * {@link InterfaceContext}'s context, passed to any component used during request building, parameter serialization and response deserialization process.
 */
public interface InterfaceContext {

    InterfaceConfig getConfig();

    Map<String, Object> getCustomProperties();

    <T> T getCustomProperty(String name);

}
