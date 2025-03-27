package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.Office
import com.example.android.politicalpreparedness.network.models.Official
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class RepresentativeViewModel(
    private val repository: PoliticalPreparednessRepository
): ViewModel() {

    //TODO: Establish live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>(emptyList())
    val representatives: LiveData<List<Representative>> = _representatives

    private val _address = MutableLiveData<Address>(
        Address("", null, "","",""))
    val address : LiveData<Address> = _address

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading : LiveData<Boolean> = _isLoading

    /**
    The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :
    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives
     */
    private fun getRepresentativesDeferred() : Deferred<Pair<List<Office>, List<Official>>> =
        viewModelScope.async {
            address.value?.let { address ->
                Timber.d("address: ${address.toApiServiceString()}")
                val result = repository.getRepresentatives(address.toApiServiceString())
                result.fold(
                    onSuccess = { representatives ->
                        Timber.d("Successfully retrieved representatives from network")
                        Pair(representatives.offices, representatives.officials)
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to retrieve representatives from network")
                        Pair(emptyList(), emptyList())
                    }
                )
            } ?: Pair(emptyList(), emptyList())
        }

    fun fetchRepresentativesUsingAddress(address: Address) {
        updateAddress(address)
        fetchRepresentatives()
    }


    //TODO: Create function to fetch representatives from API from a provided address
    fun fetchRepresentatives() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            val (offices, officials) = getRepresentativesDeferred().await()
            _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
            _isLoading.postValue(false)
            Timber.d("update representatives: ${_representatives.value}")
        }
    }


    //TODO: Create function get address from geo location

    //TODO: Create function to get address from individual fields (x)
    fun updateLine1(text: String) {
        _address.value = _address.value?.copy(line1 = text)
    }

    fun updateLine2(text: String) {
        _address.value = _address.value?.copy(line2 = text)
    }

    fun updateCity(text: String) {
        _address.value = _address.value?.copy(city = text)
    }

    fun updateState(text: String) {
        _address.value = _address.value?.copy(state = text)
    }

    fun updateZip(text: String) {
        _address.value = _address.value?.copy(zip = text)
    }

    private fun updateAddress(address: Address) {
        Timber.d("updateAddress | address: $address")
        _address.postValue(address)
    }

    fun toggleIsLoading() {
        _isLoading.value = isLoading.value?.not()
    }

}
