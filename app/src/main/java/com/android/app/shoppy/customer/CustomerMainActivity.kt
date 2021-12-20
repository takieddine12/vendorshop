package com.android.app.shoppy.customer

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.app.shoppy.R
import com.android.app.shoppy.customer.products.PaymentActivity
import com.android.app.shoppy.databinding.ActivityCustomerMainBinding
import com.android.app.shoppy.fragments.customer.CustomerFirstFragment
import com.android.app.shoppy.fragments.customer.CustomerSecondFragment
import com.android.app.shoppy.roomdb.PaymentDatabase
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.String


class CustomerMainActivity : AppCompatActivity() {

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


        binding.tab.addTab(binding.tab.newTab().setText("Shops"))
        binding.tab.addTab(binding.tab.newTab().setText("Orders"))

        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (binding.tab.selectedTabPosition) {
                    0 -> {
                        supportFragmentManager.beginTransaction().replace(
                                R.id.frameLayout, CustomerFirstFragment()).commit()
                        binding.tab.setTabTextColors(Color.BLACK,Color.WHITE)
                    }
                    1 -> {
                        supportFragmentManager.beginTransaction().replace(
                                R.id.frameLayout, CustomerSecondFragment()).commit()
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
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout,
                    CustomerFirstFragment()).commit()
        }

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