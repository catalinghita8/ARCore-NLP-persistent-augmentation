package com.example.arvoice.ui.main.dialog


import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.arvoice.R
import kotlinx.android.synthetic.main.layout_loading_view.*


class ARLoadingDialog: DialogFragment() {

    private var dismissLoading: OnDismissLoading? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(android.graphics.Color.TRANSPARENT))
        isCancelable = true
        return inflater.inflate(R.layout.layout_loading_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animationView.playAnimation()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        animationView.cancelAnimation()
        dismissLoading?.cancelRequest()
    }

    companion object {

        fun newInstance(): ARLoadingDialog{
            val fragment = ARLoadingDialog()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }

    }

    object ID {
        const val GENERIC_INSTANCE = "generic_loading"
    }

    interface OnDismissLoading{
        fun cancelRequest()
    }

}
