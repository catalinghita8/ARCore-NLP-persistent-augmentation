package com.example.arvoice.domain

import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.Renderable

data class RenderableAsset(
    val id: String,
    val name: String? = null,
    val pictureUrl: String = "",
    val renderableUri: String = "",
    var renderable: Renderable? = null,
    var anchor: Anchor? = null,
    var anchorState: AppAnchorState = AppAnchorState.NONE,
    var resolvingStartTime: Long = 0,
    var isPlacedInScene: Boolean = false) {

    constructor(id: String, anchor: Anchor, anchorState: AppAnchorState, resolvingStartTime: Long):
            this(id,null, "", "", null, anchor, anchorState, resolvingStartTime)

}
