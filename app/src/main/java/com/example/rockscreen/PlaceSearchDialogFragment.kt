package com.example.rockscreen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment

class PlaceSearchDialogFragment : DialogFragment() {
    interface PlaceSearchDialogListener {
        fun onCloseButtonClicked()
        fun onSearchButtonClicked()
    }

    private var listener: PlaceSearchDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PlaceSearchDialogListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement PlaceSearchDialogListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_place_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 버튼 이벤트 처리
        view.findViewById<ImageView>(R.id.close_icon_iv).setOnClickListener {
            listener?.onCloseButtonClicked()
            dismiss()
        }

        // 검색 버튼 이벤트 처리
        view.findViewById<ConstraintLayout>(R.id.dialog_contents_search_cl).setOnClickListener {
            listener?.onSearchButtonClicked()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                262f,
                resources.displayMetrics
            ).toInt()
            dialog.window?.setLayout(width, height)
        }
    }
}
