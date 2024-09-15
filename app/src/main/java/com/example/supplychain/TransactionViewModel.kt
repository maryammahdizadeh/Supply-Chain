package com.example.supplychain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supplychainapp.DatabaseInstance
import com.example.supplychainapp.Transaction
import com.example.supplychainapp.User
import com.example.supplychainapp.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao = DatabaseInstance.getDatabase(application).transactionDao()
    private val repository: TransactionRepository = TransactionRepository(transactionDao)


    // Insert users into the database
    fun insertTransactions(transactions: List<Transaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransactions(transactions)
        }
    }

    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(transaction)
        }
    }

    // Delete all users from the database
    fun deleteAllTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTransactions()
        }
    }


    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun getTransactionsForUser(userId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = repository.getTransactionsForUser(userId)
            withContext(Dispatchers.Main) {
                _transactions.value = transactions
            }
        }
    }

    private val _selectedTransaction = MutableLiveData<Transaction?>()
    val selectedTransaction: LiveData<Transaction?> = _selectedTransaction

    fun getTransactionById(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = repository.getTransactionById(transactionId)
            withContext(Dispatchers.Main) {
                _selectedTransaction.value = transaction
            }
        }
    }

    // Update transaction approval status
    fun updateUserApproval(transactionId: Int, firstUserApproved: Boolean? = null, secondUserApproved: Boolean? = null) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId)
            transaction?.let {
                if (firstUserApproved != null) {
                    it.firstUserApproved = firstUserApproved
                }
                if (secondUserApproved != null) {
                    it.secondUserApproved = secondUserApproved
                }
                // Check if both users have approved the transaction
                if (it.firstUserApproved && it.secondUserApproved) {
                    it.status = "Approved"
                }
                repository.updateTransaction(it) // Update the transaction in the database
            }
        }
    }

    fun updateUserFinalApproval(transactionId: Int, firstUserApproved: Boolean? = null, secondUserApproved: Boolean? = null) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId)
            transaction?.let {
                // Check if both users have approved the transaction
                if (it.firstUserApproved && it.secondUserApproved) {
                    it.status = "Approved"
                }
                repository.updateTransaction(it) // Update the transaction in the database
            }
        }
    }



    // Reject the transaction
    fun rejectTransaction(transactionId: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId)
            transaction?.let {
                // Set the transaction status to Rejected
                it.status = "Rejected"

                // Update the transaction in the database
                repository.updateTransaction(it)
            }
        }
    }


}