package com.example.android.politicalpreparedness.election

import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class VoterInfoViewModel(
//    private val dataSource: ElectionDao
    private val repository: PoliticalPreparednessRepository,
    private val electionId: Int,
    private val division: Division
) : ViewModel() {

    companion object {
        private const val DEFAULT_STATE = "ca"
    }

    //TODO: Add live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse?>(null)
    val voterInfo: LiveData<VoterInfoResponse?> = _voterInfo

    private val _navigateToElections = SingleLiveEvent<Boolean>()
    val navigateToElections: LiveData<Boolean> = _navigateToElections

    private val savedElection = repository.getSavedElection(electionId)

    val isSaved = MediatorLiveData<Boolean>(false).apply {
        addSource(savedElection) { savedElection ->
            this.value = savedElection !== null
        }
    }

    private fun updateButtonState() {
        val electionExists = election.value != null
        val alreadySaved = isSaved.value != false
        isSaveButtonEnabled.value = electionExists && !alreadySaved
    }

    val name = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo?.election?.name
        }
    }

    val state = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo?.state?.firstOrNull()?.name
        }
    }

    val election = MediatorLiveData<Election?>(null).apply {
        addSource(voterInfo) { voterInfo ->
            value = voterInfo?.election
        }
    }

    val isSaveButtonEnabled = MediatorLiveData<Boolean>(false).apply {
        addSource(election) { election ->
            value = election !== null
        }
    }


    val date = MediatorLiveData("").apply {
        addSource(voterInfo) { voterInfo ->
            val electionDay = voterInfo?.election?.electionDay
            value = electionDay?.toString() ?: ""
        }
    }

    val votingLocationUrl = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl.toString()
        }
    }

    val ballotInfoUrl = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
        }
    }

    val isBallotButtonEnabled = MediatorLiveData<Boolean>(false).apply {
        addSource(ballotInfoUrl) { url ->
            value = !url.isNullOrEmpty()
        }
    }

    val correspondenceAddress = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo?.state?.get(0)?.electionAdministrationBody?.correspondenceAddress?.toFormattedString()
        }
    }


    //TODO: Add var and methods to populate voter info
    init {
        refreshVoterInfo()
    }

    fun saveElection() {
        Timber.d("saveElection")
        viewModelScope.launch {
            repository.saveElection(election.value!!)
        }
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

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    fun removeElection() {
        Timber.d("removeElection")
        viewModelScope.launch {
            election.value?.let { repository.deleteElection(it.id) }
            Timber.d("successfully deleted election from database")
        }
    }


    //TODO: Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

}