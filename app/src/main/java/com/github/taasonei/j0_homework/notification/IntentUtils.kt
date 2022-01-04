package com.github.taasonei.j0_homework.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.taasonei.j0_homework.ContactListFragment
import com.github.taasonei.j0_homework.model.DetailedContact

class IntentUtils {
    companion object {
        const val SET_ALARM = "SET_ALARM"
        const val CONTACT_NAME = "CONTACT_NAME"
    }

    private fun getIntent(context: Context, contact: DetailedContact, contactId: String): Intent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = SET_ALARM
        intent.putExtra(ContactListFragment.CONTACT_ID_TAG, contactId)
        intent.putExtra(CONTACT_NAME, contact.name)
        return intent
    }

    private fun getPendingIntentFlagUpdateCurrent(): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun getPendingIntentFlagNoCreate(): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            else -> PendingIntent.FLAG_NO_CREATE
        }
    }

    fun getPendingIntent(context: Context, contact: DetailedContact, contactId: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            contactId.toInt(),
            getIntent(context, contact, contactId),
            getPendingIntentFlagUpdateCurrent()
        )
    }

    fun isPendingIntentCreated(context: Context, contact: DetailedContact, contactId: String): Boolean {
        return PendingIntent.getBroadcast(
            context,
            contactId.toInt(),
            getIntent(context, contact, contactId),
            getPendingIntentFlagNoCreate()
        ) != null
    }
}
