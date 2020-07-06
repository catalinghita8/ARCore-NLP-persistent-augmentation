package com.example.arvoice.ui.main.ar

import androidx.lifecycle.LiveData
import com.example.arvoice.domain.AugmentedRoom
import com.example.arvoice.domain.RenderableAsset

interface IARViewModel {

    fun getSelectedAssetObservable(): LiveData<RenderableAsset>

    fun requestAssets(key: String)

    fun getAssetListObservable(): LiveData<List<RenderableAsset>>

    fun setSelectedAsset(renderableAsset: RenderableAsset)

    fun getAugmentedRoom(code: Int): AugmentedRoom?

    fun storeAugmentedRoom(augmentedRoom: AugmentedRoom): Int

}
