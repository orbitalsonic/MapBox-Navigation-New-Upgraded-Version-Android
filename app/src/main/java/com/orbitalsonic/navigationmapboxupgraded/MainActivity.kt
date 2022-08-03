package com.orbitalsonic.navigationmapboxupgraded

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.orbitalsonic.navigationmapboxupgraded.ConstantsUtils.locationDestination
import com.orbitalsonic.navigationmapboxupgraded.ConstantsUtils.locationStart
import com.orbitalsonic.navigationmapboxupgraded.LocationHandler.isGpsEnabled
import com.orbitalsonic.navigationmapboxupgraded.LocationHandler.isLocationApproved
import com.orbitalsonic.navigationmapboxupgraded.ScreenUtils.getScreenHeight
import com.orbitalsonic.navigationmapboxupgraded.ScreenUtils.getScreenWidth
import com.orbitalsonic.navigationmapboxupgraded.databinding.ActivityMainBinding
import com.orbitalsonic.navigationmapboxupgraded.databinding.DialogProgressBinding
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("RouteFinderTAg", "$exception")
        isRouteFetched = false
    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var progressDialog: Dialog? = null

    private var isRouteFetched = false

    private var myCurrentAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (isLocationApproved()) {
            if (isGpsEnabled()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    getCurrentLocation()
                },300)
            } else {
                try {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }catch (e:Exception){

                }
            }

        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LocationHandler.LOCATION_PERMISSION
            )
        }

        onclickMethod()
        initProgressDialog()
    }

    private fun onclickMethod() {

        binding.btnStartNav.setOnClickListener {
            if (binding.etStartText.text.toString()
                    .isNotEmpty() && binding.etDestinationText.text.toString().isNotEmpty()
            ) {
                findLocations(binding.etStartText.text.toString(),binding.etDestinationText.text.toString())
            } else {
                Toast.makeText(this,"Please Enter Address",Toast.LENGTH_SHORT).show()

            }
        }

        binding.btnStartVoice.setOnClickListener {
            getSpeechStart(ConstantsUtils.REQUEST_CODE_STT_START)
        }
        binding.btnDestinationVoice.setOnClickListener {
            getSpeechStart(ConstantsUtils.REQUEST_CODE_STT_END)
        }

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    locationStart.placeLatitude = location.latitude
                    locationStart.placeLongitude = location.longitude

                    findAddress(locationStart)
                }
            }

    }

    private fun initProgressDialog() {
        progressDialog = Dialog(this)
        val dialogBinding: DialogProgressBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.dialog_progress, null, false
        )
        progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog?.setContentView(dialogBinding.root)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog?.setCancelable(false)

        dialogBinding.progressCard.requestLayout()
        dialogBinding.progressCard.layoutParams.width =
            (getScreenWidth() * .90).toInt()
        dialogBinding.progressCard.layoutParams.height =
            (getScreenHeight() * .10).toInt()

    }

    private fun moveToDirection() {
        runOnUiThread {
            progressDialog?.dismiss()
        }

        startActivity(Intent(this, RouteDirectionActivity::class.java))
    }

    @SuppressLint("SetTextI18n")
    private fun findAddress(mLoc: MapLocation) {

        var addresses: List<Address> = ArrayList()

        runOnUiThread {
            progressDialog?.show()
        }

            CoroutineScope(Dispatchers.Main + handler).launch {
                async(Dispatchers.IO+handler) {

                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    addresses = geocoder.getFromLocation(mLoc.placeLatitude, mLoc.placeLongitude, 1)
                }.await()
               runOnUiThread {
                    progressDialog?.dismiss()
                }

                try {
                    if (addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0)?.let { mAddress ->
                            myCurrentAddress = mAddress
                        runOnUiThread {
                                binding.etStartText.setText(mAddress)
                            }
                        } ?: run {
                            myCurrentAddress = "Unknown Place"
                            binding.etStartText.setText("Unknown Place")
                        }
                    }
                } catch (ex: Exception) {

                }
            }


    }

    private fun findLocations(startAddress: String,endAddress:String) {
        var startResponses: List<Address> = ArrayList()
        var endResponses: List<Address> = ArrayList()

      runOnUiThread {
            progressDialog?.show()
        }
            CoroutineScope(Dispatchers.Main + handler).launch {
                try {

                    if (myCurrentAddress.equals(startAddress)){
                        Log.d("MapInformation", "Addresses are equals")

                        async(Dispatchers.IO+handler) {
                            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                            endResponses = geocoder.getFromLocationName(endAddress, 1)
                            Log.d("MapInformation", "Geocoder End")
                        }.await()

                        if (endResponses.isNotEmpty()) {
                            Log.d("MapInformation", "Both Response are ready")
                            locationDestination = MapLocation(endResponses[0].latitude,endResponses[0].longitude)

                            binding.etDestinationText.setText(endResponses[0].getAddressLine(0))
                            Log.d("MapInformation", "End Address: ${endResponses[0].getAddressLine(0)}")

                            moveToDirection()

                        }else{
                            Log.d("MapInformation", "Both Response not ready")
                            runOnUiThread {
                                progressDialog?.dismiss()
                            }
                        }

                    }else{
                        Log.d("MapInformation", "Addresses are not equals")

                        async(Dispatchers.IO+handler) {
                            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                            startResponses = geocoder.getFromLocationName(startAddress, 1)
                            Log.d("MapInformation", "Geocoder Start")
                        }.await()


                        if (startResponses.isNotEmpty()) {
                            Log.d("MapInformation", "First Response is ready")
                            async(Dispatchers.IO+handler) {
                                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                                endResponses = geocoder.getFromLocationName(endAddress, 1)
                                Log.d("MapInformation", "Geocoder End")
                            }.await()

                            if (endResponses.isNotEmpty()) {
                                Log.d("MapInformation", "Both Response are ready")
                                locationStart = MapLocation(startResponses[0].latitude,startResponses[0].longitude)
                                locationDestination = MapLocation(endResponses[0].latitude,endResponses[0].longitude)

                                binding.etStartText.setText(startResponses[0].getAddressLine(0))
                                binding.etDestinationText.setText(endResponses[0].getAddressLine(0))
                                myCurrentAddress = startResponses[0].getAddressLine(0)
                                Log.d("MapInformation", "Start Address: ${startResponses[0].getAddressLine(0)}")
                                Log.d("MapInformation", "End Address: ${endResponses[0].getAddressLine(0)}")

                                moveToDirection()

                            }else{
                                Log.d("MapInformation", "Both Response not ready")
                                runOnUiThread {
                                    progressDialog?.dismiss()
                                }
                            }

                        }else{
                            Log.d("MapInformation", "First Response not ready")
                            runOnUiThread {
                                progressDialog?.dismiss()
                            }
                        }

                    }



                } catch (ex: Exception) {
                    Log.d("MapInformation", "${ex.message}")
                  runOnUiThread {
                        progressDialog?.dismiss()
                    }
                }
            }


    }

    private fun getSpeechStart(codeRequest:Int) {
        try {
            val mIntent = Intent(
                RecognizerIntent
                    .ACTION_RECOGNIZE_SPEECH
            )
            mIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            mIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            startActivityForResult(mIntent, codeRequest)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this,"Voice not supported!",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            ConstantsUtils.REQUEST_CODE_STT_START -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        // Recognized text is in the first position.
                        val recognizedText = result[0]
                        // Do what you want with the recognized text.
                        binding.etStartText.setText(recognizedText)
                    }
                }
            }
            ConstantsUtils.REQUEST_CODE_STT_END -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        try {
                            // Recognized text is in the first position.
                            val recognizedText = result[0]
                            // Do what you want with the recognized text.
                            binding.etDestinationText.setText(recognizedText)
                        }catch (e:Exception){
                        }

                    }
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (LocationHandler.LOCATION_PERMISSION == requestCode) {
            try {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (isGpsEnabled()) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                getCurrentLocation()
                            },300)
                        } else {
                            try {
                                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }catch (e:Exception){

                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }


        }
    }
}