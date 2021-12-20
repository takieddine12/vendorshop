package com.android.app.shoppy.seller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Telephony
import android.telephony.mbms.DownloadProgressListener
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.Utils
import com.android.app.shoppy.customer.CustomerMainActivity
import com.android.app.shoppy.customer.CustomerRegistrationActivity
import com.android.app.shoppy.databinding.ActivitySellerRegistrationBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*

class SellerRegistrationActivity : AppCompatActivity() {
    private var country: String = ""
    private var state: String = ""
    private var city: String = ""
    private var address: String = ""
    private var latitude = ""
    private var longitude = ""
    private lateinit var imageDownloadUrl: String
    private var imageUrl: Uri? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var progressDialog: ProgressDialog
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var _binding: ActivitySellerRegistrationBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySellerRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        init()


        binding.register.setOnClickListener {
            registerNewCustomer()
        }

        binding.sellerImg.setOnClickListener {
            selectCustomerImage()
        }

        binding.haveAccount.setOnClickListener {
            Intent(this, SellerLoginActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }

        binding.location.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                displaySettings()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun displaySettings() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult : LocationResult) {
                super.onLocationResult(locationResult)
                if(locationResult == null)
                    return
                else
                    for(location in locationResult.locations){
                        val geoCoder = Geocoder(this@SellerRegistrationActivity, Locale.getDefault())
                        val addressList = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                        country = addressList[0].countryName
                        city = addressList[0].locality
                        address = addressList[0].getAddressLine(0)
                        state = addressList[0].locality

                        binding.country.setText(country)
                        binding.state.setText(state)
                        binding.city.setText(city)
                        binding.address.setText(address)

                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()
                    }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }
        }
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()


        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())

        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
            .addOnCompleteListener { response ->

                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener {
                        if(it != null){
                            val geoCoder = Geocoder(this, Locale.getDefault())
                            val addressList = geoCoder.getFromLocation(it.latitude, it.longitude, 1)
                            country = addressList[0].countryName
                            city = addressList[0].locality
                            address = addressList[0].getAddressLine(0)
                            state = addressList[0].locality

                            binding.country.setText(country)
                            binding.state.setText(state)
                            binding.city.setText(city)
                            binding.address.setText(address)

                            latitude = it.latitude.toString()
                            longitude = it.longitude.toString()
                        }
                    }
            }
            .addOnFailureListener {
                val exception = it as ApiException
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(
                                this, REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }

                }
            }

    }

    private fun init() {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
    }


    private fun registerNewCustomer() {
        val sellerUserName = binding.userName.text.toString()
        val sellerPhone = binding.phone.text.toString()
        val sellerEmail = binding.email.text.toString()
        val sellerPassword = binding.password.text.toString()
        val shopName = binding.shopName.text.toString()
        val customerCountry = binding.country.text.toString()
        val customerState = binding.state.text.toString()
        val customerCity = binding.state.text.toString()
        val customerAddress = binding.address.text.toString()

        if (TextUtils.isEmpty(sellerUserName)) {
            Toast.makeText(this, "Missing Username..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(sellerPhone)) {
            Toast.makeText(this, "Missing phone..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(sellerEmail)) {
            Toast.makeText(this, "Missing Email..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(sellerPassword)) {
            Toast.makeText(this, "Missing password..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(shopName)) {
            Toast.makeText(this, "Missing shop name..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(customerCountry)) {
            Toast.makeText(this, "Missing country..", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(customerState)) {
            Toast.makeText(this, "Missing state..", Toast.LENGTH_SHORT).show()

            return
        }
        if (TextUtils.isEmpty(customerCity)) {
            Toast.makeText(this, "Missing city", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(customerAddress)) {
            Toast.makeText(this, "Missing address..", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launchWhenStarted {
            progressDialog.show()
            try {
                firebaseAuth.createUserWithEmailAndPassword(sellerEmail, sellerPassword).await()

                imageDownloadUrl = if (imageUrl == null) {
                    ""
                } else {
                    val task = storageReference.child("Sellers")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .child(UUID.randomUUID().toString())
                        .putFile(imageUrl!!).await()
                    task.storage.downloadUrl.await().toString()
                }

                val credentialsMap = hashMapOf<String, Any>()
                credentialsMap["sellerName"] = sellerUserName
                credentialsMap["sellerEmail"] = sellerEmail
                credentialsMap["sellerPhone"] = sellerPhone
                credentialsMap["accountType"] = "Seller"
                credentialsMap["profileImage"] = imageDownloadUrl
                credentialsMap["shopName"] = shopName
                credentialsMap["sellerUid"] = firebaseAuth.currentUser?.uid!!
                credentialsMap["sellerCountry"] = country
                credentialsMap["sellerCity"] = city
                credentialsMap["sellerState"] = state
                credentialsMap["sellerAddress"] = address
                credentialsMap["latitude"] = latitude
                credentialsMap["longitude"] = longitude
                credentialsMap["status"] = "open"


                databaseReference
                    .child("Accounts")
                    .child("Sellers")
                    .child(firebaseAuth.currentUser?.uid!!)
                    .setValue(credentialsMap).await()

                progressDialog.dismiss()

                firebaseAuth.currentUser!!.sendEmailVerification()
                Toast.makeText(this@SellerRegistrationActivity,"Verification Email has been sent",
                Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    Intent(this@SellerRegistrationActivity, SellerMainActivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }

            } catch (ex: Exception) {
                Log.d(Utils.ACTIVITY_TAG, "Exception ${ex.message}")
                progressDialog.dismiss()
            }
        }
    }

    private fun selectCustomerImage() {
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            displaySettings()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                val uri = result.uri
                imageUrl = uri
                binding.sellerImg.setImageURI(uri)
            }

        } else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            // error
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
        const val REQUEST_CHECK_SETTINGS = 500
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}