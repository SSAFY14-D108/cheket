package com.example.cheketqr.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cheketqr.data.network.NetworkModule
import com.example.cheketqr.data.repository.TicketRepositoryImpl
import com.example.cheketqr.domain.usecase.VerifyQrUseCase

object ScannerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            val apiService = NetworkModule.provideApiService()
            val repository = TicketRepositoryImpl(apiService)
            val useCase = VerifyQrUseCase(repository)
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
