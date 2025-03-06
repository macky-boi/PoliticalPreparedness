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
        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
         */

        val args: VoterInfoFragmentArgs by navArgs()
        val electionId = args.argElectionId
        val division = args.argDivision


        // TODO: Add ViewModel values and create ViewModel (x)
        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        viewModel = ViewModelProvider(this,
            VoterInfoViewModelFactory(repository, electionId, division)
        )[VoterInfoViewModel::class.java]


        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_voter_info, container,false)

        return binding.root
    }

    // TODO: Populate voter info -- hide views without provided data.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            // TODO: Add binding values (x)
            lifecycleOwner = viewLifecycleOwner
            viewModelBinding = viewModel

            // TODO: Handle loading of URLs (x)
            stateBallot.setOnClickListener {
                onBallotClick()
            }

            // TODO: Handle save button clicks (x)
            saveElectionButton.setOnClickListener {
                onSaveElectionClick()
            }
        }

        viewModel.apply {
            navigateToElections.observe(viewLifecycleOwner) {
                if (it) findNavController().popBackStack()
            }

            isBallotButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
                binding.stateBallot.isEnabled = isEnabled
            }


            // TODO: Handle save button UI state (x)
            isSaved.observe(viewLifecycleOwner) { isSaved ->
                binding.saveElectionButton.text = if (isSaved)
                    getString(R.string.btn_unfollow_election)
                 else
                    getString(R.string.btn_follow_election)
            }
            isSaveButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
                binding.saveElectionButton.isEnabled = isEnabled != false
            }
            correspondenceAddress.observe(viewLifecycleOwner) { address ->
                Timber.d("address: $address")
            }

        }
    }

    private fun onBallotClick() {
        viewModel.ballotInfoUrl.value?.let { loadIntent(it) }
    }

    private fun onSaveElectionClick() {
        if (viewModel.isSaved.value == true)
            viewModel.removeElection() else
            viewModel.saveElection()
    }


    // TODO: Create method to load URL intents (x)
    private fun loadIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}