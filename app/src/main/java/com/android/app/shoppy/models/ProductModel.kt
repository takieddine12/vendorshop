package com.android.app.shoppy.models

import android.os.Parcel
import android.os.Parcelable

data class ProductModel(
        val productPrice : String ,
        val productName : String ,
        val productDiscount : String ,
        val productDeliveryFee : String ,
        val productID : String ,
        val productSavedTime : String ,
        val shopStatus : String ,
        val productImage : String ,
        val productCategory : String ,
        val productDescription : String

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
                parcel.readString()!!
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(productPrice)
                parcel.writeString(productName)
                parcel.writeString(productDiscount)
                parcel.writeString(productDeliveryFee)
                parcel.writeString(productID)
                parcel.writeString(productSavedTime)
                parcel.writeString(shopStatus)
                parcel.writeString(productImage)
                parcel.writeString(productCategory)
                parcel.writeString(productDescription)
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
