package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(
    private val repository: PoliticalPreparednessRepository
): ViewModel() {

    //TODO: Create live data val for upcoming elections
    private val _elections = MutableLiveData<List<Election>>(emptyList())
    val elections: LiveData<List<Election>> = _elections

    init {
        Timber.d("initialize")
        viewModelScope.launch(Dispatchers.IO){
            val electionResponse = repository.getElections()
            electionResponse.onSuccess { election ->
                Timber.d("successfully retrieved election response")
                withContext(Dispatchers.Main) {
                    _elections.value = election.elections
                }
            } .onFailure {
                Timber.e(it, "Error retrieving election response from the network")
            }
        }
    }

    //TODO: Create live data val for saved elections

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database

    //TODO: Create functions to navigate to saved or upcoming election voter info

}