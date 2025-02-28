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
        // TODO: Add ViewModel values and create ViewModel (x)
        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        val factory = ElectionsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ElectionsViewModel::class.java]

        // TODO: Add binding values (x)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_election, container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        // TODO: Initiate recycler adapters (x)
        binding.electionsRecycleView.apply {
            layoutManager = LinearLayoutManager(this.context)
            val adapter = ElectionListAdapter(ElectionListener { election ->
                viewModel.navigateToInfo(election)
            })
            // TODO: Populate recycler adapters (x)
            this.adapter = adapter
        }

        viewModel.elections.observe(viewLifecycleOwner, Observer { elections ->
            Timber.d("elections: $elections")})

        // TODO: Link elections to voter info (x)
        viewModel.navigateToInfo.observe(viewLifecycleOwner, Observer { election ->
            Timber.d("observe viewModel.navigateToInfo | election: $election")
            findNavController().navigate(
                ElectionsFragmentDirections
                    .actionElectionsFragmentToVoterInfoFragment(election.id, election.division))})
    }

    // TODO: Refresh adapters when fragment loads (x)
//    override fun onResume() {
//        super.onResume()
//        viewModel.refreshElections()
//    }
}