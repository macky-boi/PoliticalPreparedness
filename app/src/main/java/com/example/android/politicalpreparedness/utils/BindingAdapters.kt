package com.example.android.politicalpreparedness.utils

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.models.ElectionUi
import com.example.android.politicalpreparedness.network.models.Election

object BindingAdapters {
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<Election>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? ElectionListAdapter)?.setData(itemList)
        }
    }
}