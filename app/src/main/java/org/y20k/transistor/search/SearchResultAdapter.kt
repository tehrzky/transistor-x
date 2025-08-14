// app/src/main/java/org/y20k/transistor/search/SearchResultAdapter.kt
package org.y20k.transistor.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.y20k.transistor.core.Station
import org.y20k.transistor.databinding.ListitemSearchResultBinding

class SearchResultAdapter(
    private val listener: SearchResultAdapterListener,
    private val stations: List<Station>
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    interface SearchResultAdapterListener {
        fun onSearchResultTapped(station: Station)
    }

    inner class ViewHolder(val binding: ListitemSearchResultBinding) : 
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListitemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = stations[position]
        holder.binding.station = station
        holder.binding.root.setOnClickListener {
            listener.onSearchResultTapped(station)
        }
    }

    override fun getItemCount(): Int = stations.size
}
