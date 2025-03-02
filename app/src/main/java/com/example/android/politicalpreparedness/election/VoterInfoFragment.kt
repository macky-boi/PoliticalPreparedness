package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.PoliticalPreparednessApplication
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.Division
import timber.log.Timber

class VoterInfoFragment : Fragment() {

    private lateinit var viewModel: VoterInfoViewModel
    private lateinit var binding: FragmentVoterInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {
        val args: VoterInfoFragmentArgs by navArgs()
        val electionId = args.argElectionId
        val division = args.argDivision
        Timber.d("onCreateView | electionId: $electionId, division: $division")


        // TODO: Add ViewModel values and create ViewModel
        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        val factory = VoterInfoViewModelFactory(repository, electionId, division)
        viewModel = ViewModelProvider(this, factory)[VoterInfoViewModel::class.java]

        // TODO: Add binding values
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_voter_info, container,false)

        // TODO: Populate voter info -- hide views without provided data.

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
        */

        // TODO: Handle loading of URLs

        // TODO: Handle save button UI state
        // TODO: cont'd Handle save button clicks
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.voterInfo.observe(viewLifecycleOwner, Observer { voterInfoResponse ->
            Timber.d("voterInfoResponse: $voterInfoResponse")
            val input = voterInfoResponse?.state?.firstOrNull()?.name ?: ""
            binding.stateHeader.text = getString(R.string.header_state, input)
        })

        viewModel.navigateToElections.observe(viewLifecycleOwner, Observer {
            if (it) findNavController().popBackStack()
        })

        viewModel.ballotInfoUrl.observe(viewLifecycleOwner, Observer { url ->
            Timber.d("ballotInfoUrl: $url")
            if (url !== null) {
                binding.stateBallot.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
        })

        viewModel.correspondenceAddress.observe(viewLifecycleOwner, Observer { correspondenceAddress ->
            Timber.d("correspondenceAddress: $correspondenceAddress")
        })

        binding.saveElectionButton.setOnClickListener {
            viewModel.saveElection()
        }

        viewModel.election.observe(viewLifecycleOwner, Observer { election ->
            binding.saveElectionButton.isEnabled = election != null
        })

    }

    // TODO: Create method to load URL intents
}