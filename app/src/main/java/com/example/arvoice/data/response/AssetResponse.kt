package com.example.arvoice.data.response

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

data class AssetListResponse(val assets: List<AssetResponse>) {
    // Needed for awaitObjectResponse, awaitObject, etc.
    class Deserializer : ResponseDeserializable<AssetListResponse> {
        override fun deserialize(content: String): AssetListResponse {
            var value: AssetListResponse? = null
            try {
                value = Gson().fromJson(content, AssetListResponse::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
            return value!!
        }
    }
}

data class AssetResponse(@SerializedName("name") val id: String,
                         val displayName: String?,
                         @SerializedName("thumbnail") val thumbnailResponse: ThumbnailResponse?,
                         val description: String?,
                         @SerializedName("formats") val formatListResponse: List<FormatResponse>) {


    // Needed for awaitObjectResponse, awaitObject, etc.
    class Deserializer : ResponseDeserializable<AssetResponse> {
        override fun deserialize(content: String): AssetResponse {
            var value: AssetResponse? = null
            try {
                value = Gson().fromJson(content, AssetResponse::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
            return value!!
        }
    }

}

data class ThumbnailResponse(val url: String)

data class FormatResponse(@SerializedName("root") val data: FormatInformationResponse)

data class FormatInformationResponse(val url: String, val contentType: String)