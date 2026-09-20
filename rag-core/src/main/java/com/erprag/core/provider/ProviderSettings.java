package com.erprag.core.provider;

/**
 * Config for one model (chat or embedding). Only the fields relevant to
 * {@code type} need to be set; unused fields are ignored.
 */
public record ProviderSettings(
        ProviderType type,
        String apiKey,
        String modelName,
        String baseUrl,
        String deploymentName
) {
}
