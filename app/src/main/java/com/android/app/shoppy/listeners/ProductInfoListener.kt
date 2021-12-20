package com.android.app.shoppy.listeners

import com.android.app.shoppy.models.ProductModel

interface ProductInfoListener {

    fun getProductInfo(model : ProductModel)
}