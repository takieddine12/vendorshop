package com.android.app.shoppy.seller.products

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.android.app.shoppy.adapters.ProductsAdapter
import com.android.app.shoppy.databinding.ActivityShowProductBinding
import com.android.app.shoppy.models.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.util.*

class ShowProductActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivityShowProductBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityShowProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        intent?.let {
            val productModel = it.getParcelableExtra<ProductModel>("model")
            binding.productName.text = productModel?.productName
            binding.productPrice.text = productModel?.productPrice + "$"
            binding.productCategory.text = productModel?.productCategory
            binding.productDelivery.text = productModel?.productDeliveryFee  + "$"
            binding.productDiscount.text = productModel?.productDiscount  + "$"
            binding.productDescription.text = productModel?.productDescription

            Picasso.get().load(productModel?.productImage).into(binding.productImg)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = productModel?.productSavedTime?.toLong()!!
            val formattedDate = android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString()

            binding.productTime.text = formattedDate

            binding.deleteProduct.setOnClickListener {
                AlertDialog.Builder(this)
                        .setTitle("Delete Item")
                        .setMessage("Do you want to delete this item ?")
                        .setPositiveButton("Yes",object  : DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                databaseReference
                                        .child("Sellers")
                                        .child(firebaseAuth.currentUser?.uid!!)
                                        .child("products")
                                        .child(productModel.productID)
                                        .removeValue()
                            }

                        })
                        .setNegativeButton("No",object : DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                dialog?.dismiss()
                            }

                        })
                        .create()
                        .show()
            }
            binding.editProduct.setOnClickListener {
                Intent(this,EditProductActivity::class.java).apply {
                    putExtra("productID",productModel.productID)
                    startActivity(this)
                    finish()
                }
            }
        }

    }



    private fun init(){
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}