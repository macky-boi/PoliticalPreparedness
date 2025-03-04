package com.example.android.politicalpreparedness.election

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository

//TODO: Create Factory to generate ElectionViewModel with provided election datasource
class ElectionsViewModelFactory(
    private val repository: PoliticalPreparednessRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(ElectionsViewModel::class.java)  ->
                ElectionsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
}
