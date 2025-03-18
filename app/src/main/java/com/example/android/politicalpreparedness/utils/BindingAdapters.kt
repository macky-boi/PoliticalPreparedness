package com.example.android.politicalpreparedness.utils

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.models.ElectionUi
import com.example.android.politicalpreparedness.network.models.Election
import timber.log.Timber

object BindingAdapters {
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        Timber.d("layout liveData | items: ${items?.value}")
        items?.value?.let { itemList ->
            (recyclerView.adapter as? ListAdapter<T, RecyclerView.ViewHolder>)?.submitList(itemList)
        }
    }
}