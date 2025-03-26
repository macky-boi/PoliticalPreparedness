package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.PoliticalPreparednessApplication
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import kotlinx.io.IOException
import timber.log.Timber
import java.util.Locale

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var locationProviderClient: FusedLocationProviderClient

    //TODO: Declare ViewModel (x)
    private lateinit var viewModel: RepresentativeViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        viewModel = ViewModelProvider(this,
            RepresentativeViewModelFactory(repository)
        )[RepresentativeViewModel::class.java]

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_representative, container,false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Establish bindings (x)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModelBinding = viewModel
            setupRecycleView(binding.electionsRecycleView)

            addressLine1.doAfterTextChanged { text ->
                viewModel.updateLine1(text.toString())
            }
            addressLine2.doAfterTextChanged { text ->
                viewModel.updateLine2(text.toString())
            }
            city.doAfterTextChanged { text ->
                viewModel.updateCity(text.toString())
            }
            state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedState = parent?.getItemAtPosition(position).toString()
                    viewModel.updateState(selectedState)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            zip.doAfterTextChanged { text ->
                viewModel.updateZip(text.toString())
            }

            //TODO: Establish button listeners for field and location search (x)
            buttonSearch.setOnClickListener {
                viewModel.fetchRepresentatives()
                hideKeyboard()
            }
            buttonSearchUsingDeviceLocation.setOnClickListener {
                myLocationOnClick()
                viewModel.toggleIsLoading()
                hideKeyboard()
            }

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                Timber.d("isLoading: $isLoading")
                electionsRecycleView.visibility = if (isLoading) View.GONE else View.VISIBLE
                listPlaceholder.visibility =  if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun myLocationOnClick() {
        if (!requireLocationPermissions()) return
        verifyLocationSettings {
            fetchRepresentativesUsingAddress()
        }
    }

    private fun requireLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            Timber.d("location permission is already granted")
            true
        } else {
            //TODO: Request Location permissions
            requestLocationPermissions()
            false
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Timber.d("request location Permission GRANTED")
        } else {
            Timber.d("request location DENIED")
            showSnackBar(R.string.permission_denied_explanation, Snackbar.LENGTH_LONG) { displayAppSettingsScreen() }
        }
    }

    private fun requestLocationPermissions() {
        Timber.d("requesting location permission")
        requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    //TODO: Define and assign Representative adapter (x)
    //TODO: Populate Representative adapter (x)
    private fun setupRecycleView(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RepresentativeListAdapter()
        }
    }

    private val requestLocationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { retryCheckLocationSettings() }

    private fun retryCheckLocationSettings() {
        // Delay to ensure system has applied the settings
        Handler(Looper.getMainLooper()).postDelayed({
            verifyLocationSettings(false) { fetchRepresentativesUsingAddress() }
        }, 200)
    }

    private fun verifyLocationSettings(resolve: Boolean = true, onSuccess: () -> Unit) {
        val locationRequest = LocationRequest.Builder(0)
            .setPriority(Priority.PRIORITY_LOW_POWER)
            .build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnSuccessListener {
            Timber.d("location settings are enabled")
            onSuccess()
        }.addOnFailureListener { exception ->
            Timber.d("location settings are off")
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    requestLocationSettingsLauncher.launch(intentSenderRequest)
                    Timber.d("Prompting user to enable location settings")
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.e(exception, "Error getting location settings resolution")
                }
            } else {
                showSnackBar(R.string.location_required_error, Snackbar.LENGTH_LONG) { verifyLocationSettings { onSuccess() } }
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
        }

    }


    private fun showSnackBar(stringId: Int, length: Int, action: () -> Unit) {
        Snackbar.make(
            binding.root,
            stringId,
            length
        ).setAction(R.string.settings) {
            action()
        }.show()
    }

    private fun displayAppSettingsScreen() {
        startActivity(Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun isPermissionGranted() : Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        return ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun fetchRepresentativesUsingAddress() {
        getLocation { location ->
            getAddress(location) { address ->
                address?.let {
                    viewModel.fetchRepresentativesUsingAddress(address)
                }
            }
        }
    }

    private fun getAddress(location: Location, onAddressFound: (Address?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

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
                        onAddressFound(null)
                    }
                })
        } else {
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
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}