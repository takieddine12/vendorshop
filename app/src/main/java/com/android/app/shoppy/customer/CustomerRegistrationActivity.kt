package com.android.app.shoppy.customer

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
import android.os.strictmode.ResourceMismatchViolation
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.Utils
import com.android.app.shoppy.databinding.ActivityCustomerLoginAcitivityBinding
import com.android.app.shoppy.databinding.ActivityCustomerRegistrationBinding
import com.android.app.shoppy.seller.SellerLoginActivity
import com.android.app.shoppy.seller.SellerMainActivity
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
import java.util.jar.Manifest

class CustomerRegistrationActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private  var country : String = ""
    private  var state : String = ""
    private  var city : String = ""
    private  var address : String = ""
    private  var latitude = ""
    private  var longitude = ""
    private lateinit var imageDownloadUrl : String
    private var imageUrl : Uri? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var _binding : ActivityCustomerRegistrationBinding?  = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCustomerRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        init()

        binding.register.setOnClickListener {
            registerNewCustomer()
        }

        binding.customerImg.setOnClickListener {
            selectCustomerImage()
        }

        binding.haveAccount.setOnClickListener {
            Intent(this, CustomerLoginAcitivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }

        binding.location.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
                displaySettings()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun displaySettings(){
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
                        val geoCoder = Geocoder(this@CustomerRegistrationActivity,Locale.getDefault())
                        val addressList = geoCoder.getFromLocation(location.latitude,location.longitude,1)
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

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
                .addOnCompleteListener { response ->
                        fusedLocationProviderClient.lastLocation
                            .addOnSuccessListener {
                                if(it != null){
                                    val geoCoder = Geocoder(this@CustomerRegistrationActivity,Locale.getDefault())
                                    val addressList = geoCoder.getFromLocation(it.latitude,it.longitude,1)
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
                            resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                        } catch (e : IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e : ClassCastException ) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->{
                    }

                }
            }


    }

    private fun init(){
        progressDialog  = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            displaySettings()
        }
    }

    private fun registerNewCustomer(){
        progressDialog.show()

        val customerUserName  = binding.userName.text.toString()
        val customerPhone = binding.phone.text.toString()
        val customerEmail  = binding.email.text.toString()
        val customerPassword = binding.password.text.toString()
        val customerCountry = binding.country.text.toString()
        val customerState = binding.state.text.toString()
        val customerCity = binding.state.text.toString()
        val customerAddress = binding.address.text.toString()

        if(TextUtils.isEmpty(customerUserName)){
            progressDialog.hide()
            Toast.makeText(this,"Missing Username..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(customerPhone)){
            progressDialog.hide()
            Toast.makeText(this,"Missing phone..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(customerEmail)){
            progressDialog.hide()
            Toast.makeText(this,"Missing Email..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(customerPassword)){
            progressDialog.hide()
            Toast.makeText(this,"Missing password..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(customerCountry)){
            progressDialog.hide()
            Toast.makeText(this,"Missing country..",Toast.LENGTH_SHORT).show()

            return
        }
        if(TextUtils.isEmpty(customerState)){
            progressDialog.hide()
            Toast.makeText(this,"Missing state..",Toast.LENGTH_SHORT).show()

            return
        }
        if(TextUtils.isEmpty(customerCity)){
            progressDialog.hide()
            Toast.makeText(this,"Missing city",Toast.LENGTH_SHORT).show()

            return
        }
        if(TextUtils.isEmpty(customerAddress)){
            progressDialog.hide()
            Toast.makeText(this,"Missing address..",Toast.LENGTH_SHORT).show()

            return
        }
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                firebaseAuth.createUserWithEmailAndPassword(customerEmail,customerPassword).await()

                imageDownloadUrl = if(imageUrl == null){
                    ""
                } else {
                    val task = storageReference.child("Customers")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .child(UUID.randomUUID().toString())
                        .putFile(imageUrl!!).await()
                    task.storage.downloadUrl.await().toString()
                }

                val credentialsMap = hashMapOf<String,Any>()
                credentialsMap["customerName"] = customerUserName
                credentialsMap["customerEmail"] = customerEmail
                credentialsMap["CustomerPhone"] = customerPhone
                credentialsMap["accountType"] = "Customer"
                credentialsMap["profileImage"] = imageDownloadUrl
                credentialsMap["customerUid"] = firebaseAuth.currentUser?.uid!!
                credentialsMap["customerCountry"] = country
                credentialsMap["customerCity"] = city
                credentialsMap["customerState"] = state
                credentialsMap["customerAddress"] = address
                credentialsMap["latitude"] = latitude
                credentialsMap["longitude"]  = longitude


                databaseReference
                        .child("Accounts")
                        .child("Customers")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .setValue(credentialsMap).await()

                progressDialog.dismiss()

                firebaseAuth.currentUser!!.sendEmailVerification()

                Toast.makeText(this@CustomerRegistrationActivity,"Verification Email has been sent",
                Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)

                    Intent(this@CustomerRegistrationActivity,CustomerMainActivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }

            }catch (ex : Exception){
                progressDialog.hide()
                Log.d(Utils.ACTIVITY_TAG,"Exception Registration ${ex.message}")
            }
        }
    }

    private fun selectCustomerImage(){
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                val uri = result.uri
                imageUrl = uri
                binding.customerImg.setImageURI(uri)
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            // error
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
        const val REQUEST_CHECK_SETTINGS = 500
    }


}