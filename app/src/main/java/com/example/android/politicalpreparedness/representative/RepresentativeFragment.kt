package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.PoliticalPreparednessApplication
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.election.VoterInfoViewModel
import com.example.android.politicalpreparedness.election.VoterInfoViewModelFactory
import com.example.android.politicalpreparedness.network.models.Address
import com.google.android.gms.common.api.GoogleApi.Settings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.Locale

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var locationProviderClient: FusedLocationProviderClient

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Timber.d("request location Permission GRANTED")
        } else {
            Timber.d("request location DENIED")
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                displayAppSettingsScreen()
            }.show()
        }
    }

    companion object {
        //TODO: Add Constant for Location request
//        private const val LOCATION_PERMISSION_INDEX = 0
//        private const val LOCATION_PERMISSIONS_REQUEST_CODE = 34
    }

    //TODO: Declare ViewModel (x)
    private lateinit var viewModel: RepresentativeViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        viewModel = ViewModelProvider(this,
            RepresentativeViewModelFactory(repository)
        )[RepresentativeViewModel::class.java]

        //TODO: Establish bindings
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_representative, container,false)

        //TODO: Define and assign Representative adapter

        //TODO: Populate Representative adapter

        //TODO: Establish button listeners for field and location search
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModelBinding = viewModel
        binding.addressLine1.doAfterTextChanged { text ->
            viewModel.updateLine1(text.toString())
        }
        binding.addressLine2.doAfterTextChanged { text ->
            viewModel.updateLine2(text.toString())
        }
        binding.city.doAfterTextChanged { text ->
            viewModel.updateCity(text.toString())
        }
        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        binding.zip.doAfterTextChanged { text ->
            viewModel.updateZip(text.toString())
        }

        binding.buttonSearch.setOnClickListener {
            viewModel.fetchRepresentatives()
        }

        viewModel.address.observe(viewLifecycleOwner) { address ->
            Timber.d("address: $address")
        }

        checkLocationPermissions()
    }

    private fun displayAppSettingsScreen() {
        startActivity(Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            Timber.d("location permission is already granted")
            true
        } else {
            //TODO: Request Location permissions
            requestLocationPermissions()
            false
        }
    }


    private fun requestLocationPermissions() {
        Timber.d("requesting location permission")
        requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        *** deprecated ***
//        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        requestPermissions(
//            permissionsArray,
//            LOCATION_PERMISSIONS_REQUEST_CODE
//        )
    }

    private fun isPermissionGranted() : Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        return (ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getLocation() {
        //TODO: Get location from LocationServices
        //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
    }

//    private fun geoCodeLocation(location: Location): Address {
//        val geocoder = Geocoder(context, Locale.getDefault())
//        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
//                .map { address ->
//                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
//                }
//                .first()
//    }
//
//    private fun hideKeyboard() {
//        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
//    }

}