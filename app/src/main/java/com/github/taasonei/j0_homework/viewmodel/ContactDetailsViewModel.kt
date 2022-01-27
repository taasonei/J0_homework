package com.github.taasonei.j0_homework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.taasonei.j0_homework.model.ContactRepository
import com.github.taasonei.j0_homework.model.DetailedContact
import com.github.taasonei.j0_homework.notification.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ContactDetailsViewModel(application: Application, val id: String) :
    AndroidViewModel(application) {
    private val contactRepository = ContactRepository()
    private val alarmRepository = AlarmRepository()

    private val _contact = MutableStateFlow<State<DetailedContact?>>(State.Loading())
    val contact: StateFlow<State<DetailedContact?>>
        get() = _contact

    fun loadContact() {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.getContactById(getApplication(), id)
                .catch { e -> _contact.value = State.Error(e.toString()) }
                .collect { _contact.value = State.Success(it) }
        }
    }

    fun setAlarm() {
        if (contact.value is State.Success<DetailedContact?>) {
            val contact = (contact.value as State.Success<DetailedContact?>).data
            if (contact != null) {
                alarmRepository.setAlarm(getApplication(), contact)
            }
        }
    }

    fun cancelAlarm() {
        if (contact.value is State.Success<DetailedContact?>) {
            val contact = (contact.value as State.Success<DetailedContact?>).data
            if (contact != null) {
                alarmRepository.cancelAlarm(getApplication(), contact)
            }
        }
    }
}