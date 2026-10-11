package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CoinTransaction
import com.example.data.model.User
import com.example.data.model.Withdrawal
import com.example.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WalletUiState(
    val selectedMethod: String = "PayPal",
    val withdrawalCoinsInput: String = "10000000",
    val paymentDetailsInput: String = "",
    val conversionRate: Long = 1000000L,
    val minWithdrawalCoins: Long = 10000000L,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WalletRepository(db)
    val currentUserId = "user_default_1"

    val user: StateFlow<User?> = repository.getUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<CoinTransaction>> = repository.getUserTransactions(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawals: StateFlow<List<Withdrawal>> = repository.getUserWithdrawals(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val rate = repository.getConversionRate()
            val minCoins = repository.getMinWithdrawalCoins()
            _uiState.value = _uiState.value.copy(
                conversionRate = rate,
                minWithdrawalCoins = minCoins,
                withdrawalCoinsInput = minCoins.toString()
            )
        }
    }

    fun setMethod(method: String) {
        _uiState.value = _uiState.value.copy(selectedMethod = method)
    }

    fun setWithdrawalCoinsInput(coins: String) {
        _uiState.value = _uiState.value.copy(withdrawalCoinsInput = coins.filter { it.isDigit() })
    }

    fun setPaymentDetailsInput(details: String) {
        _uiState.value = _uiState.value.copy(paymentDetailsInput = details)
    }

    fun submitWithdrawal() {
        val state = _uiState.value
        val coins = state.withdrawalCoinsInput.toLongOrNull() ?: 0L

        if (!com.example.util.NetworkMonitor.getInstance(getApplication()).isCurrentlyOnline()) {
            _uiState.value = state.copy(errorMessage = "Active internet connection required to request withdrawals.")
            return
        }

        if (state.paymentDetailsInput.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter payment details / account address.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            val result = repository.requestWithdrawal(
                userId = currentUserId,
                coinsToWithdraw = coins,
                method = state.selectedMethod,
                paymentDetails = state.paymentDetailsInput.trim()
            )

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    successMessage = "Withdrawal request submitted! Status: PENDING review.",
                    paymentDetailsInput = ""
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Failed to submit withdrawal."
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
