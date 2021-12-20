package com.android.app.shoppy.adapters
import com.android.app.shoppy.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.databinding.MarchandiseRowsLayoutBinding
import com.android.app.shoppy.listeners.CartListener
import com.android.app.shoppy.models.ProductModel

class MarchandiseAdapter(var list : MutableList<ProductModel>,
                         var cartListener: CartListener) : RecyclerView.Adapter<MarchandiseAdapter.MarchandiseViewHolder>() {


    inner  class MarchandiseViewHolder(var marchandiseRowsLayoutBinding: MarchandiseRowsLayoutBinding)
        : RecyclerView.ViewHolder(marchandiseRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarchandiseViewHolder {
        return MarchandiseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.marchandise_rows_layout,parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: MarchandiseViewHolder, position: Int) {
        val data = list[position]
        holder.marchandiseRowsLayoutBinding.model = data
        holder.marchandiseRowsLayoutBinding.listener = cartListener
    }

    override fun getItemCount(): Int {
       return list.size
    }
}