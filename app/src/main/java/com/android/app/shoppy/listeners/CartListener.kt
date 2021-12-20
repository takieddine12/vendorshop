package com.android.app.shoppy.listeners

import com.android.app.shoppy.models.ProductModel

interface CartListener {

    fun cartItemOnClick(model : ProductModel)
}