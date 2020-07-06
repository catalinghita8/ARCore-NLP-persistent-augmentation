package com.example.arvoice.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arvoice.R
import com.example.arvoice.utils.Constants
import com.example.arvoice.utils.Constants.ARG_QUERY_VALUE
import com.example.arvoice.domain.LogItem
import com.example.arvoice.ui.main.ar.ARButtonState
import com.example.arvoice.ui.main.ar.ARFragment
import com.example.arvoice.ui.main.ar.RoomStatusChangeCallback
import com.example.arvoice.ui.main.logs.LogsAdapter
import com.example.arvoice.ui.main.logs.MainLogger
import com.example.arvoice.ui.main.navigation.MainNavigation
import com.example.arvoice.ui.main.renderables.RenderableListFragment
import com.example.arvoice.ui.main.voice.VoiceFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity(), MainNavigation, MainLogger,
    RoomStatusChangeCallback {

    private val logsAdapter = LogsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateToVoiceFragment()
        logsRecyclerView.adapter = logsAdapter
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        layoutManager.stackFromEnd = true
        logsRecyclerView.layoutManager = layoutManager

        val initializationLog: LogItem? = intent.extras?.getParcelable(Constants.ARG_LOG_ITEM)
        initializationLog?.let { appendLog(it) }
    }

    override fun setAugmentedUIState(
        isLoading: Boolean,
        buttonState: ARButtonState,
        currentRoomId: Int?,
        clickAction: () -> Unit
    ) {
        if(isLoading) {
            animationView.visibility = View.VISIBLE
            animationView.playAnimation()
        } else {
            animationView.visibility = View.GONE
            animationView.pauseAnimation()
        }

        when(buttonState) {
            ARButtonState.STATE_UNDEFINED ->  {
                buttonExploreRoom?.alpha = 0.9f
                buttonExploreRoom?.isEnabled = false
            }
            ARButtonState.STATE_EXIT -> {
                buttonExploreRoom?.alpha = 1f
                buttonExploreRoom.text = "Exit room $currentRoomId"
                buttonExploreRoom?.isEnabled = true
            }
            ARButtonState.STATE_EXPLORE -> {
                buttonExploreRoom?.alpha = 1f
                buttonExploreRoom?.text = "Explore room"
                buttonExploreRoom?.isEnabled = true
            }
        }

        buttonExploreRoom?.setOnClickListener { clickAction.invoke() }
    }

    override fun appendLog(log: LogItem) {
        logsAdapter.appendLog(log)
        logsRecyclerView?.scrollToPosition(logsAdapter.logs.size - 1)
    }

    override fun navigateToVoiceFragment() {
        showAugmentedRoomUI(false)
        var fragment = VoiceFragment()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameTarget)
        if(currentFragment != null && currentFragment is VoiceFragment)
            fragment = currentFragment
        navigateTo(fragment)
    }

    override fun navigateToRenderablesFragment(query: String) {
        logsRecyclerView.visibility = View.GONE
        showAugmentedRoomUI(false)
        var fragment =
            RenderableListFragment()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameTarget)
        if(currentFragment != null && currentFragment is RenderableListFragment)
            fragment = currentFragment

        val bundle = Bundle()
        bundle.putString(ARG_QUERY_VALUE, query)
        fragment.arguments = bundle
        navigateTo(fragment)
    }

    override fun navigateToAugmentedFragment() {
        logsRecyclerView.visibility = View.VISIBLE
        showAugmentedRoomUI(true)
        var fragment = ARFragment()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameTarget)
        if(currentFragment != null && currentFragment is ARFragment)
            fragment = currentFragment
        navigateTo(fragment)
    }

    override fun showAugmentedRoomUI(value: Boolean) {
        buttonExploreRoom.visibility = if(value) View.VISIBLE else View.GONE
    }

    private fun navigateTo(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frameTarget, fragment).addToBackStack("tag")
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.commit()
    }

}
