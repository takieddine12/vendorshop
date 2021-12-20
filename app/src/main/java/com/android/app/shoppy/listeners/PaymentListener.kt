package com.android.app.shoppy.listeners

import com.android.app.shoppy.models.PaymentModel
import com.android.app.shoppy.roomdb.RoomModel

interface PaymentListener {
    fun onItemRemoved(model : RoomModel,position : Int)
}