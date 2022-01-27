package com.github.taasonei.j0_homework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.taasonei.j0_homework.model.ContactRepository
import com.github.taasonei.j0_homework.model.ShortContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ContactListViewModel(application: Application) : AndroidViewModel(application) {
    private val contactRepository = ContactRepository()

    private var _contacts = MutableStateFlow<State<List<ShortContact>>>(State.Loading())
    val contacts: StateFlow<State<List<ShortContact>>>
        get() = _contacts

    private val _searchViewQuery: MutableLiveData<String> = MutableLiveData("")
    val searchViewQuery: LiveData<String>
        get() = _searchViewQuery

    fun loadContacts(searchString: String?) {
        _searchViewQuery.postValue(searchString)
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.getContacts(getApplication(), searchString)
                .catch { e -> _contacts.value = State.Error(e.toString()) }
                .collect { _contacts.value = State.Success(it) }
        }
    }
}