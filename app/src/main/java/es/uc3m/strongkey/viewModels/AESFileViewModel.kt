package es.uc3m.strongkey.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import es.uc3m.strongkey.models.AESFile
import es.uc3m.strongkey.models.AESFileDatabase
import es.uc3m.strongkey.models.AESFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AESFileViewModel(application: Application): AndroidViewModel(application){
    val readAll: LiveData<List<AESFile>>
    private val repository: AESFileRepository

    init {
        val aesFileDao = AESFileDatabase.getDatabase(application).AESFileDao()
        repository = AESFileRepository(aesFileDao)
        readAll = repository.readAll
    }

    fun addAESFile(aesFile: AESFile){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAESFile(aesFile)
        }
    }
}