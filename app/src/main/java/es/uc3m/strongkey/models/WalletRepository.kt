package es.uc3m.strongkey.models

import androidx.lifecycle.LiveData

class WalletRepository(private val walletdao: WalletDAO) {
    val readAll: LiveData<List<Wallet>> = walletdao.readAll()

    suspend fun addWallet(wallet: Wallet){
        walletdao.addAESFile(wallet)
    }

    suspend fun removeWallet(clave: String){
        walletdao.deleteAESFile(clave)
    }
}