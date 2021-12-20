package com.android.app.shoppy.customer.products

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.app.shoppy.R
import com.android.app.shoppy.adapters.MarchandiseAdapter
import com.android.app.shoppy.databinding.ActivityStoreProductsBinding
import com.android.app.shoppy.listeners.CartListener
import com.android.app.shoppy.models.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class StoreProductsActivity : AppCompatActivity() {
    private lateinit var sellerUid : String
    private lateinit var country : String
    private lateinit var city : String
    private lateinit var state : String
    private lateinit var address : String
    private  var sellerPhone : String = ""
    private var userlatitude = ""
    private var userlongitude = ""
    private var sellerlatitude = ""
    private var sellerlongitude = ""
    private lateinit var marchandiseAdapter: MarchandiseAdapter
    private lateinit var marchandiseList : MutableList<ProductModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivityStoreProductsBinding?  = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoreProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = ""

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        init()

        intent?.let {
            sellerUid = it.getStringExtra("sellerUid")!!
            fetchSellerShopInfo(sellerUid)
            fetchSellerProducts(sellerUid)
            getCustomerInfo()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.seller_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.phone -> {
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:$sellerPhone")
                    startActivity(intent)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),1001)
                }
            }
            R.id.location -> {
                val uri = "http://maps.google.com/maps?saddr=$userlatitude,$userlongitude&daddr=$sellerlatitude,$sellerlongitude"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }
        }
        return true
    }

    private fun getCustomerInfo(){
        databaseReference
                .child("Accounts")
                .child("Customers")
                .child(firebaseAuth.currentUser?.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            userlatitude = snapshot.child("latitude").value.toString()
                            userlongitude = snapshot.child("longitude").value.toString()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    private fun fetchSellerShopInfo(sellerUid: String){
        databaseReference
            .child("Accounts")
            .child("Sellers")
            .child(sellerUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val accountType = snapshot.child("accountType").value.toString()
                        val salesUid = snapshot.child("sellerUid").value.toString()
                        val profileImage = snapshot.child("profileImage").value.toString()
                        val sellerEmail = snapshot.child("sellerEmail").value.toString()
                        val sellerName = snapshot.child("sellerName").value.toString()
                        sellerPhone = snapshot.child("sellerPhone").value.toString()
                        val shopName = snapshot.child("shopName").value.toString()
                        country = snapshot.child("sellerCountry").value.toString()
                        city = snapshot.child("sellerCity").value.toString()
                        state = snapshot.child("sellerState").value.toString()
                        address = snapshot.child("sellerAddress").value.toString()
                        sellerlatitude = snapshot.child("latitude").value.toString()
                        sellerlongitude = snapshot.child("longitude").value.toString()


                        binding.productSeller.text = sellerName
                        binding.productEmail.text = sellerEmail
                        binding.productPhone.text = sellerPhone
                        binding.productShopName.text = shopName
                        Picasso.get().load(profileImage).into(binding.productImage)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun fetchSellerProducts(sellerUid: String){
        databaseReference
            .child("Sellers")
            .child(sellerUid)
            .child("products")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val productCategory = ds.child("productCategory").value.toString()
                            val productDeliveryFee = ds.child("productDeliveryFee").value.toString()
                            val productDiscount = ds.child("productDiscount").value.toString()
                            val productID = ds.child("productID").value.toString()
                            val productImage = ds.child("productImage").value.toString()
                            val productName = ds.child("productName").value.toString()
                            val productPrice = ds.child("productPrice").value.toString()
                            val productSavedTime = ds.child("productSavedTime").value.toString()
                            val shopStatus = ds.child("shopStatus").value.toString()
                            val productDescription = ds.child("productDescription").value.toString()

                            val productModel = ProductModel(
                                    productPrice, productName, productDiscount, productDeliveryFee, productID,
                                    productSavedTime, shopStatus, productImage, productCategory, productDescription)

                            marchandiseList.add(productModel)
                            marchandiseAdapter = MarchandiseAdapter(marchandiseList, object : CartListener {
                                override fun cartItemOnClick(model: ProductModel) {
                                    Intent(this@StoreProductsActivity, CheckActivity::class.java).apply {
                                        if (country == "") {
                                            return
                                        } else {
                                            putExtra("country", country)
                                            putExtra("city", city)
                                            putExtra("address", address)
                                            putExtra("state", state)
                                            putExtra("model", model)
                                            putExtra("sellerUid", sellerUid)
                                            startActivity(this)
                                        }
                                    }
                                }
                            })
                            binding.recycler.adapter = marchandiseAdapter


                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun init(){

        marchandiseList = mutableListOf()

        binding.recycler.layoutManager = StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL)
        binding.recycler.setHasFixedSize(true)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1001){
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$sellerPhone")
            startActivity(intent)
        }
    }
}