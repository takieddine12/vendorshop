package com.android.app.shoppy.customer

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import  com.android.app.shoppy.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.Utils
import com.android.app.shoppy.databinding.ActivityCustomerMainBinding
import com.android.app.shoppy.databinding.ActivityCustomerProfileBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*

class CustomerProfileActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private var latitude = ""
    private var longitude = ""
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private  var previousEmail : String = ""
    private lateinit var imageDownloadUrl : String
    private  var imageUrl : Uri?  =  null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivityCustomerProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding  = ActivityCustomerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        init()

        binding.update.setOnClickListener {
            updateCustomerProfile()
        }
        binding.customerImg.setOnClickListener {
            getUserProfileImage()
        }

        getCustomerProfile()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit -> {
                binding.update.isEnabled = true
                binding.email.isEnabled = true
                binding.userName.isEnabled = true
                binding.phone.isEnabled = true
            }
            R.id.location -> {
                // Enable Location
                fetchLocation()
            }
        }
        return true
    }

    private fun fetchLocation(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
        == PackageManager.PERMISSION_GRANTED){
            // permission granted
            displayGpsSettings()
         } else {
            // denied
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayGpsSettings(){
      locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if(locationResult == null)
                    return
                else
                    for(location in locationResult.locations){
                        val geocoder = Geocoder(this@CustomerProfileActivity, Locale.getDefault())
                        val addressList = geocoder.getFromLocation(location.latitude,location.longitude,1)
                        binding.country.setText(addressList[0].countryName)
                        binding.city.setText(addressList[0].locality)
                        binding.state.setText(addressList[0].locality)
                        binding.address.setText(addressList[0].getAddressLine(0))

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

         LocationServices.getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
            .addOnCompleteListener {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener {
                        if(it != null){
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addressList = geocoder.getFromLocation(it.latitude,it.longitude,1)
                            binding.country.setText(addressList[0].countryName)
                            binding.city.setText(addressList[0].locality)
                            binding.state.setText(addressList[0].locality)
                            binding.address.setText(addressList[0].getAddressLine(0))

                            latitude = it.latitude.toString()
                            longitude = it.longitude.toString()


                        }
                    }
                    .addOnFailureListener {
                        val exception = it as ApiException
                        when(exception.statusCode){
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                try {
                                    val resolvableApiException = exception as ResolvableApiException
                                    resolvableApiException.startResolutionForResult(this, GPS_DIALOG_REQUEST_CODE)
                                }catch (ex : IntentSender.SendIntentException){
                                    /// nothing here to do
                                }
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->{

                            }
                        }

                    }
            }
            .addOnFailureListener {
                // handle gps dialog
            }
    }

    private fun getUserProfileImage(){
        Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            startActivityForResult(this, IMAGE_REQUEST_CODE)
        }
    }

    private fun updateCustomerProfile(){
        progressDialog.show()

        val userName = binding.userName.text.toString()
        val userEmail = binding.email.text.toString()
        val userPhone = binding.phone.text.toString()
        val country = binding.country.text.toString()
        val city = binding.city.text.toString()
        val state = binding.state.text.toString()
        val address = binding.address.text.toString()

        if(TextUtils.isEmpty(userName)){
            progressDialog.hide()
            Toast.makeText(this,"Name missing..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(userEmail)){
            progressDialog.hide()
            Toast.makeText(this,"Email missing..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(userPhone)){
            progressDialog.hide()
            Toast.makeText(this,"Phone missing..",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(country)){
            progressDialog.hide()
            Toast.makeText(this,"Country missing..", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(state)){
            progressDialog.hide()
            Toast.makeText(this,"State missing..", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(city)){
            progressDialog.hide()
            Toast.makeText(this,"City missing..", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(address)){
            progressDialog.hide()
            Toast.makeText(this,"Address missing..", Toast.LENGTH_SHORT).show()
            return
        }
        if(imageUrl == null){
            progressDialog.hide()
            Toast.makeText(this,"Please select an image..",Toast.LENGTH_SHORT).show()
            return
        }

       lifecycleScope.launchWhenStarted {
            try {

                val task = storageReference.child("Customers")
                    .child(firebaseAuth.currentUser?.uid!!)
                    .putFile(imageUrl!!).await()

                imageDownloadUrl =  task.storage.downloadUrl.await().toString()
                
                val customerInfo = hashMapOf<String,Any>()
                customerInfo["customerName"] = userName
                customerInfo["customerEmail"] = userEmail
                customerInfo["CustomerPhone"] = userPhone
                customerInfo["profileImage"] = imageDownloadUrl
                customerInfo["latitude"] = latitude
                customerInfo["longitude"] = longitude
                customerInfo["customerCountry"] = country
                customerInfo["customerCity"] = city
                customerInfo["customerState"] = state
                customerInfo["customerAddress"] = address

                databaseReference
                        .child("Accounts")
                        .child("Customers")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .updateChildren(customerInfo).await()

                firebaseAuth.currentUser!!.updateEmail(userEmail)
                if(previousEmail != userEmail){
                    Toast.makeText(this@CustomerProfileActivity,"Email has been changed,please login again..",
                    Toast.LENGTH_SHORT).show()
                    progressDialog.hide()
                    firebaseAuth.signOut()
                    Intent(this@CustomerProfileActivity,CustomerLoginAcitivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }

                progressDialog.hide()

                binding.update.isEnabled = false
                binding.userName.isEnabled = false
                binding.email.isEnabled = false
                binding.phone.isEnabled = false


            }catch (ex : Exception){
                progressDialog.hide()
                Log.d(Utils.ACTIVITY_TAG,"Exception ${ex.message}")
            }
        }


    }

    private fun getCustomerProfile(){
        databaseReference
                .child("Accounts")
                .child("Customers")
                .child(firebaseAuth.currentUser?.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val profileImage = snapshot.child("profileImage").value.toString()
                            val userName  = snapshot.child("customerName").value.toString()
                            val userEmail = snapshot.child("customerEmail").value.toString()
                            val userPhone  = snapshot.child("CustomerPhone").value.toString()
                            val country = snapshot.child("customerCountry").value.toString()
                            val city = snapshot.child("customerCity").value.toString()
                            val state = snapshot.child("customerState").value.toString()
                            val address = snapshot.child("customerAddress").value.toString()

                            previousEmail = userEmail
                            binding.userName.setText(userName)
                            binding.email.setText(userEmail)
                            binding.phone.setText(userPhone)
                            binding.country.setText(country)
                            binding.state.setText(state)
                            binding.city.setText(city)
                            binding.address.setText(address)

                            if(profileImage == ""){
                                binding.customerImg.setImageResource(R.drawable.ic_boy)
                            } else {
                                Picasso.get().load(profileImage).into(binding.customerImg)
                            }

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    private fun init(){
        progressDialog  = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference  = FirebaseStorage.getInstance().reference
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_REQUEST_CODE){
            data?.data.let {
                imageUrl = it
                binding.customerImg.setImageURI(imageUrl)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CODE){
            displayGpsSettings()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 1003
        const val PERMISSION_REQUEST_CODE = 1004
        const val GPS_DIALOG_REQUEST_CODE = 1005
    }
}