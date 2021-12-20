package com.android.app.shoppy.passwordrecovery

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.databinding.ActivityCustomerLoginAcitivityBinding
import com.android.app.shoppy.databinding.ActivityCustomerPasswordRecoveryBinding
import com.android.app.shoppy.databinding.ActivitySellerPasswordRecoveryBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class CustomerPasswordRecoveryActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var _binding : ActivityCustomerPasswordRecoveryBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCustomerPasswordRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)


       init()

        binding.sendVerification.setOnClickListener {
            recoverPassword()
        }
    }


    private fun init(){

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog  = ProgressDialog(this)
        progressDialog.setMessage("Sending Verification Email..")
        progressDialog.setCanceledOnTouchOutside(false)
    }
    private fun recoverPassword(){
        progressDialog.show()
        val sellerEmail = binding.emailEdit.text.toString()

        if(TextUtils.isEmpty(sellerEmail)){
            progressDialog.hide()
            Toast.makeText(this,"Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            try {
                firebaseAuth.sendPasswordResetEmail(sellerEmail).await()
                Toast.makeText(this@CustomerPasswordRecoveryActivity,"Verification Email has been sent", Toast.LENGTH_SHORT).show()
                binding.emailEdit.setText("")
                progressDialog.hide()
            }catch (ex : Exception){
                progressDialog.hide()
            }
        }

    }
}