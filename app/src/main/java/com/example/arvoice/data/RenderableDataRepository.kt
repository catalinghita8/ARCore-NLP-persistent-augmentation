package com.example.arvoice.data

import awaitObjectResponse
import com.example.arvoice.data.response.AssetListResponse
import com.example.arvoice.data.response.AssetResponse
import com.example.arvoice.domain.RenderableAsset
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map

class RenderableDataRepository: IRenderableRepository {

    override suspend fun getAssetsDetailsByQuery(query: String): Result<List<RenderableAsset>, FuelError> {
        return requestAssetsData(query)
            .awaitObjectResponse(AssetListResponse.Deserializer())
            .third.map { assetListResponse ->
            val renderableList = arrayListOf<RenderableAsset>()
            assetListResponse.assets.forEach assetsForEach@{ asssetResponse ->
                asssetResponse.formatListResponse.forEach formatsForEach@{ formatResponse ->
                    if ((asssetResponse.displayName?.contains(query) == true
                                || asssetResponse.description?.contains(query) == true)
                        && formatResponse.data.url.contains(RENDERABLE_FORMAT)
                    ) {
                        renderableList.add(
                            RenderableAsset(
                                asssetResponse.id,
                                asssetResponse.displayName,
                                asssetResponse.thumbnailResponse?.url ?: "",
                                formatResponse.data.url,
                                null
                            )
                        )
                    }
                }
            }

            return@map renderableList
        }
    }

    override suspend fun getAssetDetailsById(id: String): Result<List<RenderableAsset>, FuelError> {
        return getAssetResponseById(id)
            .awaitObjectResponse(AssetResponse.Deserializer())
            .third.map { assetResponse ->
                val renderableList = arrayListOf<RenderableAsset>()
                assetResponse.formatListResponse.forEach formatsForEach@{ formatResponse ->
                    if (formatResponse.data.url.contains(RENDERABLE_FORMAT))
                        renderableList.add(RenderableAsset(
                            assetResponse.id,
                            assetResponse.displayName,
                            assetResponse.thumbnailResponse?.url ?: "",
                            formatResponse.data.url,
                            null
                        ))
                }
                return@map renderableList
        }
    }

    private fun requestAssetsData(query: String): Request = assetsURL.httpGet(
        listOf(
            "keywords" to query,
            "maxComplexity" to "MEDIUM",
            "format" to "GLTF",
            "pageSize" to 30,
            "key" to key
        )
    )

    private fun getAssetResponseById(id: String): Request = (assetURL + id).httpGet(
        listOf(
            "key" to key
        )
    )

    companion object {

        const val key = "AIzaSyB5NXIfCP7V_2GG2TIo0_kfH0ePVSrGGNo"
        const val baseURL = "https://poly.googleapis.com/v1"

        const val RENDERABLE_FORMAT = "gltf"

        // its url
        val assetURL = "$baseURL/"
        val assetsURL = "$baseURL/assets"

        // For Singleton instantiation
        @Volatile
        private var instance: RenderableDataRepository? = null

        fun getInstance() =
            instance
                ?: synchronized(this) {
                    instance
                        ?: RenderableDataRepository()
                            .also { instance = it }
                }

    }

}