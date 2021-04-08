package es.uc3m.strongkey.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Wallet_table")
data class Wallet (
    @PrimaryKey
    var path: String
    )