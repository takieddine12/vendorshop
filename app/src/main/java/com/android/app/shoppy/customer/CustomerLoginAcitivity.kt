package com.android.app.shoppy.customer

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import com.android.app.shoppy.Utils
import com.android.app.shoppy.databinding.ActivityCustomerLoginAcitivityBinding
import com.android.app.shoppy.passwordrecovery.CustomerPasswordRecoveryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class CustomerLoginAcitivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var job : Job
    private var _binding : ActivityCustomerLoginAcitivityBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       _binding = ActivityCustomerLoginAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        
        binding.loginBtn.setOnClickListener {
            loginCustomer()
        }
        binding.customerNewAccount.setOnClickListener {
            Intent(this,CustomerRegistrationActivity::class.java).apply {
                startActivity(this)
            }
        }
        binding.forgotPassword.setOnClickListener {
            Intent(this,CustomerPasswordRecoveryActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun init(){
        progressDialog  = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        job = Job()
    }
    private fun loginCustomer(){
        progressDialog.show()

        val email = binding.loginEmail.text.toString()
        val password = binding.loginPassword.text.toString()
        
        if(TextUtils.isEmpty(email)){
            progressDialog.hide()
            Toast.makeText(this,"Missing email",Toast.LENGTH_SHORT).show()
            return 
        }
        if(TextUtils.isEmpty(password)){
            progressDialog.hide()
            Toast.makeText(this,"Missing Password",Toast.LENGTH_SHORT).show()
            return
        }

        job =  lifecycleScope.launchWhenStarted {
            try {
                firebaseAuth.signInWithEmailAndPassword(email,password).await()

                databaseReference.child("Accounts")
                        .child("Customers")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        val accountType = snapshot.child("accountType").value.toString()
                                        if(accountType == "Customer"){
                                            if(firebaseAuth.currentUser!!.isEmailVerified){
                                                Intent(this@CustomerLoginAcitivity,CustomerMainActivity::class.java).apply {
                                                    startActivity(this)
                                                    finish()
                                                }
                                            }
                                        } else {
                                            progressDialog.hide()
                                            Toast.makeText(this@CustomerLoginAcitivity,"Not a customer..",
                                            Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                    } else {
                                        progressDialog.hide()
                                        Toast.makeText(this@CustomerLoginAcitivity,"Not a customer..",
                                                Toast.LENGTH_SHORT).show()
                                        return
                                    }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                progressDialog.hide()
                            }
                        })
            }catch (ex : Exception){
                progressDialog.hide()
                Log.d(Utils.ACTIVITY_TAG,"Exception ${ex.message}")
            }
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        _binding = null
    }
}