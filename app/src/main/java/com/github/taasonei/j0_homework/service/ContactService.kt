package com.github.taasonei.j0_homework.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.github.taasonei.j0_homework.model.ContactHelper
import com.github.taasonei.j0_homework.model.DetailedContact
import com.github.taasonei.j0_homework.model.ShortContact

class ContactService : Service() {

    private val binder = ContactBinder()
    private val contactHelper = ContactHelper()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class ContactBinder : Binder() {
        fun getService(): ContactService = this@ContactService
    }

    fun getContactList(): List<ShortContact> {
        return contactHelper.getAllContacts(applicationContext)
    }

    fun getContactById(id: String?): DetailedContact? {
        return if (id != null) {
            contactHelper.getContactById(applicationContext, id)
        } else null
    }
}
