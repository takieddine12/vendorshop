package com.android.app.shoppy.customer.products

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.simplepass.loadingbutton.customViews.ProgressButton
import com.android.app.shoppy.R
import com.android.app.shoppy.adapters.PaymentAdapter
import com.android.app.shoppy.customer.CustomerMainActivity
import com.android.app.shoppy.databinding.ActivityPaymentBinding
import com.android.app.shoppy.databinding.PaymentDialogBinding
import com.android.app.shoppy.listeners.PaymentListener
import com.android.app.shoppy.models.PaymentModel
import com.android.app.shoppy.roomdb.PaymentDatabase
import com.android.app.shoppy.roomdb.RoomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.view.CardInputWidget
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import p32929.androideasysql_library.Column
import p32929.androideasysql_library.EasyDB
import java.io.IOException
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PaymentActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var cardInputWidget: CardInputWidget
    private val backendUrl = "https://stripe-payment-api-dz.herokuapp.com/"
    private lateinit var okHttpClient: OkHttpClient
    private  var paymentIntentClientSecret: String? = null
    private lateinit var stripe: Stripe
    private var totalQuantity : Int = 0
    lateinit var country : String
    lateinit var city : String
    lateinit var address : String
    lateinit var state : String
    private var totalPrice = 0.0
    private lateinit var paymentAdapter: PaymentAdapter
    private lateinit var list : MutableList<RoomModel>
    private var _binding : ActivityPaymentBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO : Set up server key
        stripe = Stripe(applicationContext, "pk_test_51IVasBFP6YK87V1sCbMrswWJDG0nl4qdHUBsvzWqbDAKS4UwPTL5MKR1hulV26gQTYgRFPsZqk9zlg0Cw7RCA0Vc00Lzgmtm5n")

        init()
        getDataFromDB()
        binding.orderPayBtn.isEnabled = true
        binding.orderPayBtn.setOnClickListener {
            startCheckout()
        }
    }


    private fun init(){

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Processing Payment...")
        progressDialog.setCanceledOnTouchOutside(false)
        list = mutableListOf()
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)
        binding.orderRecycler.setHasFixedSize(true)
    }
    private fun getDataFromDB() {
        var subTotal : String = ""
        CoroutineScope(Dispatchers.IO).launch {
            val data = PaymentDatabase.invoke(this@PaymentActivity).paymentDao().getAllPayments()
            withContext(Dispatchers.Main){
                data.observe(this@PaymentActivity,{
                    if(it.size > 0){
                        list = it

                        binding.orderReceiver.text = list[0].customerName

                        paymentAdapter = PaymentAdapter(it, object : PaymentListener {
                            override fun onItemRemoved(model: RoomModel, position: Int) {

                                CoroutineScope(Dispatchers.IO).launch {
                                    PaymentDatabase.invoke(this@PaymentActivity)
                                        .paymentDao()
                                        .deleteOneItem(position)

                                    withContext(Dispatchers.Main){
                                        paymentAdapter.deleteNewPosition(position)
                                        paymentAdapter.notifyItemChanged(position)
                                        paymentAdapter.notifyDataSetChanged()

                                        totalQuantity -= model.itemQuantity.toInt()

                                        // SubTotal Price
                                        val subTotal = binding.orderSubTotal.text.toString().replace("$", "").toDouble() - model.itemTotalPrice.toDouble()
                                        binding.orderSubTotal.text = "$subTotal$"

                                        val taxVat = binding.orderTax.text.toString().replace("$","").toDouble()

                                        totalPrice -= (subTotal + taxVat)
                                        binding.orderTotalPrice.text = "$totalPrice$"
                                    }
                                }

                            }
                        })
                        binding.orderRecycler.adapter = paymentAdapter
                        binding.orderSubTotal.text = "" + 0

                        for (i in 0 until list.size) {

                            val itemTotalPrice = list[i].itemTotalPrice.toDouble()

                            subTotal = if(binding.orderSubTotal.text.toString().contains("$")){
                                binding.orderSubTotal.text.toString().replace("$","")
                            } else {
                                binding.orderSubTotal.text.toString()
                            }


                            val total = subTotal.toDouble() + itemTotalPrice
                            binding.orderSubTotal.text = "$$total"

                            val taxVat = binding.orderTax.text.toString().replace("$","").toDouble()

                            totalQuantity += list[i].itemQuantity.toInt()
                            totalPrice = total + taxVat
                            binding.orderTotalPrice.text = "$$totalPrice"

                        }
                    }
                })
            }
        }
    }
    private fun startCheckout() {

        var allQuantity : Int  = 0
        val view = LayoutInflater.from(this).inflate(R.layout.payment_dialog,null)
        val totalToPay = view.findViewById<TextView>(R.id.totalToPay)
        val btnPay = view.findViewById<Button>(R.id.pay)
        cardInputWidget = view.findViewById<CardInputWidget>(R.id.cardInputWidget)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)

        val dialog = alertDialog.create()
        dialog.show()

        totalToPay.text = totalPrice.toString()
        btnPay.setOnClickListener {
            progressDialog.show()
            //---------------
            val weakActivity = WeakReference<Activity>(this)
            // Create a PaymentIntent by calling your server's endpoint.
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val payMap  = hashMapOf<String, Any>()
            val itemMap  = hashMapOf<String, Any>()
            val paymentList = mutableListOf<Map<String, Any>>()

            for(i in 0 until list.size){
                payMap["currency"] = "usd"
                itemMap["customer"] = list[0].customerName
                itemMap["title"] = StringBuilder().append("*").append(list[i].itemTitle).toString()
                itemMap["amount"]  = totalPrice * 100
                itemMap["category"] = StringBuilder().append("*").append(list[i].itemCategory).toString()
                itemMap["country"] = list[0].country
                itemMap["city"] = list[0].city
                itemMap["address"] = list[0].address
                paymentList.add(itemMap)
                payMap["items"] = paymentList
            }
            val jsonBody = Gson().toJson(payMap)
            val body = RequestBody.create(mediaType, jsonBody)

            val request = Request.Builder()
                .url(backendUrl + "create-payment-intent")
                .post(body)
                .build()

            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    weakActivity.get()?.let { activity ->
                        displayAlert(activity, "Failed to load page", "Error: $e")
                        progressDialog.hide()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        weakActivity.get()?.let { activity ->
                            displayAlert(
                                activity,
                                "Failed to load page",
                                "Error: $response"
                            )
                        }
                    } else {
                        val responseData = response.body?.string()
                        val responseJson = responseData?.let { JSONObject(it) } ?: JSONObject()
                        // For added security, our sample app gets the publishable key
                        // from the server.
                        paymentIntentClientSecret = responseJson.getString("clientSecret")


                        for(i in 0 until list.size){
                            allQuantity += list[i].itemQuantity.toInt()
                        }

                        Intent(this@PaymentActivity,OrderCompletionActivity::class.java).apply {
                            putStringArrayListExtra("list",list as java.util.ArrayList<String>)
                            putExtra("quantity",allQuantity.toString())
                            putExtra("totalCost",totalPrice.toString())
                            startActivity(this)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            progressDialog.hide()
                        }

                    }
                }
            })

            if(paymentIntentClientSecret != null){
                cardInputWidget.paymentMethodCreateParams?.let { params ->
                    val confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(params, paymentIntentClientSecret!!)
                    stripe.confirmPayment(this, confirmParams)
                }
            }
        }


    }
    private fun displayAlert(activity: Activity, title: String, message: String, restartDemo: Boolean = false) {
        runOnUiThread {
            val builder = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
            builder.setPositiveButton("Ok", null)
            builder.create().show()
        }
    }
}