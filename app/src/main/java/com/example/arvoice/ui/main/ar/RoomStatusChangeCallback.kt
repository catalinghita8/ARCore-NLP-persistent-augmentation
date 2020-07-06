package com.example.arvoice.ui.main.ar


interface RoomStatusChangeCallback {
    fun setAugmentedUIState(isLoading: Boolean, buttonState: ARButtonState, currentRoomId: Int?, clickAction: () -> Unit)
}