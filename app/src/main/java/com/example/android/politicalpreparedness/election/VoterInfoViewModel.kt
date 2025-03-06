package com.example.android.politicalpreparedness.election


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.lifecycle.map

class VoterInfoViewModel(
    private val repository: PoliticalPreparednessRepository,
    private val electionId: Int,
    private val division: Division
) : ViewModel() {

    companion object {
        private const val DEFAULT_STATE = "ca"
    }

    //TODO: Add live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse?>(null)

    private val savedElection = repository.getSavedElection(electionId)

    private val _navigateToElections = SingleLiveEvent<Boolean>()
    val navigateToElections: LiveData<Boolean> = _navigateToElections


    val isElectionSaved = savedElection.map { election ->
        election != null
    }

    val name = _voterInfo.map { it?.election?.name }


    val state = _voterInfo.map { voterInfo -> voterInfo?.state?.firstOrNull()?.name }

    val election = MediatorLiveData<Election?>(null).apply {
        addSource(_voterInfo) { voterInfo ->
            value = voterInfo?.election
        }
    }

    val date = _voterInfo.map { voterInfo ->
        val electionDay = voterInfo?.election?.electionDay
        electionDay?.toString() ?: ""
    }


    val votingLocationUrl = _voterInfo.map { voterInfo ->
        voterInfo?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl }


    val ballotInfoUrl = _voterInfo.map { voterInfo ->
        voterInfo?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl }


    val correspondenceAddress = _voterInfo.map { voterInfo ->
        voterInfo?.state?.get(0)?.electionAdministrationBody?.correspondenceAddress?.toFormattedString() }

    //TODO: Populate initial state of save button to reflect proper action based on election saved status (x)

    val isSaveButtonEnabled = election.map { election ->
        election != null
    }

    val isBallotButtonEnabled = ballotInfoUrl.map { url ->
        !url.isNullOrEmpty()
    }


    //TODO: Add var and methods to populate voter info
    init {
        refreshVoterInfo()
    }


    private fun refreshVoterInfo() {
        val state = division.state.ifEmpty { DEFAULT_STATE }
        val address = "${state},${division.country}"
        viewModelScope.launch {
            repository.getVoterInfo(address, electionId).onSuccess { voterInfoResponse ->
                _voterInfo.value = voterInfoResponse
            }.onFailure { e ->
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Timber.e("HTTP ${e.code()} error: $errorBody")
                }
                Timber.e(e)
            }
        }
    }



    //TODO: Add var and methods to save and remove elections to local database (x)
    fun saveElection() {
        viewModelScope.launch {
            election.value?.let {
                repository.saveElection(it)
            }
        }
    }
    fun removeElection() {
        viewModelScope.launch {
            election.value?.let {
                repository.deleteElection(it.id)
            }
        }
    }

    //TODO: Add var and methods to support loading URLs


    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

}