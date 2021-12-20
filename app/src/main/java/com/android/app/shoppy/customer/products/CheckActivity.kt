package com.android.app.shoppy.customer.products

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.app.shoppy.databinding.ActivityCheckBinding
import com.android.app.shoppy.models.PaymentModel
import com.android.app.shoppy.models.ProductModel
import com.android.app.shoppy.roomdb.PaymentDatabase
import com.android.app.shoppy.roomdb.RoomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import p32929.androideasysql_library.Column
import p32929.androideasysql_library.EasyDB
import java.lang.Exception

class CheckActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var customerName = ""
    private lateinit var sellerUid : String
    private  var country : String? = null
    private lateinit var city : String
    private lateinit var state : String
    private lateinit var address : String
    private var itemID = 0
    private  var totalPrice : Double = 0.0
    private var  itemIndex = 0
    private lateinit var binding : ActivityCheckBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            val productModel = it.getParcelableExtra<ProductModel>("model")
            country = it.getStringExtra("country")!!
            city = it.getStringExtra("city")!!
            state = it.getStringExtra("state")!!
            address = it.getStringExtra("address")!!
            sellerUid = it.getStringExtra("sellerUid")!!

            Picasso.get().load(productModel?.productImage).into(binding.productImage)
            binding.product.text = "Item : " + productModel?.productName
            binding.category.text = "Category : " + productModel?.productCategory
            binding.price.text = "Price : " + productModel?.productPrice + "$"
            binding.discount.text = "Price Discount : " + productModel?.productDiscount + "$"
            binding.deliveryFee.text = "Delivery Fee : " + productModel?.productDeliveryFee + "$"

            binding.checkout.setOnClickListener {
                if(itemIndex == 0){
                    Toast.makeText(this,"Please set a quantity",Toast.LENGTH_SHORT).show()
                } else {
                    setCheckOut(productModel!!)
                }
            }
            binding.plus.setOnClickListener {
                itemIndex++
                totalPrice += productModel?.productPrice?.toDouble()!! + productModel.productDeliveryFee.toDouble()
                binding.quantity.text = "" + itemIndex
                binding.totalToPay.text = "$totalPrice$"
            }
            binding.minus.setOnClickListener {
                itemIndex--
                if(itemIndex < 1){
                    itemIndex = 0
                    totalPrice = 0.0
                    binding.quantity.text = "" + itemIndex
                    binding.totalToPay.text = "$totalPrice"
                } else {
                    totalPrice -= (productModel?.productPrice?.toDouble()!! + productModel.productDeliveryFee.toDouble())
                    binding.quantity.text = "" + itemIndex
                    binding.totalToPay.text = "$totalPrice$"
                }
            }
        }
        init()
        getCustomerName()
        binding.visitStore.setOnClickListener {
            val intent = Intent(this,StoreProductsActivity::class.java)
            intent.putExtra("sellerUid",sellerUid)
            startActivity(intent)
        }
    }
    private fun init(){
        progressDialog  = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }
    private fun getCustomerName() {
        databaseReference
                .child("Accounts")
                .child("Customers")
                .child(firebaseAuth.currentUser?.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            customerName = snapshot.child("customerName").value.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }
    private fun setCheckOut(model : ProductModel) {
        progressDialog.show()
        if (country == null) {
            progressDialog.hide()
        } else {
            itemID++
            try {

                val roomModel = RoomModel(
                    model.productName, model.productCategory, model.productPrice,
                    totalPrice.toString(), itemIndex.toString(), model.productDeliveryFee, country!!,
                    city, address, state, sellerUid,customerName)


                CoroutineScope(Dispatchers.IO).launch {
                    val paymentDatabase  = PaymentDatabase.invoke(this@CheckActivity)
                    paymentDatabase.paymentDao().insertPayment(roomModel)

                    withContext(Dispatchers.Main){
                        Toast.makeText(this@CheckActivity, "Product Successfully Added To Cart", Toast.LENGTH_SHORT).show()
                        delay(1000)
                        progressDialog.hide()
                        Intent(this@CheckActivity, PaymentActivity::class.java).apply {
                            startActivity(this)
                        }
                    }

                }

            } catch (ex: Exception) {
                progressDialog.hide()
                Log.d("TAG", "Exception ${ex.message}")
            }
        }
    }
}