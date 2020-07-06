package com.example.arvoice.ui.main.ar

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.arvoice.ARVoiceApplication
import com.example.arvoice.data.IRenderableRepository
import com.example.arvoice.data.IStorageClient
import com.example.arvoice.data.RenderableDataRepository
import com.example.arvoice.data.StorageClient
import com.example.arvoice.domain.AugmentedRoom
import com.example.arvoice.domain.RenderableAsset
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ARViewModel: ViewModel(), CoroutineScope,
    IARViewModel {

    private val repository: IRenderableRepository = RenderableDataRepository.getInstance()
    private var storageClient: IStorageClient = StorageClient(ARVoiceApplication.applicationContext())

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.d("LOGGING C HANDLER", throwable.localizedMessage ?: "ERROR C HANDLER NOT FOUND")
    }

    private val selectedAssetData: MutableLiveData<RenderableAsset> = MutableLiveData()
    private val assetListObservable: MutableLiveData<List<RenderableAsset>> = MutableLiveData()

    var resolvedAssetsLiveData: MutableLiveData<ArrayList<RenderableAsset>> = MutableLiveData()
    var isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    override fun getSelectedAssetObservable(): LiveData<RenderableAsset> = selectedAssetData
    override fun getAssetListObservable(): LiveData<List<RenderableAsset>> = assetListObservable

    override fun setSelectedAsset(renderableAsset: RenderableAsset) {
        selectedAssetData.value = renderableAsset
    }

    override fun requestAssets(key: String) {
        launch(handler) {
            try {
                isLoadingLiveData.postValue(true)
                val assetsListComponent = withContext(Dispatchers.IO) { repository.getAssetsDetailsByQuery(key) }
                val assetsList = assetsListComponent.component1()
                val newAssetList = arrayListOf<RenderableAsset>()

                assetsList?.forEach { renderable ->
                    val renderableAsset = withContext(Dispatchers.Main) {
                        loadRenderable(ARVoiceApplication.applicationContext(), Uri.parse(renderable.renderableUri))
                    }
                    renderableAsset?.let {
                        renderable.renderable = it
                    }
                    if(renderable.renderable != null) {
                        newAssetList.add(renderable)
                        assetListObservable.value = newAssetList
                    }
                }
                isLoadingLiveData.postValue(false)
            } catch (e: Exception) {
                Log.d("LOGGING COROUTINE ERROR", e.localizedMessage ?: "ERROR COROUTINE NOT FOUND")
            }
        }

    }

    fun getRenderablesById(assetsToLoad: List<RenderableAsset>) {
        resolvedAssetsLiveData = MutableLiveData()
        viewModelScope.launch (Dispatchers.IO) {
            val idsList = arrayListOf<String>()
            for(asset in assetsToLoad)
                idsList.add(asset.id)
            val ids = idsList.distinct()
            for(id in ids) {
                val assetByIdList = repository.getAssetDetailsById(id).component1()
                assetByIdList?.forEach { renderableAsset ->
                    val renderable = withContext(Dispatchers.Main) {
                        loadRenderable(
                            ARVoiceApplication.applicationContext(),
                            Uri.parse(renderableAsset.renderableUri)
                        )
                    }
                    renderable?.let {
                        renderableAsset.renderable = it
                        val currentList = resolvedAssetsLiveData.value?: arrayListOf()
                        currentList.add(renderableAsset)
                        resolvedAssetsLiveData.postValue(currentList)
                    }
                }
            }

        }

    }

    private suspend fun loadRenderable(context: Context, uri: Uri): Renderable? =
        suspendCoroutine { coroutine ->
            ModelRenderable.builder()
                .setSource(
                    context,
                    RenderableSource.builder().setSource(
                        context, uri, RenderableSource.SourceType.GLTF2
                    ).build()
                )
                .build()
                .thenAccept {
                    coroutine.resume(it)
                }
                .exceptionally {
                    coroutine.resume(null)
                    Toast.makeText(
                        context,
                        "error parsing gltf2 link + ${it.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@exceptionally null

                }
        }

    override fun getAugmentedRoom(code: Int) = storageClient.getAugmentedRoom(code)

    override fun storeAugmentedRoom(augmentedRoom: AugmentedRoom) = storageClient.storeAugmentedRoom(augmentedRoom)

}