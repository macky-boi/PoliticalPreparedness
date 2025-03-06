package com.example.android.politicalpreparedness

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.election.VoterInfoViewModel
import com.example.android.politicalpreparedness.election.VoterInfoViewModel.Companion
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.squareup.moshi.JsonDataException
import kotlinx.io.IOException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

interface PoliticalPreparednessRepository {
    suspend fun getElections() : Result<ElectionResponse>
    suspend fun getVoterInfo(address: String, electionId: Int) : Result<VoterInfoResponse>
    suspend fun saveElection(election: Election)
    fun getSavedElections() : LiveData<List<Election>>
    fun getSavedElection(id: Int): LiveData<Election?>
    suspend fun deleteElection(id: Int)
}

class PoliticalPreparednessRepositoryImpl(
    private val civicsApiService: CivicsApiService,
    private val electionDao: ElectionDao
) : PoliticalPreparednessRepository {


    override suspend fun saveElection(election: Election) =
        runCatching { electionDao.insertElection(election) }
            .onSuccess { Timber.d("Successfully saved election") }
            .onFailure { Timber.e(it, "Error saving election") }
            .getOrThrow()

    override fun getSavedElection(id: Int): LiveData<Election?> =
        electionDao.getElection(id)

    override fun getSavedElections() : LiveData<List<Election>> =
        electionDao.getAllElections()


    override suspend fun deleteElection(id: Int) {
        try {
            return electionDao.deleteElection(id)
        } catch (e: Exception) {
            Timber.e(e,"Error deleting Election from database")
            throw e
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: JsonDataException) {
            Result.failure(e)
        } catch (e: SocketTimeoutException) {
            Result.failure(e)
        } catch (e: UnknownHostException) {
            Result.failure(e)
        } catch (e: CancellationException) {
            Result.failure(e)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getElections() : Result<ElectionResponse> =
        safeApiCall { civicsApiService.getElections() }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getVoterInfo(address: String, electionId: Int): Result<VoterInfoResponse> =
        safeApiCall { civicsApiService.getVoterInfo(address, electionId) }



}