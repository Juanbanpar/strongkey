package es.uc3m.strongkey.models

import androidx.lifecycle.LiveData

class AESFileRepository(private val aesFileDao: AESFileDao){
    val readAll: LiveData<List<AESFile>> = aesFileDao.readAll()

    suspend fun addAESFile(aesFile: AESFile){
        aesFileDao.addAESFile(aesFile)
    }
}