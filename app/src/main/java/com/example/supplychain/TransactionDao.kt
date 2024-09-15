package com.example.supplychainapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions WHERE firstUserId = :userId OR secondUserId = :userId")
    suspend fun getTransactionsForUser(userId: Int?): List<Transaction>

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Int): Transaction?

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions() : List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()









}