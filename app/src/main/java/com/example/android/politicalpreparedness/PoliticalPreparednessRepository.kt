package com.example.android.politicalpreparedness

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
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
    suspend fun getRepresentatives(address: String): Result<RepresentativeResponse>
}

class PoliticalPreparednessRepositoryImpl(
    private val service: CivicsApiService,
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

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: Exception) {
            val error = when (e) {
                is SocketTimeoutException -> "Request timed out"
                is UnknownHostException -> "No internet connection"
                is IOException -> "Network error: ${e.localizedMessage}"
                is retrofit2.HttpException -> "HTTP error ${e.code()}: ${e.message()}"
                is JsonDataException -> "JSON parsing error: ${e.localizedMessage}"
                is CancellationException -> "Operation was cancelled"
                else -> "Unknown error: ${e.localizedMessage}"
            }
            Timber.e(error)
            Result.failure(e)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getElections() : Result<ElectionResponse> =
        safeApiCall { service.getElections() }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getVoterInfo(address: String, electionId: Int): Result<VoterInfoResponse> =
        safeApiCall { service.getVoterInfo(address, electionId) }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getRepresentatives(address: String): Result<RepresentativeResponse> =
        safeApiCall { service.getRepresentatives(address) }

}