package com.example.arvoice.ui.main.navigation

interface MainNavigation {

    fun navigateToVoiceFragment()

    fun navigateToRenderablesFragment(query: String)

    fun navigateToAugmentedFragment()

    fun showAugmentedRoomUI(value: Boolean)

}
