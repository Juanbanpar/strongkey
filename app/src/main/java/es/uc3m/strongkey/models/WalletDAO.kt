package es.uc3m.strongkey.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WalletDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAESFile(wallet: Wallet)

    @Query("SELECT * FROM Wallet_table ORDER BY path ASC")
    fun readAll(): LiveData<List<Wallet>>

    @Query("DELETE FROM Wallet_table WHERE path = :path")
    fun deleteAESFile(path: String?)
}