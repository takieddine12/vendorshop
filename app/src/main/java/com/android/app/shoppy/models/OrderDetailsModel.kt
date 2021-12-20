package com.android.app.shoppy.models

data class OrderDetailsModel(
    var productTitle : String = "",
    var productCategory : String  = "",
    var productDeliveryFee : String  = "",
    var productEachPrice : String = "",
    var productQuantity : String = ""
)