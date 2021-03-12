package es.uc3m.strongkey.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AESFile_table")
data class AESFile(
    @PrimaryKey
    val path: String,
    val uid: String,
    val extension: String,
    val password: String
)