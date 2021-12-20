package com.android.app.shoppy.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso

object ImagesBinding {

    @BindingAdapter("productImg")
    @JvmStatic
    fun getImageUrl(view : ImageView , link  : String?){
        if(link != ""){
            Picasso.get().load(link).into(view)
        }
    }
}