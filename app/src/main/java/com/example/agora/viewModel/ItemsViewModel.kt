package com.example.agora.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.model.Item
import com.example.agora.model.Response
import com.example.agora.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(private val useCases: UseCases) : ViewModel() {

    companion object {
        private const val TAG = "ItemsViewModel"
    }

    private val _items = MutableLiveData<List<Item>>()
    val items = _items as LiveData<List<Item>>

    //    private val firebaseRepo = ItemRepository()
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

    fun sellItem(item: Item) = viewModelScope.launch {
        useCases.sellItem(item)
    }

}