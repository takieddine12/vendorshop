package com.android.app.shoppy.auth

import com.android.app.shoppy.models.OrderDetailsModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthResponse {

    @FormUrlEncoded
    @POST("insert.php")
    fun postOrder(
        @Field("orderStatus") orderStatus: String,
        @Field("totalCost") totalCost: String,
        @Field("OrderId") OrderId: String,
        @Field("allQuantity") allQuantity: String,
        @Field("orderTime") orderTime: String,
        @Field("customerUID") customerUid: String,
        @Field("country") country: String,
        @Field("city") city: String,
        @Field("address") address: String
    ) : Call<String>

}

