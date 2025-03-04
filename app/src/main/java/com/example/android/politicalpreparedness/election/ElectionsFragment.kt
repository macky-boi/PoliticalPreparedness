package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.PoliticalPreparednessApplication
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import timber.log.Timber

class ElectionsFragment: Fragment() {

    // TODO: Declare ViewModel

    private lateinit var viewModel: ElectionsViewModel
    private lateinit var binding: FragmentElectionBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {
        // TODO: Add ViewModel values and create ViewModel (x)
        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        viewModel = ViewModelProvider(this, ElectionsViewModelFactory(repository))[ElectionsViewModel::class.java]

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_election, container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Add binding values (x)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ElectionsFragment.viewModel
        }

        // TODO: Initiate recycler adapters (x)
        // TODO: Populate recycler adapters (x)
        // TODO: Refresh adapters when fragment loads (x)
        setupRecyclerView(binding.electionsRecycleView)
        setupRecyclerView(binding.savedElectionsRecycleView)

        // TODO: Link elections to voter info (x)
        viewModel.navigateToInfo.observe(viewLifecycleOwner) { election ->
            findNavController().navigate(
                ElectionsFragmentDirections
                    .actionElectionsFragmentToVoterInfoFragment(election.id, election.division))}
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ElectionListAdapter(ElectionListener { election ->
                viewModel.navigateToInfo(election)
            })
        }
    }
}

