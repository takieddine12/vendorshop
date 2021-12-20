package com.android.app.shoppy.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.android.app.shoppy.adapters.ProductsAdapter
import com.android.app.shoppy.customer.products.CheckActivity
import com.android.app.shoppy.databinding.ActivityViewProductsBinding
import com.android.app.shoppy.listeners.ProductInfoListener
import com.android.app.shoppy.models.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewProductsActivity : AppCompatActivity() {
    private lateinit var productsList : MutableList<ProductModel>
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var sellerUid : String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private lateinit var binding : ActivityViewProductsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityViewProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            sellerUid = it.getStringExtra("sellerUid")!!
        }
        init()
        getStoreProducts()
    }

    private fun init(){
        productsList = mutableListOf()
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference  = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = GridLayoutManager(this,2)
        binding.recycler.setHasFixedSize(true)
    }

    private fun getStoreProducts(){
        databaseReference
            .child("Sellers")
            .child("products")
            .orderByChild("SellerUid").equalTo(sellerUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(ds in snapshot.children){
                            val productModel = ds.getValue(ProductModel::class.java)
                            productsList.add(productModel!!)
                            productsAdapter = ProductsAdapter(productsList,object : ProductInfoListener {
                                override fun getProductInfo(model: ProductModel) {
                                    val intent = Intent(this@ViewProductsActivity,CheckActivity::class.java)
                                    intent.putExtra("model",productModel)
                                    startActivity(intent)
                                }

                            })
                            binding.recycler.adapter = productsAdapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}