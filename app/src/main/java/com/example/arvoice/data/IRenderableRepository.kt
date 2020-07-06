package com.example.arvoice.data

import com.example.arvoice.domain.RenderableAsset
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result

interface IRenderableRepository {
    suspend fun getAssetsDetailsByQuery(query: String): Result<List<RenderableAsset>, FuelError>
    suspend fun getAssetDetailsById(id: String): Result<List<RenderableAsset>, FuelError>
}
