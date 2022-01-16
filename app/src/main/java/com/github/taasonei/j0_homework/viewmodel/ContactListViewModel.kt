package com.github.taasonei.j0_homework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.taasonei.j0_homework.model.ContactRepository
import com.github.taasonei.j0_homework.model.ShortContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactListViewModel(application: Application) : AndroidViewModel(application) {
    private val contactRepository = ContactRepository()

    private val _contacts: MutableLiveData<List<ShortContact>> =
        MutableLiveData<List<ShortContact>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                it.postValue(contactRepository.getAllContacts(application))
            }
        }

    val contacts: LiveData<List<ShortContact>>
        get() = _contacts
}
