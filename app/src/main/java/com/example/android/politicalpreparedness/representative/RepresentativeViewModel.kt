package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.lifecycle.AndroidViewModel
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import java.io.IOException

class RepresentativeViewModel(
    private val repository: PoliticalPreparednessRepository,
    application: Application
): AndroidViewModel(application) {

    //TODO: Establish live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>(emptyList())
    val representatives: LiveData<List<Representative>> = _representatives

    private val _address = MutableLiveData<Address>(
        Address("", null, "","",""))
    val address : LiveData<Address> = _address

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading : LiveData<Boolean> = _isLoading

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())

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

    private fun updateAddress(address: Address) {
        Timber.d("updateAddress | address: $address")
        _address.postValue(address)
    }

    fun fetchRepresentativesUsingAddress() {
        getLocation { location ->
            getAddress(location) { address ->
                address?.let {
                    updateAddress(address)
                    fetchRepresentatives()
                }
            }
        }
    }

    //TODO: Get location from LocationServices (x)
    //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
    private fun getLocation(onLocationFound: (Location) -> Unit) {
        val cancellationTokenSource = CancellationTokenSource()

        try {
            locationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token).addOnSuccessListener { location ->
                    onLocationFound(location)
            }
        } catch (e: SecurityException) {
            Timber.e(e,"getLocation | Permissions are not granted")
            _isLoading.value = false
        }
    }

    private fun getAddress(location: Location, onAddressFound: (Address?) -> Unit) {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        val address = addresses.firstOrNull()?.let { addr ->
                            Address(
                                addr.thoroughfare,
                                addr.subThoroughfare,
                                addr.locality,
                                addr.adminArea,
                                addr.postalCode
                            )
                        }
                        onAddressFound(address)
                    }

                    override fun onError(errorMessage: String?) {
                        Timber.e("Geocoding error: $errorMessage")
                        _isLoading.value = false
                        onAddressFound(null)
                    }
                })
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addressList?.firstOrNull()?.let { addr ->
                        Timber.d("getAddress | addr: $addr")
                        Address(addr.thoroughfare, addr.subThoroughfare, addr.locality, addr.adminArea, addr.postalCode)
                    }
                    onAddressFound(address)
                } catch (e: IOException) {
                    Timber.e(e, "Geocoding failed")
                    onAddressFound(null)
                    _isLoading.value = false
                }
            }
        }
    }

    //TODO: Create function to fetch representatives from API from a provided address
    fun fetchRepresentatives() {
        viewModelScope.launch {
            _isLoading.value = true
            val (offices, officials) = withContext(Dispatchers.IO) {
                getRepresentativesDeferred().await()
            }

            _representatives.value = withContext(Dispatchers.Default) {
                offices.flatMap { office ->
                    office.getRepresentatives(officials)
                }
            }

            _isLoading.value = false

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

    fun updateIsLoading(boolean: Boolean) {
        _isLoading.value = boolean
    }

}
