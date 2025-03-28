package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.example.android.politicalpreparedness.election.VoterInfoViewModel
import com.example.android.politicalpreparedness.network.models.Division

class RepresentativeViewModelFactory(
    private val repository: PoliticalPreparednessRepository,
    private val application: Application
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepresentativeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepresentativeViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}