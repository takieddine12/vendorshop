package com.android.app.shoppy.seller

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.android.app.shoppy.R
import com.android.app.shoppy.databinding.ActivitySellerMainBinding
import com.android.app.shoppy.ui.seller.SellerFirstFragment
import com.android.app.shoppy.ui.seller.SellerSecondFragment
import com.android.app.shoppy.models.OrderModel
import com.android.app.shoppy.notification.NotificationService
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class SellerMainActivity : AppCompatActivity() {
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

        binding.tab.addTab(binding.tab.newTab().setText("Products"))
        binding.tab.addTab(binding.tab.newTab().setText("Orders"))

        binding.tab.addOnTabSelectedListener(object  : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(binding.tab.selectedTabPosition){
                    0 -> {
                        supportFragmentManager.beginTransaction().replace(
                            R.id.frameLayout,SellerFirstFragment()).commit()
                        binding.tab.setTabTextColors(Color.BLACK, Color.WHITE)
                    }
                    1 -> {
                        supportFragmentManager.beginTransaction().replace(
                            R.id.frameLayout,SellerSecondFragment()).commit()
                        binding.tab.setTabTextColors(Color.BLACK,Color.WHITE)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout,SellerFirstFragment()).commit()
        }

        sendSellerNotification()

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

    override fun onDestroy() {
        sellerStatus("Closed")
        super.onDestroy()

    }
}