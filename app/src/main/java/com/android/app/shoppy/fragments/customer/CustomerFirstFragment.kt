package com.android.app.shoppy.fragments.customer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.shoppy.adapters.StoresAdapter
import com.android.app.shoppy.customer.products.StoreProductsActivity
import com.android.app.shoppy.databinding.CustomerFirstLayoutBinding
import com.android.app.shoppy.listeners.StoresListener
import com.android.app.shoppy.models.StoresModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.w3c.dom.Text

class CustomerFirstFragment : Fragment() {
    private lateinit var storesAdapter: StoresAdapter
    private lateinit var storesList : MutableList<StoresModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : CustomerFirstLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = CustomerFirstLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        getAvailableStores()

        binding.editSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStores(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }



//    private fun filterDatabase(query : String){
//        databaseReference
//            .child("Accounts")
//            .child("Sellers")
//            .orderByChild("shopName").equalTo(query)
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.exists()){
//                        val accountType = snapshot.child("accountType").value.toString()
//                        val profileImage = snapshot.child("profileImage").value.toString()
//                        val sellerEmail = snapshot.child("sellerEmail").value.toString()
//                        val sellerName = snapshot.child("sellerName").value.toString()
//                        val sellerPhone = snapshot.child("sellerPhone").value.toString()
//                        val shopName = snapshot.child("shopName").value.toString()
//                        val sellerUid = snapshot.child("sellerUid").value.toString()
//
//                        val storesModel = StoresModel(accountType,profileImage,sellerEmail,
//                            sellerName,sellerPhone,shopName,sellerUid)
//                        ///////
//
//                        storesList.add(storesModel)
//                        storesAdapter = StoresAdapter(storesList,object : StoresListener{
//                            override fun getStoreProduct(model: StoresModel) {
//                                Intent(requireContext(),StoreProductsActivity::class.java).apply {
//                                    putExtra("sellerUid",model.sellerUid)
//                                    startActivity(this)
//                                }
//                            }
//                        })
//                        binding.recycler.adapter = storesAdapter
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
//    }
    private fun init(){

        storesList = mutableListOf()

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.setHasFixedSize(true)

    }

    private fun getAvailableStores(){
        databaseReference.child("Accounts")
                .child("Sellers")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(ds in snapshot.children){
                            val accountType = ds.child("accountType").value.toString()
                            val profileImage = ds.child("profileImage").value.toString()
                            val sellerEmail = ds.child("sellerEmail").value.toString()
                            val sellerName = ds.child("sellerName").value.toString()
                            val sellerPhone = ds.child("sellerPhone").value.toString()
                            val shopName = ds.child("shopName").value.toString()
                            val sellerUid = ds.child("sellerUid").value.toString()

                            val storesModel = StoresModel(accountType,profileImage,sellerEmail,
                                    sellerName,sellerPhone,shopName,sellerUid)
                            ///////

                            storesList.add(storesModel)
                            storesAdapter = StoresAdapter(storesList,object : StoresListener{
                                override fun getStoreProduct(model: StoresModel) {
                                    Intent(requireContext(),StoreProductsActivity::class.java).apply {
                                        putExtra("sellerUid",model.sellerUid)
                                        startActivity(this)
                                    }
                                }
                            })
                            binding.recycler.adapter = storesAdapter
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    private fun filterStores(storeName : String){
        val simpleList = mutableListOf<StoresModel>()
        for(model in storesList){
            if(model.shopName.toLowerCase().contains(storeName)){
                simpleList.add(model)

                storesAdapter = StoresAdapter(simpleList,object : StoresListener{
                    override fun getStoreProduct(model: StoresModel) {
                        Intent(requireContext(),StoreProductsActivity::class.java).apply {
                            putExtra("sellerUid",model.sellerUid)
                            startActivity(this)
                        }
                    }
                })
                binding.recycler.adapter = storesAdapter
            }
        }
    }
}