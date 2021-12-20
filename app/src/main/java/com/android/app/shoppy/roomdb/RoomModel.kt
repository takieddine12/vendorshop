package com.android.app.shoppy.roomdb

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.io.Serializable




@Entity(tableName = "payment")
class RoomModel(

     var itemTitle: String,
     var itemCategory: String,
     var itemPriceEach: String,
     var itemTotalPrice: String,
     var itemQuantity: String,
     var itemDeliveryFee: String,
     var country: String,
     var city: String,
     var address: String,
     var state: String,
     var sellerUid: String,
     var customerName: String


)  : Serializable {
    @PrimaryKey(autoGenerate = true)
    @NotNull
    var id : Int? = null;
}
