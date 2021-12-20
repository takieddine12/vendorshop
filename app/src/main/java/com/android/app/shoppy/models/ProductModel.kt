package com.android.app.shoppy.models

import android.os.Parcel
import android.os.Parcelable

data class ProductModel(
        val productCategory : String = "" ,
        val productDeliveryFee : String = "" ,
        val productDescription : String = "" ,
        val productDiscount : String = "" ,
        val productID : String = "" ,
        val productImage : String = "" ,
        val productName : String = "" ,
        val productPrice : String = "" ,
        val productSavedTime : String = "" ,
        val sellerUid : String = "" ,
        val shopStatus :  String = ""

) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(productCategory)
                parcel.writeString(productDeliveryFee)
                parcel.writeString(productDescription)
                parcel.writeString(productDiscount)
                parcel.writeString(productID)
                parcel.writeString(productImage)
                parcel.writeString(productName)
                parcel.writeString(productPrice)
                parcel.writeString(productSavedTime)
                parcel.writeString(sellerUid)
                parcel.writeString(shopStatus)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<ProductModel> {
                override fun createFromParcel(parcel: Parcel): ProductModel {
                        return ProductModel(parcel)
                }

                override fun newArray(size: Int): Array<ProductModel?> {
                        return arrayOfNulls(size)
                }
        }

}
