package com.example.supplychain

import com.example.supplychainapp.Transaction
import com.example.supplychainapp.TransactionDao
import com.example.supplychainapp.User
import com.example.supplychainapp.UserDao

class TransactionRepository (private val transactionDao: TransactionDao){

    // Function to insert users
    suspend fun insertTransactions(transactions: List<Transaction>) {
        transactionDao.insertTransactions(transactions)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    // Function to delete all users
    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun getTransactionsForUser(userId: Int?): List<Transaction> {
       return transactionDao.getTransactionsForUser(userId)
    }

    suspend fun getTransactionById(transactionId: Int): Transaction? {
        return transactionDao.getTransactionById(transactionId)
    }


    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }
}