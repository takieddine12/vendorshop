package com.android.app.shoppy.adapters

import com.android.app.shoppy.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.databinding.ProductsRowsLayoutBinding
import com.android.app.shoppy.listeners.ProductInfoListener
import com.android.app.shoppy.models.ProductModel

class ProductsAdapter(var productsList : MutableList<ProductModel>,
                      var productInfoListener: ProductInfoListener)
    : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>(){

    var isCloseImageVisible = false
    var isDeleteImageVisible = false

    inner  class ProductViewHolder(var productsRowsLayoutBinding: ProductsRowsLayoutBinding)
        : RecyclerView.ViewHolder(productsRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.products_rows_layout,parent,false

                )
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val data = productsList[position]
        holder.productsRowsLayoutBinding.model = data
        holder.productsRowsLayoutBinding.listener = productInfoListener

    }



    override fun getItemCount(): Int {
       return productsList.size
    }
}