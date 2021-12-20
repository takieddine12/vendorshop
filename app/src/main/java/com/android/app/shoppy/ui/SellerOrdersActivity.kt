package com.android.app.shoppy.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.shoppy.adapters.SellerOrdersAdapter
import com.android.app.shoppy.databinding.ActivityCustomerMainBinding
import com.android.app.shoppy.databinding.SellerSecondLayoutBinding
import com.android.app.shoppy.models.OrderDetailsModel
import com.android.app.shoppy.models.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SellerOrdersActivity : AppCompatActivity() {

    private lateinit var cal : Calendar
    private lateinit var orderDetailsList : MutableList<OrderDetailsModel>
    private lateinit  var ordersList: MutableList<OrderModel>
    private lateinit var ordersAdapter: SellerOrdersAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding : SellerSecondLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SellerSecondLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        getSellerOrders()
        binding.editDate.setOnClickListener {
            showDatePicker()
        }

    }

    private val dateListener  = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
        cal.set(Calendar.MONTH,month)
        cal.set(Calendar.YEAR,year)
    }

    private fun showDatePicker() {

        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        DatePickerDialog(this,dateListener,dayOfMonth,month,year).show()

        // Update edittext and filter data
        val simpleDateFormat = SimpleDateFormat("dd MM yyyy",Locale.getDefault())
        val formattedDate = simpleDateFormat.format(cal.time)
        binding.editDate.setText(formattedDate)

        filterOrders(formattedDate)
    }

    private fun init(){

        cal = Calendar.getInstance()
        ordersList = mutableListOf()
        orderDetailsList = mutableListOf()
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.setHasFixedSize(true)
    }

    private fun getSellerOrders(){

        databaseReference.child("Accounts")
                .child("Sellers")
                .child(firebaseAuth.currentUser?.uid!!)
                .child("payments")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(ds in snapshot.children){

                                val orderId = ds.child("OrderId").value.toString()
                                val orderStatus = ds.child("OrderStatus").value.toString()
                                val address = ds.child("address").value.toString()
                                val allQuantity = ds.child("allQuantity").value.toString()
                                val city = ds.child("city").value.toString()
                                val country = ds.child("country").value.toString()
                                val customerUID = ds.child("customerUID").value.toString()
                                val orderTime = ds.child("orderTime").value.toString()
                                var totalCost = ds.child("totalCost").value.toString()

                                val orderModel = OrderModel(
                                    orderId,address,city,country,orderStatus,allQuantity,orderTime,customerUID,totalCost)

                                ordersList.add(orderModel)

                                val genericType = object : GenericTypeIndicator<ArrayList<OrderDetailsModel>>(){}
                                orderDetailsList = ds.child("productsList").getValue(genericType)!!

                                ordersAdapter = SellerOrdersAdapter(this@SellerOrdersActivity,ordersList,orderDetailsList)
                                binding.recycler.adapter = ordersAdapter
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    private fun filterOrders(orderDate : String){
        val simpleList = mutableListOf<OrderModel>()
        for(model in ordersList){
            val orderTime = model.orderTime
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = orderTime.toLong()
            val formattedDate = DateFormat.format("dd MM yyyy",calendar.time).toString()

            if(formattedDate.contains(orderDate)){
                simpleList.add(model)
                ordersAdapter = SellerOrdersAdapter(this,simpleList,orderDetailsList)
                binding.recycler.adapter = ordersAdapter
            }
        }
    }

}