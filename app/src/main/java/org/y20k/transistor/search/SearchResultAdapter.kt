package org.y20k.transistor.dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.y20k.transistor.R
import org.y20k.transistor.core.Station
import org.y20k.transistor.search.SearchResultAdapter

class AddStationDialog(
    private val context: Context, 
    private val stationList: List<Station>, 
    private val listener: AddStationDialogListener
) : SearchResultAdapter.SearchResultAdapterListener {

    interface AddStationDialogListener {
        fun onAddStationDialog(station: Station)
    }

    private val TAG = AddStationDialog::class.java.simpleName
    private lateinit var dialog: AlertDialog
    private lateinit var stationSearchResultList: RecyclerView
    private lateinit var searchResultAdapter: SearchResultAdapter
    private var station: Station = Station()

    override fun onSearchResultTapped(result: Station) {
        station = result
        if (::dialog.isInitialized) {
            activateAddButton()
        }
    }

    fun show() {
        val builder = MaterialAlertDialogBuilder(context, R.style.Theme_Transistor_AlertDialog)
            .setTitle(R.string.dialog_add_station_title)
            .setPositiveButton(R.string.dialog_find_station_button_add) { _, _ ->
                listener.onAddStationDialog(station)
            }
            .setNegativeButton(R.string.dialog_generic_button_cancel, null)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_station, null)
        stationSearchResultList = view.findViewById(R.id.station_list)
        setupRecyclerView(context)

        dialog = builder.setView(view).create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    private fun setupRecyclerView(context: Context) {
        searchResultAdapter = SearchResultAdapter(this, stationList)
        stationSearchResultList.apply {
            adapter = searchResultAdapter
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations() = true
            }
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun activateAddButton() {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
    }
}
