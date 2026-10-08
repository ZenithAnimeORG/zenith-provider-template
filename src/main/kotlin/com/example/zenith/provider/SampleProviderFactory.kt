package com.example.zenith.provider

import com.pilldev.zenith.provider.ZenithProvider
import com.pilldev.zenith.provider.ZenithProviderFactory
import com.pilldev.zenith.provider.model.PluginManifest

public class SampleProviderFactory : ZenithProviderFactory {
    override fun create(manifest: PluginManifest): ZenithProvider {
        return SampleProvider(manifest)
    }
}
