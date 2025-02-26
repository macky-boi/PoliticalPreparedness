package com.example.android.politicalpreparedness

import com.example.android.politicalpreparedness.network.CivicsApi

interface AppContainer {
    val politicalPreparednessRepository: PoliticalPreparednessRepository
}

class DefaultAppContainer: AppContainer {
    override val politicalPreparednessRepository: PoliticalPreparednessRepository by lazy {
        PoliticalPreparednessRepositoryImpl(CivicsApi.retrofitService)
    }
}