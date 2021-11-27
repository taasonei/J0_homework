package com.github.taasonei.j0_homework.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.github.taasonei.j0_homework.model.Contact
import com.github.taasonei.j0_homework.model.getContactsInfo
import kotlinx.coroutines.delay

class ContactService : Service() {

    private val binder = ContactBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class ContactBinder : Binder() {
        fun getService(): ContactService = this@ContactService
    }

    suspend fun getContactList(): List<Contact> {
        delay(2000L)
        return getContactsInfo()
    }

    suspend fun getContactById(id: Int?): Contact? {
        delay(2000L)
        return if (id != null) {
            getContactsInfo()[id]
        } else null
    }

}
