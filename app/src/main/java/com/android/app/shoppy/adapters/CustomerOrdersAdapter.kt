package com.android.app.shoppy.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.R
import com.android.app.shoppy.databinding.CustomerOrdersRowsLayoutBinding
import com.android.app.shoppy.models.OrderDetailsModel
import com.android.app.shoppy.models.OrderModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.StringBuilder


class CustomerOrdersAdapter(
    private var orderDetailsList: MutableList<OrderDetailsModel>,
    private var orderList: MutableList<OrderModel>
) : RecyclerView.Adapter<CustomerOrdersAdapter.OrderViewHolder>() {

    // data + orderdetailsmodel
    private var recyclerViewPool = RecyclerView.RecycledViewPool()
    private lateinit var customSubOrderAdapter: CustomSubOrderAdapter

    inner  class OrderViewHolder(var customerOrdersRowsLayoutBinding: CustomerOrdersRowsLayoutBinding)
        : RecyclerView.ViewHolder(customerOrdersRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerOrdersAdapter.OrderViewHolder {
        return OrderViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.customer_orders_rows_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orders = orderList[position]
        holder.customerOrdersRowsLayoutBinding.model = orders

        val layoutManager = LinearLayoutManager(holder.customerOrdersRowsLayoutBinding.root.context)
        layoutManager.initialPrefetchItemCount = orderList.size

        holder.customerOrdersRowsLayoutBinding.subRecycler.layoutManager = layoutManager
        holder.customerOrdersRowsLayoutBinding.subRecycler.setHasFixedSize(true)

        customSubOrderAdapter = CustomSubOrderAdapter(orderDetailsList)

        holder.customerOrdersRowsLayoutBinding.subRecycler.adapter = customSubOrderAdapter
        holder.customerOrdersRowsLayoutBinding.subRecycler.setRecycledViewPool(recyclerViewPool)


        // TODO : set shipping address
        val stringBuilder = StringBuilder().append("Shipping To : ")
            .append(orders.address)
            .append(" , ")
            .append(orders.country)
            .append(" , ")
            .append(orders.city)

        holder.customerOrdersRowsLayoutBinding.shippingAddress.text = stringBuilder.toString()


        val pattern = "dd MM yyyy , HH:mm"
        val calendar = Calendar.getInstance()
        calendar.timeInMillis  = orders.orderTime.toLong()
        val dateFormatted = android.text.format.DateFormat.format(pattern, calendar.timeInMillis).toString()
        holder.customerOrdersRowsLayoutBinding.orderDate.text = "Ordered On : $dateFormatted"


    }

    override fun getItemCount(): Int {
        return orderList.size
    }


}