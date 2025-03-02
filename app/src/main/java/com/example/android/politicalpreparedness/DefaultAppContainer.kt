package com.example.android.politicalpreparedness

import android.content.Context
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi

interface AppContainer {
    val politicalPreparednessRepository: PoliticalPreparednessRepository
}

class DefaultAppContainer(private val context: Context): AppContainer {
    override val politicalPreparednessRepository: PoliticalPreparednessRepository by lazy {
        PoliticalPreparednessRepositoryImpl(CivicsApi.retrofitService, ElectionDatabase.getInstance(context).electionDao)
    }
}