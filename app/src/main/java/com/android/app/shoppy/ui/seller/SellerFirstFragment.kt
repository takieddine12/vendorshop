package com.android.app.shoppy.ui.seller

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.app.shoppy.adapters.ProductsAdapter
import com.android.app.shoppy.databinding.SellerFirstLayoutBinding
import com.android.app.shoppy.listeners.ProductInfoListener
import com.android.app.shoppy.models.ProductModel
import com.android.app.shoppy.seller.products.AddProductActivity
import com.android.app.shoppy.seller.products.ShowProductActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class SellerFirstFragment : Fragment() {

    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var productsList : MutableList<ProductModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : SellerFirstLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SellerFirstLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        getProducts()

        binding.editSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProductsList(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.add.setOnClickListener {
            Intent(requireContext(), AddProductActivity::class.java).apply {
                startActivity(this)
                requireActivity().finish()
            }
        }
    }
    private fun init(){
        productsList = mutableListOf()
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL)
        binding.recycler.setHasFixedSize(true)
    }

    
//    // Filter List using realtime db
//    private fun filterList(query : String){
//
//        databaseReference
//            .child("Sellers")
//            .child(firebaseAuth.currentUser?.uid!!)
//            .child("products")
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.exists()) {
//                        for(ds in snapshot.children){
//                            val productID = ds.child("productID").value.toString()
//                            finalListFiltering(productID,query)
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })
//    }
//
//    private fun finalListFiltering(productID: String,query: String) {
//        databaseReference
//            .child("Sellers")
//            .child(firebaseAuth.currentUser?.uid!!)
//            .child("products")
//            .child(productID)
//            .orderByChild("productName").startAt(query).endAt("$query~")
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()){
//                        val productPrice = snapshot.child("productPrice").value.toString()
//                        val productName = snapshot.child("productName").value.toString()
//                        val productDiscount = snapshot.child("productDiscount").value.toString()
//                        val productDeliveryFee = snapshot.child("productDeliveryFee").value.toString()
//                        val productSavedTime = snapshot.child("productSavedTime").value.toString()
//                        val shopStatus = snapshot.child("shopStatus").value.toString()
//                        val productImage = snapshot.child("productImage").value.toString()
//                        val productCategory = snapshot.child("productCategory").value.toString()
//                        val productDescription = snapshot.child("productDescription").value.toString()
//
//                        val productModel = ProductModel(
//                            productPrice,
//                            productName,
//                            productDiscount,
//                            productDeliveryFee,
//                            productID,
//                            productSavedTime,
//                            shopStatus,
//                            productImage,
//                            productCategory,
//                            productDescription
//                        )
//
//                        productsList.add(productModel)
//                        productsAdapter = ProductsAdapter(productsList, object : ProductInfoListener {
//                            override fun getProductInfo(model: ProductModel) {
//                                Intent(requireContext(), ShowProductActivity::class.java).apply {
//                                    putExtra("model", model)
//                                    startActivity(this)
//                                }
//                            }
//                        })
//                        binding.recycler.adapter = productsAdapter
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
//
//    }

    private fun getProducts(){
        productsList.clear()
        databaseReference.child("Sellers")
            .child(firebaseAuth.currentUser?.uid!!)
            .child("products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val productPrice = ds.child("productPrice").value.toString()
                        val productName = ds.child("productName").value.toString()
                        val productDiscount = ds.child("productDiscount").value.toString()
                        val productDeliveryFee = ds.child("productDeliveryFee").value.toString()
                        val productID = ds.child("productID").value.toString()
                        val productSavedTime = ds.child("productSavedTime").value.toString()
                        val shopStatus = ds.child("shopStatus").value.toString()
                        val productImage = ds.child("productImage").value.toString()
                        val productCategory = ds.child("productCategory").value.toString()
                        val productDescription = ds.child("productDescription").value.toString()

                        val productModel = ProductModel(
                            productPrice,
                            productName,
                            productDiscount,
                            productDeliveryFee,
                            productID,
                            productSavedTime,
                            shopStatus,
                            productImage,
                            productCategory,
                            productDescription
                        )

                        productsList.add(productModel)
                        productsAdapter = ProductsAdapter(productsList, object : ProductInfoListener {
                                override fun getProductInfo(model: ProductModel) {
                                    Intent(requireContext(), ShowProductActivity::class.java).apply {
                                        putExtra("model", model)
                                        startActivity(this)
                                    }
                                }
                            })
                        binding.recycler.adapter = productsAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    
    // Filter List 
    private fun filterProductsList(query : String){
        val simpleList = mutableListOf<ProductModel>()
        for(model in productsList){
            if(model.productName.toLowerCase().contains(query)){
                simpleList.add(model)

                productsAdapter = ProductsAdapter(simpleList, object : ProductInfoListener {
                    override fun getProductInfo(model: ProductModel) {
                        Intent(requireContext(), ShowProductActivity::class.java).apply {
                            putExtra("model", model)
                            startActivity(this)
                        }
                    }
                })
                binding.recycler.adapter = productsAdapter
            } else {
                productsAdapter = ProductsAdapter(simpleList, object : ProductInfoListener {
                    override fun getProductInfo(model: ProductModel) {
                        Intent(requireContext(), ShowProductActivity::class.java).apply {
                            putExtra("model", model)
                            startActivity(this)
                        }
                    }
                })
                binding.recycler.adapter = productsAdapter
            }
        }
    }
}