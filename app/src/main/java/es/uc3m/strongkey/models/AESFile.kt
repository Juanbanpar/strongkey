package es.uc3m.strongkey.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AESFile_table")
data class AESFile(
    @PrimaryKey
    var path: String,
    var uid: String,
    var extension: String,
    var password: String
)