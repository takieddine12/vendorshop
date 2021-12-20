package com.android.app.shoppy.extras

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.widget.TextView
import com.android.app.shoppy.R
import com.android.app.shoppy.customer.CustomerLoginAcitivity
import com.android.app.shoppy.customer.CustomerMainActivity
import com.android.app.shoppy.databinding.ActivitySelectionBinding
import com.android.app.shoppy.seller.SellerLoginActivity
import com.android.app.shoppy.seller.SellerMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import maes.tech.intentanim.CustomIntent.customType

class SelectionActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivitySelectionBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setTextSpan()

        binding.customer.setOnClickListener {
            if(firebaseAuth.currentUser == null){
                Intent(this,CustomerLoginAcitivity::class.java).apply {
                    startActivity(this)
                }
            } else {
                databaseReference.child("Accounts")
                        .child("Customers")
                        .child(firebaseAuth.currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(!snapshot.exists()){
                                    Intent(this@SelectionActivity,CustomerLoginAcitivity::class.java).apply {
                                        startActivity(this)
                                    }
                                } else {
                                    val accountType = snapshot.child("accountType").value.toString()
                                    if(accountType == "Customer"){
                                        Intent(this@SelectionActivity,CustomerMainActivity::class.java).apply {
                                            startActivity(this)
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
            }
        }

        binding.seller.setOnClickListener {
            if(firebaseAuth.currentUser == null){
                Intent(this,SellerLoginActivity::class.java).apply {
                    startActivity(this)
                }
            } else {
                databaseReference.child("Accounts")
                        .child("Sellers")
                        .child(firebaseAuth.currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(!snapshot.exists()){
                                    Intent(this@SelectionActivity,SellerLoginActivity::class.java).apply {
                                        startActivity(this)
                                    }
                                } else {
                                    val accountType = snapshot.child("accountType").value.toString()
                                    if(accountType == "Seller"){
                                        Intent(this@SelectionActivity,SellerMainActivity::class.java).apply {
                                            startActivity(this)
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
            }

            }


    }

    private fun init(){
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun setTextSpan(){
        val text = binding.policyText.text
        val ssb = SpannableStringBuilder(text)
        ssb.setSpan(ForegroundColorSpan(Color.BLUE),30,36,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(Color.BLUE),41,51,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(UnderlineSpan(),30,36,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(UnderlineSpan(),41,51,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.policyText.setText(ssb,TextView.BufferType.SPANNABLE)
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }
}