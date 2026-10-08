package com.example.zenith.provider

import com.pilldev.zenith.provider.BaseZenithProvider
import com.pilldev.zenith.provider.CatalogMediaDetails
import com.pilldev.zenith.provider.CatalogMediaItem
import com.pilldev.zenith.provider.CatalogProvider
import com.pilldev.zenith.provider.CatalogSearchResult
import com.pilldev.zenith.provider.CatalogSection
import com.pilldev.zenith.provider.MediaSourceProvider
import com.pilldev.zenith.provider.context.ProviderContext
import com.pilldev.zenith.provider.model.MediaId
import com.pilldev.zenith.provider.model.MediaRef
import com.pilldev.zenith.provider.model.PluginManifest
import com.pilldev.zenith.provider.model.ProviderEpisode
import com.pilldev.zenith.provider.model.ProviderMediaStream
import com.pilldev.zenith.provider.model.ProviderResult
import com.pilldev.zenith.provider.model.ProviderStatusInfo
import com.pilldev.zenith.provider.model.ProviderVideoSource

public class SampleProvider(
    override val manifest: PluginManifest,
) : BaseZenithProvider(manifest), CatalogProvider, MediaSourceProvider {

    override suspend fun executeAction(
        actionId: String,
        context: ProviderContext,
    ): ProviderResult<String> {
        return when (actionId) {
            "ping" -> {
                val mirror = context.getEffectiveMirror().ifBlank { "https://api.example.com" }
                ProviderResult.Success("Connected to mirror: $mirror")
            }
            else -> ProviderResult.Failure("Unsupported action: $actionId")
        }
    }

    override suspend fun probeStatus(context: ProviderContext): ProviderResult<ProviderStatusInfo> {
        val mirror = context.getEffectiveMirror().ifBlank { "https://api.example.com" }
        return ProviderResult.Success(
            ProviderStatusInfo(
                status = "OK",
                details = mapOf("activeMirror" to mirror),
                latencyMs = 42L,
            ),
        )
    }

    override suspend fun search(
        query: String,
        page: Int,
        pageSize: Int,
    ): ProviderResult<CatalogSearchResult> {
        val dummyItems = listOf(
            CatalogMediaItem(
                mediaRef = MediaRef(
                    id = MediaId("sample_1"),
                    externalIds = mapOf(manifest.id to "sample_1"),
                ),
                title = "Sample Release: $query",
                originalTitle = "Sample Original Title",
                posterUrl = "https://example.com/poster.jpg",
                score = 8.5,
                year = 2026,
                totalEpisodes = 12,
            ),
        )
        return ProviderResult.Success(
            CatalogSearchResult(
                items = dummyItems,
                hasNextPage = false,
            ),
        )
    }

    override suspend fun getDetails(mediaRef: MediaRef): ProviderResult<CatalogMediaDetails> {
        return ProviderResult.Success(
            CatalogMediaDetails(
                mediaRef = mediaRef,
                title = "Sample Detailed Title",
                originalTitle = "Sample Detailed Original Title",
                description = "Demonstration media item provided by zenith-provider-template.",
                posterUrl = "https://example.com/poster.jpg",
                score = 8.5,
                year = 2026,
                episodesCount = 12,
                genres = listOf("Action", "Comedy"),
                status = "ongoing",
            ),
        )
    }

    override suspend fun getHomeSections(): ProviderResult<List<CatalogSection>> {
        return ProviderResult.Success(
            listOf(
                CatalogSection(
                    title = "Featured Releases",
                    items = listOf(
                        CatalogMediaItem(
                            mediaRef = MediaRef(
                                id = MediaId("sample_featured"),
                                externalIds = mapOf(manifest.id to "sample_featured"),
                            ),
                            title = "Featured Anime",
                            posterUrl = "https://example.com/featured.jpg",
                            score = 9.0,
                            year = 2026,
                            totalEpisodes = 24,
                        ),
                    ),
                ),
            ),
        )
    }

    override suspend fun getSources(
        shikimoriId: Int,
        animeName: String,
        russianName: String?,
    ): ProviderResult<List<ProviderVideoSource>> {
        val source = ProviderVideoSource(
            name = "Sample Dubbing",
            episodes = listOf(
                ProviderEpisode(
                    number = 1,
                    url = "https://example.com/stream/episode_1.m3u8",
                ),
            ),
        )
        return ProviderResult.Success(listOf(source))
    }

    override suspend fun resolveStream(episode: ProviderEpisode): ProviderResult<List<ProviderMediaStream>> {
        return ProviderResult.Success(
            listOf(
                ProviderMediaStream(
                    url = episode.url,
                    quality = "1080p",
                    isHls = episode.url.endsWith(".m3u8"),
                ),
            ),
        )
    }
}
