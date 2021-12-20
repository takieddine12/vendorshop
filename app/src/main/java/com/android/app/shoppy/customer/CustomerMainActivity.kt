package com.android.app.shoppy.customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.android.app.shoppy.R
import com.android.app.shoppy.adapters.ProductsAdapter
import com.android.app.shoppy.customer.products.PaymentActivity
import com.android.app.shoppy.databinding.ActivityCustomerMainBinding
import com.android.app.shoppy.listeners.ProductInfoListener
import com.android.app.shoppy.models.ProductModel
import com.android.app.shoppy.roomdb.PaymentDatabase
import com.android.app.shoppy.ui.CustomerOrdersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.String


class CustomerMainActivity : AppCompatActivity() {
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var productList : MutableList<ProductModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var notificationImg : ImageView
    private lateinit var badgeTxt : TextView
    private lateinit var menuItem : MenuItem
    private var notificationCounter = 0
    private var _binding : ActivityCustomerMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCustomerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = ""


        init()
        getAllProducts()

        binding.orders.setOnClickListener {
            val intent = Intent(this,CustomerOrdersActivity::class.java)
            startActivity(intent)
        }


    }


    private fun init(){

        productList = mutableListOf()

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = GridLayoutManager(this,2)
        binding.recycler.setHasFixedSize(true)

    }

    private fun getAllProducts(){
        databaseReference
            .child("Sellers")
            .child("products")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(ds in snapshot.children){
                            val productModel = ds.getValue(ProductModel::class.java)
                            productList.add(productModel!!)
                            productsAdapter = ProductsAdapter(productList,object : ProductInfoListener{
                                override fun getProductInfo(model: ProductModel) {

                                }

                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser == null){
            Intent(this, CustomerLoginAcitivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.simple_menu2, menu)

        menuItem = menu?.findItem(R.id.cart)!!
        menuItem.setActionView(R.layout.action_layout)
        val view: View = menuItem.actionView

        badgeTxt = view.findViewById(R.id.badgeTxt)
        notificationImg = view.findViewById(R.id.notificationImg)

        menuItem.actionView.setOnClickListener {
            Intent(this, PaymentActivity::class.java).apply {
                startActivity(this)
            }
        }

        try {

            CoroutineScope(Dispatchers.IO).launch {
               val paymentDB =  PaymentDatabase.invoke(this@CustomerMainActivity).paymentDao()
                  withContext(Dispatchers.Main){
                      paymentDB.getAllPayments()
                          .observe(this@CustomerMainActivity,{
                              notificationCounter = it.size

                              if(notificationCounter == 0){
                                  badgeTxt.visibility = View.GONE

                              } else {
                                  badgeTxt.visibility = View.VISIBLE
                                  badgeTxt.text = String.valueOf(notificationCounter)

                              }
                          })
                  }
            }

        }catch (ex : Exception){}
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.profile -> {
                Intent(this, CustomerProfileActivity::class.java).apply {
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
                    FirebaseAuth.getInstance().signOut()
                    dialog.dismiss()
                    Intent(this@CustomerMainActivity, CustomerLoginAcitivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }
                noBtn.setOnClickListener {
                    dialog.dismiss()
                }
            }
            R.id.cart -> {
                Intent(this, PaymentActivity::class.java).apply {
                    startActivity(this)
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