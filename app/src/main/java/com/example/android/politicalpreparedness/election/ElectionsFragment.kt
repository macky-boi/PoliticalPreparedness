package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.android.politicalpreparedness.PoliticalPreparednessApplication
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.network.models.Election
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
        Timber.d("onCreateView")
        // TODO: Add ViewModel values and create ViewModel

        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        val factory = ElectionsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(ElectionsViewModel::class.java)

        // TODO: Add binding values
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_election, container,false)

        // TODO: Link elections to voter info

        // TODO: Initiate recycler adapters

        // TODO: Populate recycler adapters
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.electionsRecycleView.apply {
            layoutManager = LinearLayoutManager(this.context)
            val adapter = ElectionListAdapter(ElectionListener { election ->
                viewModel.navigateToInfo(election)
            })
            this.adapter = adapter
        }

        viewModel.elections.observe(viewLifecycleOwner, Observer { elections ->
            Timber.d("elections: $elections")})

        viewModel.navigateToInfo.observe(viewLifecycleOwner, Observer { election ->
            findNavController().navigate(
                ElectionsFragmentDirections
                    .actionElectionsFragmentToVoterInfoFragment(election.id, election.division))})
    }

    // TODO: Refresh adapters when fragment loads
}