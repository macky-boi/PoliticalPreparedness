package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
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
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.PoliticalPreparednessApplication
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Timber.d("request location Permission GRANTED")
            retryUseMyLocationButtonOnClick()
        } else {
            Timber.d("request location DENIED")
            viewModel.updateIsLoading(false)
            Snackbar.make(
                binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG
            ).setAction(R.string.settings) {
                displayAppSettingsScreen()
            }.show()
        }
    }

    private val requestLocationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Timber.d("User enabled location settings")
            retryCheckLocationSettings()
        } else {
            viewModel.updateIsLoading(false)
            verifyLocationSettings(false) { viewModel.fetchRepresentativesUsingAddress() }
        }
    }

    //TODO: Declare ViewModel (x)
    private lateinit var viewModel: RepresentativeViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val appContainer = (requireActivity().application as PoliticalPreparednessApplication)
            .container
        val repository = appContainer.politicalPreparednessRepository
        viewModel = ViewModelProvider(this,
            RepresentativeViewModelFactory(repository, requireActivity().application)
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

                override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                useMyLocationButtonOnClick()
                viewModel.updateIsLoading(true)
                hideKeyboard()
            }

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                Timber.d("isLoading: $isLoading")
                electionsRecycleView.visibility = if (isLoading) View.GONE else View.VISIBLE
                listPlaceholder.visibility =  if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    //TODO: Define and assign Representative adapter (x)
    //TODO: Populate Representative adapter (x)
    private fun setupRecycleView(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RepresentativeListAdapter()
        }
    }

    private fun retryUseMyLocationButtonOnClick() {
        // Delay to ensure system has applied the settings
        Handler(Looper.getMainLooper()).postDelayed({
            useMyLocationButtonOnClick()
        }, 200)
    }

    private fun useMyLocationButtonOnClick() {
        if (!requireLocationPermissions()) return
        verifyLocationSettings {
            viewModel.fetchRepresentativesUsingAddress()
        }
    }

    private fun requireLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            Timber.d("location permission is already granted")
            true
        } else {
            //TODO: Request Location permissions
            requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            false
        }
    }

    private fun retryCheckLocationSettings() {
        // Delay to ensure system has applied the settings
        Handler(Looper.getMainLooper()).postDelayed({
            verifyLocationSettings(false) { viewModel.fetchRepresentativesUsingAddress() }
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
            handleLocationSettingsFailure(exception, resolve, onSuccess)
        }
    }

    private fun handleLocationSettingsFailure(
        exception: Exception,
        resolve: Boolean,
        onSuccess: () -> Unit
    ) {
        if (exception is ResolvableApiException && resolve) {
            try {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                requestLocationSettingsLauncher.launch(intentSenderRequest)
                Timber.d("Prompting user to enable location settings")
            } catch (sendEx: IntentSender.SendIntentException) {
                Timber.e(sendEx, "Error getting location settings resolution")
            }
        } else {
            Snackbar.make(
                binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG
            ).setAction(R.string.settings) {
                verifyLocationSettings { onSuccess() }
            }.show()
        }
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


    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}