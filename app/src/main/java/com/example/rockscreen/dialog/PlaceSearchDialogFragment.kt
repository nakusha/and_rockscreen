package com.example.rockscreen.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rockscreen.DividerItemDecoration
import com.example.rockscreen.Place
import com.example.rockscreen.PlaceAdapter
import com.example.rockscreen.R

class PlaceSearchDialogFragment : DialogFragment() {
    interface PlaceSearchDialogListener {
        fun onCloseButtonClicked()
        fun onSearchButtonClicked(place: Place)
    }

    private var listener: PlaceSearchDialogListener? = null
    private lateinit var dialogContentsRoot: ConstraintLayout
    private lateinit var dialogContentsCl: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeAdapter: PlaceAdapter

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

        dialogContentsRoot = view.findViewById(R.id.dialog_contents_root)
        dialogContentsCl = view.findViewById(R.id.dialog_contents_cl)
        recyclerView = view.findViewById(R.id.recycler_view)

        // 닫기 버튼 이벤트 처리
        view.findViewById<ImageView>(R.id.close_icon_iv).setOnClickListener {
            listener?.onCloseButtonClicked()
            dismiss()
        }

        // 검색 버튼 이벤트 처리
        view.findViewById<ConstraintLayout>(R.id.dialog_contents_search_cl).setOnClickListener {
            //listener?.onSearchButtonClicked()
            enableRecyclerView()
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

    private fun enableRecyclerView() {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val examplePlaces = listOf(
            Place("우감만족", "소고기구이", "서울 강남구 압구정로54길 26 지상1층", "100m"),
            Place("새우공장 본점", "술집", "서울 강남구 압구정로54길 25 1층 새우공장", "200m"),
            Place("코드라운지", "바(BAR)", "서울 강남구 선릉로157길 12 지하1층", "300m"),
            Place("샵마고 푸드 편집샵", "아시아음식", "서울 강남구 압구정로54길 26 지하1층", "400m")
        )

        placeAdapter = PlaceAdapter(examplePlaces) {place ->
            listener?.onSearchButtonClicked(place)
            dismiss()
        }
        recyclerView.adapter = placeAdapter

        //val space = resources.getDimensionPixelSize(R.dimen.recycler_view_item_spacing)
        val space = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            4f,
            resources.displayMetrics
        ).toInt()
        recyclerView.addItemDecoration(DividerItemDecoration(space))

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                adjustDialogHeight()
            }
        })
    }

    private fun adjustDialogHeight() {
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                454f,
                resources.displayMetrics
            ).toInt()
            dialog.window?.setLayout(width, height)
        }

        val params1 = dialogContentsRoot.layoutParams
        val height1 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            454f,
            resources.displayMetrics
        ).toInt()
        params1.height = height1
        dialogContentsRoot.layoutParams = params1

        val params2 = dialogContentsCl.layoutParams
        val height2 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            428f,
            resources.displayMetrics
        ).toInt()
        params2.height = height2
        dialogContentsCl.layoutParams = params2
    }
}
