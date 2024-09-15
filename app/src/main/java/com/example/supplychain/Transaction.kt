package com.example.supplychainapp

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstUserId: Int,
    val secondUserId: Int,
    val firstQRCode: String,
    val secondQRCode: String,
    var firstUserApproved: Boolean = false,
    var secondUserApproved: Boolean = false,
    var status: String = "Pending"
)
