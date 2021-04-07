package es.uc3m.strongkey.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AESFileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAESFile(aesFile: AESFile)

    @Query("SELECT * FROM AESFile_table ORDER BY path ASC")
    fun readAll(): LiveData<List<AESFile>>

    @Query("DELETE FROM AESFile_table WHERE path = :path")
    fun deleteAESFile(path: String?)
}