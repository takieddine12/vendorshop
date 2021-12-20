package com.android.app.shoppy.seller

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.Utils
import com.android.app.shoppy.customer.CustomerMainActivity
import com.android.app.shoppy.databinding.ActivitySellerLoginBinding
import com.android.app.shoppy.passwordrecovery.CustomerPasswordRecoveryActivity
import com.android.app.shoppy.passwordrecovery.SellerPasswordRecoveryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class SellerLoginActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var job : Job
    private var _binding : ActivitySellerLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding  = ActivitySellerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        binding.loginBtn.setOnClickListener {
            loginCustomer()
        }

        binding.sellerNewAccount.setOnClickListener {
            Intent(this,SellerRegistrationActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.forgotPassword.setOnClickListener {
            Intent(this, SellerPasswordRecoveryActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun init(){
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        job = Job()
    }
    private fun loginCustomer(){
        val email = binding.loginEmail.text.toString()
        val password = binding.loginPassword.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Missing email", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Missing email",Toast.LENGTH_SHORT).show()
            return
        }


        job =  lifecycleScope.launchWhenStarted {
            try {
                progressDialog.show()
                firebaseAuth.signInWithEmailAndPassword(email,password).await()

                databaseReference.child("Accounts")
                        .child("Sellers")
                        .child(firebaseAuth.currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(!snapshot.exists()){
                                    progressDialog.dismiss()
                                    Toast.makeText(this@SellerLoginActivity,"Not a seller..",
                                            Toast.LENGTH_SHORT).show()
                                    return
                                } else {
                                    val accountType = snapshot.child("accountType").value.toString()
                                    if(accountType == "Seller"){
                                        progressDialog.dismiss()
                                        if(firebaseAuth.currentUser!!.isEmailVerified){
                                            Intent(this@SellerLoginActivity, SellerMainActivity::class.java).apply {
                                                startActivity(this)
                                                finish()
                                            }
                                        }
                                    }
                                    else {
                                        progressDialog.dismiss()
                                        Toast.makeText(this@SellerLoginActivity,"Not a seller..",
                                                Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })


            }catch (ex : Exception){
                progressDialog.dismiss()
                Log.d(Utils.ACTIVITY_TAG,"Exception ${ex.message}")
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}