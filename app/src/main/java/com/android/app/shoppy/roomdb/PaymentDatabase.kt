package com.android.app.shoppy.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.app.shoppy.models.PaymentModel

@Database(entities = [RoomModel::class],version = 1,exportSchema = false)
abstract class PaymentDatabase : RoomDatabase(){

    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile private var instance: PaymentDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            PaymentDatabase::class.java, "todo-list.db")
            .build()
    }

}