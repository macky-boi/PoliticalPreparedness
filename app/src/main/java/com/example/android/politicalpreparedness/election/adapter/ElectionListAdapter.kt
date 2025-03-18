package com.example.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ItemElectionBinding
import com.example.android.politicalpreparedness.election.models.ElectionUi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.toUiModel
import timber.log.Timber

class ElectionListAdapter(private val clickListener: ElectionListener)
    : ListAdapter<Election, ElectionViewHolder>(ElectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        Timber.d("creating ViewHolder")
        return ElectionViewHolder.from(parent)
    }

    // TODO: Bind ViewHolder
    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        Timber.d("binding viewHolder | position: $position, holder: $holder")
        val election = getItem(position)
        holder.bind(clickListener, election)
    }

}

// TODO: Create ElectionViewHolder
class ElectionViewHolder private constructor(private val binding: ItemElectionBinding) :
    RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: ElectionListener, item: Election) {
            Timber.d("bind | item: $item")
            binding.election= item
            binding.electionUi = item.toUiModel()
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    // TODO: Add companion object to inflate ViewHolder (from)
        companion object {
            fun from(parent: ViewGroup): ElectionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemElectionBinding.inflate(layoutInflater, parent, false)
                return  ElectionViewHolder(binding)
            }
        }
    }

// TODO: Create ElectionDiffCallback
class ElectionDiffCallback: DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem == newItem
    }
}

// TODO: Create ElectionListener
class ElectionListener(val clickListener: (election: Election) -> Unit) {
    fun onClick(election: Election) = clickListener(election)
}
