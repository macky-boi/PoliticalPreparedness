package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.politicalpreparedness.PoliticalPreparednessRepository
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import timber.log.Timber

class RepresentativeViewModel(
    repository: PoliticalPreparednessRepository
): ViewModel() {

    init {
        Timber.d("init")
    }

    //TODO: Establish live data for representatives and address
    private val _representatives = MutableLiveData<RepresentativeResponse?>(null)
    private val _address = MutableLiveData<Address>(
        Address("", null, "","","")
    )
    val address : LiveData<Address> = _address

    //TODO: Create function to fetch representatives from API from a provided address


    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */

    suspend fun getRepresentatives(address: String) {

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

}
