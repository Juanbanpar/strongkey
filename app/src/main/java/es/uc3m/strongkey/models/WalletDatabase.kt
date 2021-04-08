package es.uc3m.strongkey.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Wallet::class], version = 1, exportSchema = false)
abstract class WalletDatabase: RoomDatabase() {
    abstract fun WalletDAO(): WalletDAO

    companion object{

        @Volatile
        private var INSTANCE: WalletDatabase? = null

        fun getDatabase(context: Context): WalletDatabase{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WalletDatabase::class.java,
                        "Wallet_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }

    }
}