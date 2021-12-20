package com.android.app.shoppy.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class OrderModel(
        var orderId : String = "",
        val address : String = "" ,
        val city : String  = "" ,
        val country : String  = "" ,
        var orderStatus : String = "",
        val Quantity : String =  "",
        var orderTime  : String = "",
        var customerUid : String = "",
        var totalPrice : String = ""

)
