package com.android.app.shoppy.customer.products

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.app.shoppy.Utils
import com.android.app.shoppy.auth.AuthResponse
import com.android.app.shoppy.customer.CustomerMainActivity
import com.android.app.shoppy.databinding.ActivityOrderCompletionBinding
import com.android.app.shoppy.models.OrderDetailsModel
import com.android.app.shoppy.roomdb.PaymentDatabase
import com.android.app.shoppy.roomdb.RoomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class OrderCompletionActivity : AppCompatActivity() {
    private lateinit var ordersList : MutableList<OrderDetailsModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : ActivityOrderCompletionBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderCompletionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ordersList = mutableListOf()

        init()

        binding.finish.setOnClickListener {
            Intent(this@OrderCompletionActivity, CustomerMainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
                finish()
            }
        }

        postDataIntoRealtimeDB()
    }

    private fun postDataIntoRealtimeDB(){
        intent?.let {
            val list = it.getSerializableExtra("list") as ArrayList<RoomModel>
            val totalCost = it.getStringExtra("totalCost")
            val allQuantity = it.getStringExtra("quantity")

            val globalTime = ""  + System.currentTimeMillis()

            for(i in 0 until list.size){
                val orderDetailsModel  = OrderDetailsModel(
                    list[i].itemTitle,
                    list[i].itemCategory,
                    list[i].itemDeliveryFee,
                    list[i].itemPriceEach,
                    list[i].itemQuantity
                )
                ordersList.add(orderDetailsModel)
            }


            val orderMap  = hashMapOf<String, Any>()
            orderMap["OrderStatus"] = "Awaiting Acceptance"
            orderMap["totalCost"] = totalCost.toString()
            orderMap["OrderId"] = globalTime
            orderMap["allQuantity"] = allQuantity.toString()
            orderMap["orderTime"]  = globalTime
            orderMap["customerUID"] = firebaseAuth.currentUser?.uid!!
            orderMap["country"] = list[0].country
            orderMap["city"] = list[0].city
            orderMap["address"] = list[0].address
            orderMap["productsList"] = ordersList

            uploadOrderToPhpServer(
                orderStatus = "Awaiting Acceptance",
                totalCost = totalCost.toString(),
                allQuantity = allQuantity.toString(),
                country = list[0].country,
                city =  list[0].city,
                address = list[0].address
            )
            lifecycleScope.launch {
                databaseReference
                    .child("Accounts")
                    .child("Sellers")
                    .child(list[0].sellerUid)
                    .child("payments")
                    .child(globalTime)
                    .setValue(orderMap).await()


                databaseReference
                    .child("Accounts")
                    .child("Customers")
                    .child(firebaseAuth.currentUser?.uid!!)
                    .child("payments")
                    .child(globalTime)
                    .setValue(orderMap).await()
            }

            CoroutineScope(Dispatchers.IO).launch {
                PaymentDatabase
                    .invoke(this@OrderCompletionActivity)
                    .paymentDao()
                    .deleteAllPayments()
            }
        }
    }

    private fun uploadOrderToPhpServer(
        orderStatus : String , totalCost : String , allQuantity : String , country : String , city : String ,
        address : String){

        Retrofit.Builder()
            .baseUrl(Utils.SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(AuthResponse::class.java)
            .postOrder(orderStatus,totalCost,System.currentTimeMillis().toString(),allQuantity,
                System.currentTimeMillis().toString(),firebaseAuth.currentUser?.uid!!.toString(),
              country,city,address)
            .enqueue(object  : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if(response.isSuccessful){
                        Log.d("TAG PROCESS","Response Code ${response.code()}")

                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("TAG PROCESS","Response Error ${t.message}")
                }
            })

    }
    private fun init(){
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this@OrderCompletionActivity, CustomerMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
            finish()
        }
    }
}