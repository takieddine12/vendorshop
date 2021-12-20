package com.android.app.shoppy.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.R
import com.android.app.shoppy.databinding.StoresRowsLayoutBinding
import com.android.app.shoppy.listeners.StoresListener
import com.android.app.shoppy.models.StoresModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StoresAdapter(var storesList : MutableList<StoresModel>,
                    var storesListener: StoresListener) : RecyclerView.Adapter<StoresAdapter.StoresViewHolder>() {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    inner class StoresViewHolder(var storesRowsLayoutBinding: StoresRowsLayoutBinding)
        : RecyclerView.ViewHolder(storesRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoresViewHolder {
        return StoresViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.stores_rows_layout,parent,false
                )
        )
    }

    override fun onBindViewHolder(holder: StoresViewHolder, position: Int) {
        val data = storesList[position]
        holder.storesRowsLayoutBinding.model = data
        holder.storesRowsLayoutBinding.listener = storesListener

        if(data.profileImage == ""){
            holder.storesRowsLayoutBinding.imageView2.setImageResource(R.drawable.ic_store)
        }

        firebaseDatabase
            .reference
            .child("Accounts")
            .child("Sellers")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(ds in snapshot.children){
                            val status = ds.child("status").value.toString()
                            if(status == "open"){
                                holder.storesRowsLayoutBinding.grayStatusIcon.visibility = View.GONE
                                holder.storesRowsLayoutBinding.greenStatusIcon.visibility = View.VISIBLE

                            } else {
                                holder.storesRowsLayoutBinding.grayStatusIcon.visibility = View.VISIBLE
                                holder.storesRowsLayoutBinding.greenStatusIcon.visibility = View.GONE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return storesList.size
    }


    override fun onViewDetachedFromWindow(holder: StoresViewHolder) {
        super.onViewDetachedFromWindow(holder)
    }
}