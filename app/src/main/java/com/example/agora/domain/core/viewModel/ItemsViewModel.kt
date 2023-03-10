package com.example.agora.domain.core.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.model.Response
import com.example.agora.domain.core.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ItemsViewModel"
@HiltViewModel
class ItemsViewModel @Inject constructor(private val useCases: UseCases) : ViewModel() {

    private val _items = MutableLiveData<List<ItemsWithReference>>()
    val items = _items as LiveData<List<ItemsWithReference>>

    fun getItems() = viewModelScope.launch {
        useCases.getItems.invoke().collect { response ->
            Log.d(TAG, "getItems: $response")
            when (response) {
                is Response.Loading -> {
                    // handle loading state
                }
                is Response.Success -> {
                    val data = response.data
                    // do something with the data
                    _items.postValue(data)
                }
                is Response.Failure -> {
                    val exception = response.e
                    // handle the failure case
                    Log.d(TAG, "getItems: Exception ${exception.toString()}")
                }
            }
        }
    }

}