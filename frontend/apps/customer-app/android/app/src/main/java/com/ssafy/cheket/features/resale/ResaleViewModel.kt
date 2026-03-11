package com.ssafy.cheket.features.resale

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.repository.ResaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResaleUiState(
    val groupedItems: List<ResaleGroupItem> = emptyList(),
    val isLoading: Boolean = true,
)

class ResaleViewModel(private val resaleRepository: ResaleRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ResaleUiState())
    val uiState: StateFlow<ResaleUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init — loading resale grouped items")
        viewModelScope.launch {
            resaleRepository.getResaleGrouped().collect { items ->
                Log.d(TAG, "getResaleGrouped() received ${items.size} groups")
                _uiState.value = ResaleUiState(groupedItems = items, isLoading = false)
            }
        }
    }

    companion object {
        private const val TAG = "ResaleViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ResaleViewModel(app.appContainer.resaleRepository)
            }
        }
    }
}
