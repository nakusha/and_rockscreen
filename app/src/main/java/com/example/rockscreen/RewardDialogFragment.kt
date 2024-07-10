package com.example.rockscreen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

class RewardDialogFragment : DialogFragment() {
    interface RewardDialogListener {
        fun onConfirm()
    }

    private var listener: RewardDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RewardDialogListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement RewardDialogListener")
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
        return inflater.inflate(R.layout.dialog_reward, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 버튼 이벤트 처리
        view.findViewById<ImageView>(R.id.dialog_reward_iv).setOnClickListener {
            listener?.onConfirm()
            dismiss()
        }
    }
}
