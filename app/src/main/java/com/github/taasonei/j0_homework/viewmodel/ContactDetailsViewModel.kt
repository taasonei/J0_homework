package com.github.taasonei.j0_homework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.taasonei.j0_homework.model.ContactRepository
import com.github.taasonei.j0_homework.model.DetailedContact
import com.github.taasonei.j0_homework.notification.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactDetailsViewModel(application: Application, id: String) :
    AndroidViewModel(application) {
    private val contactRepository = ContactRepository()
    private val alarmRepository = AlarmRepository()

    private val _contact: MutableLiveData<DetailedContact> =
        MutableLiveData<DetailedContact>().also {
            viewModelScope.launch(Dispatchers.IO) {
                it.postValue(contactRepository.getContactById(application, id))
            }
        }

    val contact: LiveData<DetailedContact>
        get() = _contact

    fun setAlarm() {
        val contact = contact.value
        if (contact != null) {
            alarmRepository.setAlarm(getApplication(), contact)
        }
    }

    fun cancelAlarm() {
        val contact = contact.value
        if (contact != null) {
            alarmRepository.cancelAlarm(getApplication(), contact)
        }
    }
}