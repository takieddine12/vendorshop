package com.android.app.shoppy.seller.products

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.R
import com.android.app.shoppy.Utils
import com.android.app.shoppy.databinding.ActivityAddProductBinding
import com.android.app.shoppy.seller.SellerMainActivity
import com.android.app.shoppy.seller.SellerRegistrationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*

class AddProductActivity : AppCompatActivity() {
    private var selectedCategory : String = ""
    private lateinit var imageDownloadUrl : String
    private lateinit var job : Job
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var progressDialog: ProgressDialog
    private var imageUrl : Uri? = null
    private var _binding  : ActivityAddProductBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        init()

        binding.addProduct.setOnClickListener {
            addNewProduct()
        }

        binding.productImg.setOnClickListener {
            getProductImage()
        }

        binding.editCategory.setText(Utils.productsCategories[0])

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
    }

    private fun  getProductImage(){
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == CropImageActivity.RESULT_OK){
                val uri  = result.uri
                imageUrl = uri
                binding.productImg.setImageURI(uri)
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            /// error picking an image
        }
    }

    private fun init(){
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
    }

    private fun addNewProduct(){

        val productID = System.currentTimeMillis().toString()

        val productName = binding.productName.text.toString()
        val productPrice = binding.productPrice.text.toString()
        val productDiscount = binding.productDiscount.text.toString()
        val productDeliveryFee = binding.deliveryFee.text.toString()
        val productDescription = binding.productDescription.text.toString()

        if(TextUtils.isEmpty(productName)){
            Toast.makeText(this,"Missing Product Name...",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(productDescription)){
            Toast.makeText(this,"Missing Product Description...",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(this,"Missing Product Price...",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(productDiscount)){
            Toast.makeText(this,"Missing Product Discunt...",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(productDeliveryFee)){
            Toast.makeText(this,"Missing Delivery Fee...",Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(selectedCategory)){
            Toast.makeText(this@AddProductActivity,"Please pick a category",Toast.LENGTH_SHORT).show()
            return
        }

        job = lifecycleScope.launchWhenStarted {
            progressDialog.show()
            try {
                imageDownloadUrl = if(imageUrl == null){ ""
                    Toast.makeText(this@AddProductActivity,"Please select an image",Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
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
                productDetailsMap["productID"] = productID.toString()
                productDetailsMap["productSavedTime"] = productID.toString()
                productDetailsMap["shopStatus"] = "Open"
                productDetailsMap["productImage"] = imageDownloadUrl
                productDetailsMap["productCategory"] = selectedCategory
                productDetailsMap["productDescription"] = productDescription
                productDetailsMap["sellerUid"] = firebaseAuth.currentUser!!.uid

                databaseReference.child("Sellers")
                    .child("products")
                    .child(productID)
                    .setValue(productDetailsMap).await()

                Toast.makeText(this@AddProductActivity,"Product Successfully Added",Toast.LENGTH_SHORT).show()

                binding.productName.setText("")
                binding.productPrice.setText("")
                binding.productDiscount.setText("")
                binding.deliveryFee.setText("")
                binding.productImg.setImageResource(R.drawable.ic_store)
                binding.productDescription.setText("")
                binding.editCategory.setText(Utils.productsCategories[0])

                progressDialog.dismiss()

            }catch (ex : Exception){
                Log.d("TAG","Firebase Exception ${ex.message.toString()}")
            }
        }

    }

    companion object {
        const val IMAGE_REQUEST_CODE = 1002
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,SellerMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
            finish()
        }
    }
}