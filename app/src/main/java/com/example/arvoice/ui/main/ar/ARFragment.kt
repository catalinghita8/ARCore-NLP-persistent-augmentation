package com.example.arvoice.ui.main.ar


import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.arvoice.utils.Constants
import com.example.arvoice.domain.AppAnchorState
import com.example.arvoice.domain.AugmentedRoom
import com.example.arvoice.domain.LogItem
import com.example.arvoice.domain.RenderableAsset
import com.example.arvoice.domain.UserAsset
import com.example.arvoice.ui.main.logs.MainLogger
import com.example.arvoice.ui.main.navigation.MainNavigation
import com.example.arvoice.ui.main.room_input.InputAugmentedRoomFragment
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class ARFragment: ArFragment() {

    private lateinit var viewModel: ARViewModel

    private var selectedRenderableAsset: RenderableAsset? = null
    private var renderableAssetsToResolve: ArrayList<RenderableAsset>? = null

    private var cloudAnchorToHost: Anchor? = null
    private var appAnchorToHostState = AppAnchorState.NONE

    private var currentResolvedRoomId = -1
    private var loggerCallback: MainLogger? = null
    private var roomStatusChangeCallback: RoomStatusChangeCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ARViewModel::class.java)

        viewModel.getSelectedAssetObservable().observe(this, Observer { asset ->
            addLog("Received asset id ${asset.id.removePrefix("assets/")}")
            selectedRenderableAsset = asset
        })
    }

    override fun getSessionConfiguration(session: Session?): Config? {
        val config = session?.config
        config?.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        return config
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPresses()

        roomStatusChangeCallback = activity as RoomStatusChangeCallback
        loggerCallback = activity as MainLogger
        arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

        setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING || appAnchorToHostState != AppAnchorState.NONE)
                return@setOnTapArPlaneListener

            addNewHostingAnchor(hitResult)
        }

        roomStatusChangeCallback?.setAugmentedUIState(false, ARButtonState.STATE_EXPLORE, null) {
            showResolveRoomIdInput()
        }

    }

    private fun showResolveRoomIdInput() {
        if (renderableAssetsToResolve != null) {
            addLog("Resolving failed: process is busy")
            Toast.makeText(context!!, "There is a room currently being resolved.", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = InputAugmentedRoomFragment()
        dialog.parentCallback = object : InputAugmentedRoomFragment.Callback {
            override fun onResolveRoomPressed(roomId: Int) {
                resolveRoom(roomId)
            }
        }
        dialog.show(activity?.supportFragmentManager!!, "Resolve")
    }

    private fun addModelToScene(anchor: Anchor, model: Renderable) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arSceneView.scene)

        val node = TransformableNode(transformationSystem)
        node.renderable = model
        node.scaleController.maxScale = 0.3f
        node.scaleController.minScale = 0.05f
        node.setParent(anchorNode)
    }

    private fun resolveRoom(roomCode: Int) {
        val augmentedRoom = viewModel.getAugmentedRoom(roomCode)
        if(augmentedRoom == null) {
            addLog("Room retrieval fail:  not found")
            Toast.makeText(context!!, "Room not found. Try again", Toast.LENGTH_SHORT).show()
            return
        }

        currentResolvedRoomId = roomCode
        val resolvedRenderableAssets = arrayListOf<RenderableAsset>()


        addLog("Getting cloud anchors for room ${augmentedRoom.id}...")
        for(userAsset in augmentedRoom.userAssets) {
            val resolvedAnchor = arSceneView.session?.resolveCloudAnchor(userAsset.cloudAnchorId)
            resolvedAnchor?.let { cloudAnchor ->
                resolvedRenderableAssets.add(RenderableAsset(userAsset.renderableModelId, cloudAnchor,
                    AppAnchorState.RESOLVING, System.currentTimeMillis())) }
        }

        roomStatusChangeCallback?.setAugmentedUIState(true, ARButtonState.STATE_EXIT, augmentedRoom.id) { clearCurrentRoom() }
        addLog("Resolving ${resolvedRenderableAssets.size} cloud anchors out of ${augmentedRoom.userAssets.size}")
        renderableAssetsToResolve = resolvedRenderableAssets

        addLog("Caching assets for room ${augmentedRoom.id}...")
        viewModel.getRenderablesById(resolvedRenderableAssets)
    }

    @Synchronized
    private fun onUpdateFrame(frameTime: FrameTime) {
        checkHostingAnchorState()
        checkResolvingAnchorsState()
    }

    @Synchronized
    private fun checkHostingAnchorState() {
        if (appAnchorToHostState != AppAnchorState.HOSTING)
            return

        val cloudState: Anchor.CloudAnchorState = cloudAnchorToHost?.cloudAnchorState!!
        if (appAnchorToHostState == AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                addLog("Hosting anchor: fail. Restarting host anchor...")
                Toast.makeText(context!!, "Error hosting anchor...", Toast.LENGTH_SHORT).show()
                appAnchorToHostState = AppAnchorState.NONE
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                if(cloudAnchorToHost?.cloudAnchorId != null && selectedRenderableAsset != null) {
                    addLog("Hosted anchor for asset ${selectedRenderableAsset?.id?.removePrefix("assets/")} ...")
                    val newUserAsset = UserAsset(cloudAnchorToHost?.cloudAnchorId!!, selectedRenderableAsset?.id!!)
                    var updatedAugmentedRoom = viewModel.getAugmentedRoom(currentResolvedRoomId)
                    if(updatedAugmentedRoom == null) {
                        addLog("Room not found. Creating new one")
                        updatedAugmentedRoom = AugmentedRoom(-1, arrayListOf()) // Create new room
                    }
                    val newUserAssets = arrayListOf<UserAsset>()
                    newUserAssets.addAll(updatedAugmentedRoom.userAssets)
                    newUserAssets.add(newUserAsset)
                    updatedAugmentedRoom.userAssets = newUserAssets.toList()
                    val augmentedRoomCode = viewModel.storeAugmentedRoom(updatedAugmentedRoom)
                    addLog("Saved ${selectedRenderableAsset?.id?.removePrefix("assets/")} in room $augmentedRoomCode ...")
                    addNewlyHostedModelToResolvingList()

                    addLog("Anchor hosted. Updated room: $augmentedRoomCode")
                    currentResolvedRoomId = augmentedRoomCode
                    appAnchorToHostState = AppAnchorState.HOSTED

                    roomStatusChangeCallback?.setAugmentedUIState(false, ARButtonState.STATE_EXIT, currentResolvedRoomId) { clearCurrentRoom() }

                    restartHostingAnchor()
                }
            }
        }
    }

    private fun addNewlyHostedModelToResolvingList() {
        val newRenderableAsset = selectedRenderableAsset
        newRenderableAsset?.let {
            newRenderableAsset.anchor = cloudAnchorToHost
            newRenderableAsset.anchorState = AppAnchorState.NONE
            newRenderableAsset.isPlacedInScene = true
            if(renderableAssetsToResolve == null)
                renderableAssetsToResolve = arrayListOf()
            renderableAssetsToResolve!!.add(newRenderableAsset)
        }
    }

    @Synchronized
    private fun checkResolvingAnchorsState() {
        val renderableAssets = renderableAssetsToResolve
        renderableAssets?.let {
            checkAnchorsState@ for(renderableAssetToResolve in renderableAssets) {
                if(renderableAssetToResolve.anchorState != AppAnchorState.RESOLVING ||
                    anchorResolvingHasExpired(renderableAssetToResolve))
                    continue@checkAnchorsState

                val cloudAnchorToResolve = renderableAssetToResolve.anchor!!
                val cloudState: Anchor.CloudAnchorState = cloudAnchorToResolve.cloudAnchorState!!
                if (renderableAssetToResolve.anchorState == AppAnchorState.RESOLVING) {
                    if (cloudState.isError) {
                        Toast.makeText(context!!, "Error resolving anchor. No model will be added for this anchor.", Toast.LENGTH_SHORT).show()
                        addLog("Error resolving anchor for asset ${renderableAssetToResolve.id}...")
                        renderableAssetToResolve.anchorState = AppAnchorState.NONE
                        renderableAssetToResolve.anchor?.detach()
                    } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                        renderableAssetToResolve.anchorState = AppAnchorState.RESOLVED
                        addLog("Resolved anchor for asset ${renderableAssetToResolve.id.removePrefix("assets/")}")
                        addLog("Anchors resolved: ${getResolvedAnchors()}/${renderableAssetsToResolve?.size}...")

                        val renderableIdToLoad = renderableAssetToResolve.id
                        viewModel.resolvedAssetsLiveData.observe(this, Observer { renderableAssets ->
                            if(renderableAssets != null) {
                                for(renderableAsset in renderableAssets) {
                                    if(renderableAsset.id == renderableIdToLoad && !renderableAssetToResolve.isPlacedInScene) {
                                        addLog("Placed model ${renderableAsset.id}")
                                        renderableAssetToResolve.isPlacedInScene = true
                                        addModelToScene(cloudAnchorToResolve, renderableAsset.renderable!!)
                                    }
                                }
                                checkAllAssetsLoaded()
                            }
                        })
                    }
                }
            }
        }
    }

    private fun checkAllAssetsLoaded() {
        renderableAssetsToResolve?.forEach { asset ->
            if(!asset.isPlacedInScene)
                return
        }
        roomStatusChangeCallback?.setAugmentedUIState(false, ARButtonState.STATE_EXIT, currentResolvedRoomId) {
            clearCurrentRoom()
        }
    }

    private fun anchorResolvingHasExpired(asset: RenderableAsset): Boolean {
        if(System.currentTimeMillis() - asset.resolvingStartTime > Constants.CLOUD_ANCHOR_RESOLVING_THRESHOLD) {
            addLog("Anchor resolve time limit  reached")
            Toast.makeText(context!!, "Resolving anchor time threshold was reached. " +
                    "No model will be added for this anchor.", Toast.LENGTH_SHORT).show()
            asset.anchor?.detach()
            asset.anchorState = AppAnchorState.NONE
            asset.isPlacedInScene = true // This way we marked it as placed in scene with error
            roomStatusChangeCallback?.setAugmentedUIState(false, ARButtonState.STATE_EXIT, currentResolvedRoomId) {
                clearCurrentRoom()
            }
            return true
        }
        return false
    }

    private fun clearCurrentRoom() {
        addLog("Remove assets and exit room $currentResolvedRoomId")
        for(renderableAsset in renderableAssetsToResolve?: listOf<RenderableAsset>()) {
            renderableAsset.anchor?.detach()
            renderableAsset.anchorState = AppAnchorState.NONE
        }

        renderableAssetsToResolve?.clear()
        renderableAssetsToResolve = null
        currentResolvedRoomId = -1
        cloudAnchorToHost?.detach()
        cloudAnchorToHost = null
        appAnchorToHostState = AppAnchorState.NONE

        roomStatusChangeCallback?.setAugmentedUIState(false, ARButtonState.STATE_EXPLORE, null) {
            showResolveRoomIdInput()
        }
    }

    private fun addNewHostingAnchor(hitResult: HitResult) {
        val anchor = arSceneView.session?.hostCloudAnchor(hitResult.createAnchor())
        if(roomModelsLimitIsReached())
            return

        roomStatusChangeCallback?.setAugmentedUIState(true, ARButtonState.STATE_UNDEFINED, null) { }
        cloudAnchorToHost?.detach()
        cloudAnchorToHost = anchor

        appAnchorToHostState = AppAnchorState.HOSTING
        addLog("Placing new model and start hosting anchor")
        selectedRenderableAsset?.renderable?.let { model -> addModelToScene(cloudAnchorToHost!!, model) }
    }

    private fun restartHostingAnchor() {
        appAnchorToHostState = AppAnchorState.NONE
        cloudAnchorToHost = null
        addLog("Restarting hosting anchor state")
    }

    private fun roomModelsLimitIsReached(): Boolean {
        val currentAugmentedRoom = viewModel.getAugmentedRoom(currentResolvedRoomId)
        if(currentAugmentedRoom != null && currentAugmentedRoom.userAssets.size >= Constants.MAX_MODELS_ROOM_SIZE) {
            addLog("The limit of models has been reached for room $currentResolvedRoomId...")
            Toast.makeText(context!!, "The limit of models has been reached for room $currentResolvedRoomId. " +
                    "Your model has not been saved.", Toast.LENGTH_SHORT).show()
            cloudAnchorToHost?.detach()
            restartHostingAnchor()
            return true
        }
        return false
    }

    private fun addLog(message: String) {
        loggerCallback?.appendLog(LogItem(System.currentTimeMillis(), message))
    }

    private fun getResolvedAnchors() = renderableAssetsToResolve?.count { it.anchorState == AppAnchorState.RESOLVED }

    private fun handleBackPresses() {
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return if (keyCode == KeyEvent.KEYCODE_BACK) {
                    (activity as MainNavigation).showAugmentedRoomUI(false)
                    activity?.onBackPressed()
                    true
                } else false
            }
        })
    }

}
