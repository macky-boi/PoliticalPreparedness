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
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
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
    private val _voterInfo = MutableLiveData<VoterInfoResponse>(null)
    val voterInfo: LiveData<VoterInfoResponse> = _voterInfo

    val name = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo.election.name
        }
    }

    val date = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo.election.electionDay.toString()
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

    val correspondenceAddress = MediatorLiveData<String>().apply {
        addSource(voterInfo) { voterInfo ->
            this.value = voterInfo?.state?.get(0)?.electionAdministrationBody?.correspondenceAddress?.toFormattedString()
        }
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

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

}