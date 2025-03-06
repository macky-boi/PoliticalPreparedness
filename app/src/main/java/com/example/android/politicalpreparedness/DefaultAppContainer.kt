package com.example.android.politicalpreparedness

import android.content.Context
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService

interface AppContainer {
    val politicalPreparednessRepository: PoliticalPreparednessRepository
}

class DefaultAppContainer(private val context: Context): AppContainer {
    private val electionDao: ElectionDao by lazy {
        ElectionDatabase.getInstance(context).electionDao
    }

    private val civicsApiService: CivicsApiService by lazy {
        CivicsApi.retrofitService
    }

    override val politicalPreparednessRepository: PoliticalPreparednessRepository by lazy {
        PoliticalPreparednessRepositoryImpl(civicsApiService, electionDao)
    }
}