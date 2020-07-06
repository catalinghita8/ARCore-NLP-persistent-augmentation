package com.example.arvoice.ui.main.renderables

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arvoice.R
import com.example.arvoice.domain.RenderableAsset
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_renderable_asset.view.*


class RenderableAssetsRecyclerViewAdapter(
    private val context: Context,
    private val listener: RenderableListClickListener?
) : RecyclerView.Adapter<RenderableAssetsRecyclerViewAdapter.ViewHolder>() {

    private var renderables: List<RenderableAsset> = listOf()
    private val mOnClickListener: View.OnClickListener

    fun setAssets(renderables: List<RenderableAsset>) {
        this.renderables = renderables
        notifyDataSetChanged()
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as RenderableAsset
            listener?.onRenderableAssetPressed(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_renderable_asset, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = renderables[position]
        holder.textTitle.text = item.name
        Picasso.with(context)
            .load(item.pictureUrl)
            .fit().into(holder.imageBackground)

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = renderables.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val textTitle: TextView = mView.textTitle
        val imageBackground: ImageView = mView.imageBackground

    }

    interface RenderableListClickListener {
        fun onRenderableAssetPressed(item: RenderableAsset)
    }
}
