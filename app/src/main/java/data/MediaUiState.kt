package com.example.arklauncher.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object MediaUiState {
    private val _isExpanded = mutableStateOf(false)
    val isExpanded: State<Boolean> get() = _isExpanded

    fun expand() { _isExpanded.value = true }
    fun collapse() { _isExpanded.value = false }
    fun toggle() { _isExpanded.value = !_isExpanded.value }
}