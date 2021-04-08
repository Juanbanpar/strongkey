package es.uc3m.strongkey.ui.wallet

import android.app.Application
import androidx.lifecycle.*
import es.uc3m.strongkey.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    val readAll: LiveData<List<Wallet>>
    private val repository: WalletRepository
    init {
        val wallefiledao = WalletDatabase.getDatabase(application).WalletDAO()
        repository = WalletRepository(wallefiledao)
        readAll = repository.readAll
    }

    fun addFile(fichero: Wallet){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addWallet(fichero)
        }
    }

    fun removeFile(clave: String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeWallet(clave)
        }
    }
}