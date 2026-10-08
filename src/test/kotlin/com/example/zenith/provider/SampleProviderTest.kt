package com.example.zenith.provider

import com.pilldev.zenith.provider.model.PluginManifest
import com.pilldev.zenith.provider.model.ProviderCapability
import com.pilldev.zenith.provider.model.ProviderEpisode
import com.pilldev.zenith.provider.model.ProviderId
import com.pilldev.zenith.provider.model.ProviderResult
import com.pilldev.zenith.provider.testkit.FakeProviderContext
import com.pilldev.zenith.provider.testkit.ProviderContractTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SampleProviderTest : ProviderContractTestBase() {

    private val manifest = PluginManifest(
        id = ProviderId("template_provider"),
        name = "Template Provider",
        version = "1.0.0",
        capabilities = setOf(ProviderCapability.CATALOG, ProviderCapability.MEDIA_SOURCE),
        entryClass = "com.example.zenith.provider.SampleProviderFactory",
        hosts = listOf("api.example.com"),
    )

    @Test
    fun testManifestCompliance() {
        verifyManifestCompliance(manifest)
    }

    @Test
    fun testFactoryCreatesSampleProvider() {
        val factory = SampleProviderFactory()
        val provider = factory.create(manifest)
        assertEquals(manifest.id, provider.manifest.id)
        verifyRequiredCapabilities(provider, setOf(ProviderCapability.CATALOG, ProviderCapability.MEDIA_SOURCE))
        verifyBasicLifecycle(provider)
    }

    @Test
    fun testCatalogSearchAndDetails() = runTest {
        val provider = SampleProvider(manifest)
        val context = FakeProviderContext(
            initialSettings = mapOf("mirror" to "https://api.example.com"),
            effectiveMirrorValue = "https://api.example.com",
        )
        provider.init(context)

        val searchResult = provider.search("Demon Slayer")
        assertIs<ProviderResult.Success<*>>(searchResult)
        val items = (searchResult as ProviderResult.Success).data.items
        assertTrue(items.isNotEmpty())

        val detailsResult = provider.getDetails(items.first().mediaRef)
        assertIs<ProviderResult.Success<*>>(detailsResult)
        val details = (detailsResult as ProviderResult.Success).data
        assertEquals("Sample Detailed Title", details.title)
    }

    @Test
    fun testMediaStreamResolution() = runTest {
        val provider = SampleProvider(manifest)
        val sourcesResult = provider.getSources(shikimoriId = 1, animeName = "Test", russianName = "Тест")
        assertIs<ProviderResult.Success<*>>(sourcesResult)
        val sources = (sourcesResult as ProviderResult.Success).data
        assertTrue(sources.isNotEmpty())

        val episode = sources.first().episodes.first()
        val streamResult = provider.resolveStream(episode)
        assertIs<ProviderResult.Success<*>>(streamResult)
        val streams = (streamResult as ProviderResult.Success).data
        assertEquals("1080p", streams.first().quality)
    }

    @Test
    fun testActionAndStatusProbe() = runTest {
        val provider = SampleProvider(manifest)
        val context = FakeProviderContext(
            initialSettings = mapOf("mirror" to "https://api.example.com"),
            effectiveMirrorValue = "https://api.example.com",
        )
        provider.init(context)

        val pingResult = provider.executeAction("ping", context)
        assertNotNull(pingResult)
        assertIs<ProviderResult.Success<*>>(pingResult)

        val statusResult = provider.probeStatus(context)
        assertIs<ProviderResult.Success<*>>(statusResult)
        val status = (statusResult as ProviderResult.Success).data
        assertEquals("OK", status.status)
    }
}
