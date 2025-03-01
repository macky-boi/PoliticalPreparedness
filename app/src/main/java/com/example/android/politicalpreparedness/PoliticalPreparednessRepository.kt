package com.example.android.politicalpreparedness

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.android.politicalpreparedness.election.VoterInfoViewModel
import com.example.android.politicalpreparedness.election.VoterInfoViewModel.Companion
import com.example.android.politicalpreparedness.network.CivicsApiService
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
}

class PoliticalPreparednessRepositoryImpl(
    private val civicsApiService: CivicsApiService) : PoliticalPreparednessRepository {
        @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)

    companion object {
        private const val DEFAULT_STATE = "ca"
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getElections() : Result<ElectionResponse> {
        return try {
            val response = civicsApiService.getElections()
            Result.success(response)
        } catch (ioE: IOException) {
            Result.failure(ioE)
        } catch (httpE: HttpException) {
            Result.failure(httpE)
        } catch (jsonDataE: JsonDataException) {
            Result.failure(jsonDataE)
        } catch (socketTimeoutE: SocketTimeoutException) {
            Result.failure(socketTimeoutE)
        } catch (unknownE: UnknownHostException) {
            Result.failure(unknownE)
        } catch (cancellationE: CancellationException) {
            Result.failure(cancellationE)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getVoterInfo(address: String, electionId: Int): Result<VoterInfoResponse> {
        return try {
            val response = civicsApiService.getVoterInfo(address, electionId)
            Result.success(response)
        } catch (ioE: IOException) {
            Result.failure(ioE)
        } catch (httpE: retrofit2.HttpException) {
            Result.failure(httpE)
        } catch (jsonDataE: JsonDataException) {
            Result.failure(jsonDataE)
        } catch (socketTimeoutE: SocketTimeoutException) {
            Result.failure(socketTimeoutE)
        } catch (unknownE: UnknownHostException) {
            Result.failure(unknownE)
        } catch (cancellationE: CancellationException) {
            Result.failure(cancellationE)
        }
    }
}