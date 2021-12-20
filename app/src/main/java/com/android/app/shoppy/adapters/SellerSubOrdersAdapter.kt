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
import com.android.app.shoppy.databinding.SellerSubOrdersRowsLayoutBinding
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


class SellerSubOrdersAdapter(
    private var context  : Context,
    private var ordersList: MutableList<OrderDetailsModel>
) : RecyclerView.Adapter<SellerSubOrdersAdapter.OrderViewHolder>() {

    private var progressDialog : ProgressDialog = ProgressDialog(context)
    init {
        progressDialog.setMessage("Processing Request..")
        progressDialog.setCanceledOnTouchOutside(false)
    }
    inner  class OrderViewHolder(var sellerSubOrdersRowsLayoutBinding: SellerSubOrdersRowsLayoutBinding)
        : RecyclerView.ViewHolder(sellerSubOrdersRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerSubOrdersAdapter.OrderViewHolder {
        return OrderViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.seller_sub_orders_rows_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orders = ordersList[position]
        holder.sellerSubOrdersRowsLayoutBinding.model = orders



    }

    override fun getItemCount(): Int {
        return ordersList.size
    }


}