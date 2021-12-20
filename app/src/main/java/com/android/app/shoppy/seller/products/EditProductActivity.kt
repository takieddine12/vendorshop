package com.android.app.shoppy.seller.products

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.R
import com.android.app.shoppy.Utils
import com.android.app.shoppy.databinding.ActivityEditProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*

class EditProductActivity : AppCompatActivity() {
    private lateinit var productID : String
    private lateinit var imageDownloadUrl : String
    private lateinit var job : Job
    private lateinit var storageReference: StorageReference
    private var imageUrl : Uri? = null
    private lateinit var selectedCategory : String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivityEditProductBinding?  = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        intent?.let {
            productID = it.getStringExtra("productID")!!
            getCurrentProductInfo(productID)
        }

        binding.editCategory.setOnClickListener{
            val alertDialog = AlertDialog.Builder(this)

            alertDialog
                .setTitle("Pick Category")
                .setItems(Utils.productsCategories) { dialog, which ->
                    selectedCategory = Utils.productsCategories[which]
                    binding.editCategory.setText(selectedCategory)
                    dialog.dismiss()
                }

            val dialog = alertDialog.create()
            dialog.show()


        }
        binding.updateProduct.setOnClickListener {
            updateProductInfo()
        }
        binding.productImg.setOnClickListener {
            getProductImage()
        }
    }

    private fun init(){
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
    }

    private fun  getProductImage(){
        Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            startActivityForResult(this, AddProductActivity.IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AddProductActivity.IMAGE_REQUEST_CODE){
            data?.data.let {
                imageUrl = it
                binding.productImg.setImageURI(imageUrl)
            }
        }
    }

    private fun updateProductInfo(){

        val systemTime = System.currentTimeMillis().toString()

        val productName = binding.productName.text.toString()
        val productPrice = binding.productPrice.text.toString()
        val productDiscount = binding.productDiscount.text.toString()
        val productDeliveryFee = binding.deliveryFee.text.toString()
        val productDescription = binding.productDescription.text.toString()

        if(TextUtils.isEmpty(productName)){
            Toast.makeText(this,"Missing Product Name...", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(productDescription)){
            Toast.makeText(this,"Missing Product Description...", Toast.LENGTH_SHORT).show()

            return
        }
        if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(this,"Missing Product Price...", Toast.LENGTH_SHORT).show()

            return
        }
        if(TextUtils.isEmpty(productDiscount)){
            Toast.makeText(this,"Missing Product Discunt...", Toast.LENGTH_SHORT).show()

            return
        }
        if(TextUtils.isEmpty(productDeliveryFee)){
            Toast.makeText(this,"Missing Delivery Fee...", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(selectedCategory)){
            Toast.makeText(this,"Please pick a category", Toast.LENGTH_SHORT).show()

            return
        }



        job = lifecycleScope.launchWhenStarted {
            try {

                imageDownloadUrl = if(imageUrl == null){
                    ""
                    Toast.makeText(this@EditProductActivity,"Please select an image",Toast.LENGTH_SHORT).show()
                    return@launchWhenStarted
                } else {
                    val task = storageReference.child("sellers")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .child("productsImages")
                        .child(UUID.randomUUID().toString())
                        .putFile(imageUrl!!).await()
                    task.storage.downloadUrl.await().toString()
                }


                val productDetailsMap = hashMapOf<String,Any>()
                productDetailsMap["productName"] = productName
                productDetailsMap["productPrice"] = productPrice
                productDetailsMap["productDiscount"] = productDiscount
                productDetailsMap["productDeliveryFee"] = productDeliveryFee
                productDetailsMap["productID"] = productID
                productDetailsMap["productSavedTime"] = systemTime
                productDetailsMap["shopStatus"] = "Open"
                productDetailsMap["productImage"] = imageDownloadUrl
                productDetailsMap["productCategory"] = selectedCategory
                productDetailsMap["productDescription"] = productDescription

                databaseReference.child("Sellers")
                    .child(firebaseAuth.currentUser?.uid!!)
                    .child("products")
                    .child(productID)
                    .updateChildren(productDetailsMap).await()


                Toast.makeText(this@EditProductActivity,"Product Successfully Updated",Toast.LENGTH_SHORT).show()



                binding.productName.setText("")
                binding.productPrice.setText("")
                binding.productDiscount.setText("")
                binding.deliveryFee.setText("")
                binding.productImg.setImageResource(R.drawable.ic_store)
                binding.productDescription.setText("")
                binding.editCategory.setText(Utils.productsCategories[0])

            }catch (ex : Exception){
            }
        }


    }
    private fun getCurrentProductInfo(productID : String){
        databaseReference
            .child("Sellers")
            .child(firebaseAuth.currentUser?.uid!!)
            .child("products")
            .child(productID)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        selectedCategory = snapshot.child("productCategory").value.toString()
                        val productDescription = snapshot.child("productDescription").value.toString()
                        val productDeliveryFee = snapshot.child("productDeliveryFee").value.toString()
                        val productDiscount = snapshot.child("productDiscount").value.toString()
                        val productID = snapshot.child("productID").value.toString()
                        val productImage = snapshot.child("productImage").value.toString()
                        val productName = snapshot.child("productName").value.toString()
                        val productPrice = snapshot.child("productPrice").value.toString()
                        var productSavedTime = snapshot.child("productSavedTime").value.toString()
                        var shopStatus = snapshot.child("shopStatus").value.toString()

                        binding.productName.setText(productName)
                        binding.deliveryFee.setText(productDeliveryFee)
                        binding.productDescription.setText(productDescription)
                        binding.productDiscount.setText(productDiscount)
                        binding.productPrice.setText(productPrice)
                        binding.editCategory.setText(selectedCategory)
                        Picasso.get().load(productImage).into(binding.productImg)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 1002
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}