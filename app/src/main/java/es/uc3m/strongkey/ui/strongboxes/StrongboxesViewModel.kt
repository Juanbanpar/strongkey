package es.uc3m.strongkey.ui.strongboxes


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uc3m.strongkey.models.AESFile
import es.uc3m.strongkey.models.AESFileDatabase
import es.uc3m.strongkey.models.AESFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StrongboxesViewModel(application: Application) : AndroidViewModel(application) {
    val readAll: LiveData<List<AESFile>>
    private val repository: AESFileRepository
    init {
        val aesfiledao = AESFileDatabase.getDatabase(application).AESFileDao()
        repository = AESFileRepository(aesfiledao)
        readAll = repository.readAll
    }

    fun addFile(fichero: AESFile){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAESFile(fichero)
        }
    }

    fun removeFile(clave: String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeAESFile(clave)
        }
    }
}