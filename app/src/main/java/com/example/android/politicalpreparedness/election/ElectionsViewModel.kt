package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber

//TODO: Construct ViewModel and provide election datasource (x)
class ElectionsViewModel(
    private val repository: PoliticalPreparednessRepository
): ViewModel() {
    private val _navigateToInfo = SingleLiveEvent<Election>()
    val navigateToInfo: LiveData<Election> = _navigateToInfo

    //TODO: Create live data val for upcoming elections (x)
    private val _elections = MutableLiveData<List<Election>>(emptyList())
    val elections: LiveData<List<Election>> = _elections

    //TODO: Create live data val for saved elections (x)
    val savedElections = repository.getSavedElections()

    init {
        refreshElections()
    }

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database (x)
    private fun refreshElections() {
        viewModelScope.launch {
            val electionResponse = repository.getElections()
            electionResponse.onSuccess { election ->
                Timber.d("successfully retrieved election response")
                _elections.value = election.elections
            } .onFailure {
                Timber.e(it, "Error retrieving election response from the network")
            }
        }
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info (x)
    fun navigateToInfo(election: Election) {
        _navigateToInfo.value = election
    }

}