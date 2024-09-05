package org.mb.tools.rpx.utils;

/**
 * Provides a mechanism for retrieving system properties.
 * <p>
 * This interface abstracts the retrieval of system properties to enable easier testing
 * and to provide flexibility in how properties are accessed.
 * </p>
 */
public interface SystemPropertyProvider {

    /**
     * Retrieves the value of the specified system property.
     *
     * @param key The name of the system property to retrieve.
     * @return The value of the system property, or {@code null} if the property is not set.
     */
    String getProperty(String key);
}

