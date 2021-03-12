package es.uc3m.strongkey.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AESFile::class], version = 1, exportSchema = false)
abstract class AESFileDatabase: RoomDatabase() {
    abstract fun AESFileDao(): AESFileDao

    companion object{

        @Volatile
        private var INSTANCE: AESFileDatabase? = null

        fun getDatabase(context: Context): AESFileDatabase{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AESFileDatabase::class.java,
                        "AESFile_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }

    }

}