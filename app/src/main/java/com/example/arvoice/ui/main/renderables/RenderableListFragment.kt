package com.example.arvoice.ui.main.renderables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.arvoice.R
import com.example.arvoice.utils.Constants
import com.example.arvoice.domain.RenderableAsset
import com.example.arvoice.ui.main.ar.ARViewModel
import com.example.arvoice.ui.main.dialog.ARLoadingDialog
import com.example.arvoice.ui.main.navigation.MainNavigation
import kotlinx.android.synthetic.main.fragment_renderable_asset_list.*


class RenderableListFragment: Fragment(), RenderableAssetsRecyclerViewAdapter.RenderableListClickListener {

    private var columnCount = 2

    private lateinit var viewModel: ARViewModel
    private lateinit var adapter: RenderableAssetsRecyclerViewAdapter
    private var loadingDialog: ARLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ARViewModel::class.java)

        viewModel.getAssetListObservable().observe(this, Observer { assets ->
            displayAssets(assets)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_renderable_asset_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerRenderableItems.setHasFixedSize(false)
        recyclerRenderableItems.layoutManager = GridLayoutManager(activity!!, columnCount)
        adapter =
            RenderableAssetsRecyclerViewAdapter(
                context!!,
                this
            )
        recyclerRenderableItems.adapter = adapter

        loadingDialog = ARLoadingDialog.newInstance()
        val query = arguments?.getString(Constants.ARG_QUERY_VALUE, null)

        query?.let { viewModel.requestAssets(query) }
        viewModel.isLoadingLiveData.observe(this, Observer { isLoading ->
            showLoading(isLoading)
        })
    }

    override fun onRenderableAssetPressed(item: RenderableAsset) {
        viewModel.setSelectedAsset(item)
        (activity as MainNavigation).navigateToAugmentedFragment()
    }

    private fun displayAssets(assets: List<RenderableAsset>) {
        adapter.setAssets(assets)
    }

    private fun showLoading(visible: Boolean) {
        childFragmentManager.executePendingTransactions()
        if (visible && !loadingDialog!!.isAdded && !loadingDialog!!.isVisible) loadingDialog!!.show(
            childFragmentManager,
            ARLoadingDialog.ID.GENERIC_INSTANCE
        ) else if (loadingDialog!!.isAdded) loadingDialog!!.dismiss()
    }

}
