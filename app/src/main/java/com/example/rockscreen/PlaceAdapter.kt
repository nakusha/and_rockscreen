package com.example.rockscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

data class Place(val name: String, val subName: String, val address: String, val distance: String)

class PlaceAdapter(private val places: List<Place>, private val itemClickListener: (Place) -> Unit) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place, itemClickListener)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val placeRoot: ConstraintLayout = itemView.findViewById(R.id.place_root)
        private val placeName: TextView = itemView.findViewById(R.id.place_name)
        private val placeNameSub: TextView = itemView.findViewById(R.id.place_name_sub)
        private val placeAddress: TextView = itemView.findViewById(R.id.place_address)
        private val placeDistance: TextView = itemView.findViewById(R.id.place_distance)

        fun bind(place: Place, itemClickListener: (Place) -> Unit) {
            placeName.text = place.name
            placeNameSub.text = place.subName
            placeAddress.text = place.address
            placeDistance.text = place.distance

            placeRoot.setOnClickListener {
                itemClickListener(place)
            }
        }
    }
}