package com.android.app.shoppy.fragments.customer

import android. app.DatePickerDialog
import android.media.CamcorderProfile
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.shoppy.adapters.CustomerOrdersAdapter
import com.android.app.shoppy.databinding.CustomerSeconLayoutBinding
import com.android.app.shoppy.models.OrderDetailsModel
import com.android.app.shoppy.models.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CustomerSecondFragment : Fragment() {

    private lateinit var calendar : Calendar
    private lateinit var ordersAdapter: CustomerOrdersAdapter
    private lateinit var orderDetailsList : MutableList<OrderDetailsModel>
    private lateinit  var ordersList: MutableList<OrderModel>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var _binding : CustomerSeconLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = CustomerSeconLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        getCustomerOrders()
        binding.editDate.setOnClickListener {
             showDatePicker()
         }

    }

    var dateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            calendar.set(Calendar.MONTH,month)
            calendar.set(Calendar.YEAR,year)
        }
    private fun showDatePicker() {

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        DatePickerDialog(requireContext(),dateListener,year,month,dayOfMonth).show()


        // Update edittext and filter data
        val simpleDateFormat = SimpleDateFormat("dd MM yyyy",Locale.getDefault())
        val formattedDate = simpleDateFormat.format(calendar.time)
        binding.editDate.setText(formattedDate)

        filterOrders(formattedDate)
    }

    private fun init(){

        calendar = Calendar.getInstance()

        ordersList = mutableListOf()
        orderDetailsList = mutableListOf()

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.setHasFixedSize(true)
    }

    private fun getCustomerOrders(){

        databaseReference.child("Accounts")
                .child("Customers")
                .child(firebaseAuth.currentUser?.uid!!)
                .child("payments")
                .addListenerForSingleValueEvent(object : ValueEventListener{
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
                                val totalCost = ds.child("totalCost").value.toString()

                                val orderModel = OrderModel(
                                    orderId,address,city,country,orderStatus,allQuantity,orderTime,customerUID,totalCost)

                                ordersList.add(orderModel)

                                val genericType = object : GenericTypeIndicator<ArrayList<OrderDetailsModel>>(){}
                                orderDetailsList = ds.child("productsList").getValue(genericType)?.toMutableList()!!

                                ordersAdapter = CustomerOrdersAdapter(orderDetailsList,orderList = ordersList)
                                binding.recycler.adapter = ordersAdapter
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }
//    private fun filterList(query : String ){
//        databaseReference.child("Accounts")
//            .child("Customers")
//            .child(firebaseAuth.currentUser?.uid!!)
//            .child("payments")
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.exists()){
//                       for(ds in snapshot.children){
//                           val genericTypeIndicator = object : GenericTypeIndicator<ArrayList<OrderModel>>(){}
//                           var orderList = ds.child("productsList").getValue(genericTypeIndicator)
//
//                           for(i in 0 until orderList!!.size){
//                               if(orderList[i]. == query){
//                                   ordersAdapter = CustomerOrdersAdapter(ordersList)
//                                   binding.recycler.adapter = ordersAdapter
//                               }
//                           }
//                       }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })
//    }

    private fun filterOrders(orderDate : String){
        val simpleList = mutableListOf<OrderModel>()
        for(model in ordersList){
            val orderTime = model.orderTime
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = orderTime.toLong()
            val formattedDate = DateFormat.format("dd MM yyyy",calendar.time).toString()

            if(formattedDate.contains(orderDate)){
                simpleList.add(model)
                ordersAdapter = CustomerOrdersAdapter(orderDetailsList = orderDetailsList,orderList = simpleList)
                binding.recycler.adapter = ordersAdapter
            }
        }
    }
}