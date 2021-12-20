package com.android.app.shoppy.adapters

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.R
import com.android.app.shoppy.databinding.SellerOrdersRowsLayoutBinding
import com.android.app.shoppy.models.OrderDetailsModel
import com.android.app.shoppy.models.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList


class SellerOrdersAdapter(
    private var context: Context,
    private var ordersList: MutableList<OrderModel>,
    private var orderDetailsList : MutableList<OrderDetailsModel>
) : RecyclerView.Adapter<SellerOrdersAdapter.OrderViewHolder>() {

    private lateinit var sellerSubOrdersAdapter: SellerSubOrdersAdapter
    private var recyclerViewPool = RecyclerView.RecycledViewPool()
    private var progressDialog : ProgressDialog = ProgressDialog(context)
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private  var customerUID : String = ""
    init {
        progressDialog.setMessage("Processing Request..")
        progressDialog.setCanceledOnTouchOutside(false)
    }
    inner  class OrderViewHolder(var sellerOrdersRowsLayoutBinding: SellerOrdersRowsLayoutBinding)
        : RecyclerView.ViewHolder(sellerOrdersRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerOrdersAdapter.OrderViewHolder {
        return OrderViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.seller_orders_rows_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orders = ordersList[position]
        holder.sellerOrdersRowsLayoutBinding.model = orders

        val layoutManager = LinearLayoutManager(holder.sellerOrdersRowsLayoutBinding.root.context)
        layoutManager.initialPrefetchItemCount = ordersList.size

        holder.sellerOrdersRowsLayoutBinding.subRecycler.layoutManager = layoutManager
        holder.sellerOrdersRowsLayoutBinding.subRecycler.setHasFixedSize(true)

        sellerSubOrdersAdapter = SellerSubOrdersAdapter(context,orderDetailsList)

        holder.sellerOrdersRowsLayoutBinding.subRecycler.adapter = sellerSubOrdersAdapter
        holder.sellerOrdersRowsLayoutBinding.subRecycler.setRecycledViewPool(recyclerViewPool)


        // TODO : Set Shipping address
        val stringBuilder = StringBuilder().append("Shipping To : ")
            .append(orders.address)
            .append(" , ")
            .append(orders.country)
            .append(" , ")
            .append(orders.city)

        holder.sellerOrdersRowsLayoutBinding.shippingAddress.text = stringBuilder.toString()
        val pattern = "dd MM yyyy , HH:mm"
        val calendar = Calendar.getInstance()
        calendar.timeInMillis  = orders.orderTime.toLong()
        val dateFormatted = android.text.format.DateFormat.format(pattern, calendar.timeInMillis).toString()
        holder.sellerOrdersRowsLayoutBinding.orderDate.text = "Ordered On : $dateFormatted"

        //---
        databaseReference
            .child("Accounts")
            .child("Sellers")
            .child(firebaseAuth.currentUser?.uid!!)
            .child("payments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for(ds in snapshot.children){
                            val genericTypeIndicator = object : GenericTypeIndicator<ArrayList<OrderModel>>() {}
                            val orderList = ds.child("productsList").getValue(genericTypeIndicator)
                            customerUID = ds.child("customerUID").value.toString()
                            for (i in 0 until orderList?.size!!) {
                                if (orderList[i].orderStatus == "Awaiting Acceptance") {
                                    holder.sellerOrdersRowsLayoutBinding.layout.visibility =
                                        View.VISIBLE
                                    holder.sellerOrdersRowsLayoutBinding.accept.visibility =
                                        View.VISIBLE
                                    holder.sellerOrdersRowsLayoutBinding.cancel.visibility =
                                        View.VISIBLE

                                    holder.sellerOrdersRowsLayoutBinding.layout.layoutParams =
                                        LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                    holder.sellerOrdersRowsLayoutBinding.accept.layoutParams =
                                        LinearLayout.LayoutParams(
                                            150,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                    holder.sellerOrdersRowsLayoutBinding.cancel.layoutParams =
                                        LinearLayout.LayoutParams(
                                            150,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                } else {
                                    holder.sellerOrdersRowsLayoutBinding.layout.visibility =
                                        View.VISIBLE
                                    holder.sellerOrdersRowsLayoutBinding.accept.visibility =
                                        View.VISIBLE
                                    holder.sellerOrdersRowsLayoutBinding.cancel.visibility =
                                        View.VISIBLE

                                    holder.sellerOrdersRowsLayoutBinding.layout.layoutParams =
                                        LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                    holder.sellerOrdersRowsLayoutBinding.accept.layoutParams =
                                        LinearLayout.LayoutParams(0, 0)
                                    holder.sellerOrdersRowsLayoutBinding.cancel.layoutParams =
                                        LinearLayout.LayoutParams(0, 0)
                                }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })

        holder.sellerOrdersRowsLayoutBinding.accept.setOnClickListener {
            progressDialog.show()

            val hashMap = hashMapOf<String,Any>()
            hashMap["orderStatus"] = "Accepted"
            val currentOrderID = orders.orderId
            databaseReference
                .child("Accounts")
                .child("Sellers")
                .child(firebaseAuth.currentUser?.uid!!)
                .child("payments")
                .child(currentOrderID)
                .child("productsList")
                .child(position.toString())
                .updateChildren(hashMap)

             Log.d("POSTION TAG","Position $position")
            databaseReference
                .child("Accounts")
                .child("Customers")
                .child(customerUID)
                .child("payments")
                .child(currentOrderID)
                .child("productsList")
                .child(position.toString())
                .updateChildren(hashMap)

            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                progressDialog.hide()
            }
        }
        holder.sellerOrdersRowsLayoutBinding.cancel.setOnClickListener {
            // refuse order
            progressDialog.show()
            val hashMap = hashMapOf<String,Any>()
            hashMap["orderStatus"] = "Rejected"
            val currentOrderID = orders.orderId
            databaseReference
                .child("Accounts")
                .child("Sellers")
                .child(firebaseAuth.currentUser?.uid!!)
                .child("payments")
                .child(currentOrderID)
                .updateChildren(hashMap)

            databaseReference
                .child("Accounts")
                .child("Customers")
                .child(firebaseAuth.currentUser?.uid!!)
                .child("payments")
                .child(currentOrderID)
                .updateChildren(hashMap)

            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                progressDialog.hide()
            }
        }
    }

    override fun getItemCount(): Int {
        return ordersList.size
    }


}