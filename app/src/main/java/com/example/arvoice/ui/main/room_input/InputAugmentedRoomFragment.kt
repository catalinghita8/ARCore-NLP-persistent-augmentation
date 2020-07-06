package com.example.arvoice.ui.main.room_input


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.example.arvoice.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_input_augmented_room.*


class InputAugmentedRoomFragment: BottomSheetDialogFragment() {

    var parentCallback: Callback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input_augmented_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editCommentText.isFocusableInTouchMode = true
        editCommentText.requestFocus()
        editCommentText.afterTextChanged { updateNextButtonState(it.isNotEmpty()) }

        buttonResolve.setOnClickListener {
            dismiss()
            parentCallback?.onResolveRoomPressed(editCommentText.text.toString().toInt())
        }
    }

    private fun updateNextButtonState(isInputValid: Boolean) {
        buttonResolve.isClickable = isInputValid
        buttonResolve.isEnabled = isInputValid
    }

    interface Callback {
        fun onResolveRoomPressed(roomId: Int)
    }

}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            afterTextChanged.invoke(p0.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })

}
