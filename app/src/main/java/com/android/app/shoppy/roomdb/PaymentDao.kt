package com.android.app.shoppy.roomdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payment ORDER BY id")
    fun getAllPayments() : LiveData<MutableList<RoomModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(roomModel: RoomModel)

    @Query("DELETE FROM payment")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM payment WHERE id = :position")
    suspend fun deleteOneItem(position : Int)
}