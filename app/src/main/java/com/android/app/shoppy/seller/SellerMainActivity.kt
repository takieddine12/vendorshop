package com.android.app.shoppy.seller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.app.shoppy.R
import com.android.app.shoppy.adapters.ProductsAdapter
import com.android.app.shoppy.databinding.ActivitySellerMainBinding
import com.android.app.shoppy.listeners.ProductInfoListener
import com.android.app.shoppy.models.OrderModel
import com.android.app.shoppy.models.ProductModel
import com.android.app.shoppy.notification.NotificationService
import com.android.app.shoppy.seller.products.AddProductActivity
import com.android.app.shoppy.seller.products.ShowProductActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class SellerMainActivity : AppCompatActivity() {
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var productsList : MutableList<ProductModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivitySellerMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         _binding = ActivitySellerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = ""

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        init()
        getProducts()

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(value : CharSequence?, start: Int, before: Int, count: Int) {
                if(value.toString().isNotEmpty()){
                    filterProductsList(value.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(value : Editable?) {

            }
        })

        binding.add.setOnClickListener {
            Intent(this, AddProductActivity::class.java).apply {
                startActivity(this)

            }
        }
        binding.orders.setOnClickListener {
            //val intent = Intent(this,)
        }
        sendSellerNotification()

    }

    private fun init(){
        productsList = mutableListOf()
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recycler.setHasFixedSize(true)
    }

    private fun sendSellerNotification(){
        databaseReference
            .child("Accounts")
            .child("Sellers")
            .child(firebaseAuth.currentUser?.uid!!)
            .child("payments")
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(ds in snapshot.children){
                            val generic = object : GenericTypeIndicator<ArrayList<OrderModel>>() {}
                            val ordersList = ds.child("productsList").getValue(generic)

                            val orderDate = ds.child("orderTime").value.toString()


                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = orderDate.toLong()
                            val dateToFormat = DateFormat.format("dd-MM-yyyy , HH:mm",calendar.time).toString()

                            for(i in 0 until ordersList?.size!!){
                                if(ordersList[i].orderStatus == "Awaiting Acceptance"){
                                    val intent  = Intent(this@SellerMainActivity, NotificationService::class.java)
                                    intent.putExtra("date",dateToFormat)
                                    intent.putExtra("orderId",ordersList[i].orderId)
                                    startService(intent)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.simple_menu,menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser == null){
            Intent(this,SellerMainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }else {
            sellerStatus("open")
        }
    }

    private fun sellerStatus(status : String){
        val hashMap  = hashMapOf<String,Any>()
        hashMap["status"] = status
        databaseReference
            .child("Accounts")
            .child("Sellers")
            .child(firebaseAuth.currentUser?.uid!!)
            .updateChildren(hashMap)
    }

    private fun getProducts(){
        productsList.clear()
        databaseReference
            .child("Sellers")
            .child("products")
            .orderByChild("sellerUid").equalTo(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val productModel = ds.getValue(ProductModel::class.java)
                        productsList.add(productModel!!)
                        productsAdapter = ProductsAdapter(productsList, object :
                            ProductInfoListener {
                            override fun getProductInfo(model: ProductModel) {
                                Intent(this@SellerMainActivity, ShowProductActivity::class.java).apply {
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

    private fun filterProductsList(query : String){
        val emptyList = mutableListOf<ProductModel>()
        for(model in productsList){
            if(model.productName.toLowerCase().contains(query)){
                emptyList.add(model)
                productsAdapter = ProductsAdapter(emptyList, object : ProductInfoListener {
                    override fun getProductInfo(model: ProductModel) {
                        Intent(this@SellerMainActivity, ShowProductActivity::class.java).apply {
                            putExtra("model", model)
                            startActivity(this)
                        }
                    }
                })
                binding.recycler.adapter = productsAdapter
            } else {
                productsAdapter = ProductsAdapter(productsList, object : ProductInfoListener {
                    override fun getProductInfo(model: ProductModel) {
                        Intent(this@SellerMainActivity, ShowProductActivity::class.java).apply {
                            putExtra("model", model)
                            startActivity(this)
                        }
                    }
                })
                binding.recycler.adapter = productsAdapter
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.profile -> {
                Intent(this, SellerProfileActivity::class.java).apply {
                    startActivity(this)
                }
            }
            R.id.logout -> {
                val view = LayoutInflater.from(this).inflate(R.layout.cupertino_dialog_layout,null)
                val title = view.findViewById<TextView>(R.id.title)
                val body = view.findViewById<TextView>(R.id.body)
                val yesBtn = view.findViewById<TextView>(R.id.yes)
                val noBtn = view.findViewById<TextView>(R.id.no)


                val dialog = AlertDialog.Builder(this)
                    .setView(view)
                    .create()
                dialog.show()


                yesBtn.text = "Sign out"
                noBtn.text = "Cancel"

                title.text = "Logging Out!"
                body.text = "Are you sure you want to quit app!"
                yesBtn.setOnClickListener {
                    sellerStatus("Closed")
                    FirebaseAuth.getInstance().signOut()
                    Intent(this, SellerLoginActivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }
                noBtn.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}