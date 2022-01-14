package com.github.taasonei.j0_homework.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.github.taasonei.j0_homework.ui.ContactListFragment
import com.github.taasonei.j0_homework.MainActivity
import com.github.taasonei.j0_homework.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_BIRTHDAY = "CHANNEL_BIRTHDAY"
        private const val NOTIFY_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == IntentUtils.SET_ALARM) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.channel_name_birthday)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(CHANNEL_BIRTHDAY, name, importance)
                notificationManager.createNotificationChannel(mChannel)
            }

            val name = intent.extras?.getString(IntentUtils.CONTACT_NAME)
            val contactId = intent.extras?.getString(ContactListFragment.CONTACT_ID_TAG)
            val bundle = Bundle()
            bundle.putString(ContactListFragment.CONTACT_ID_TAG, contactId)

            val pendingIntentFlag = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                else -> PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent: PendingIntent = if (!contactId.isNullOrEmpty()) {
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.contactDetailsFragment)
                    .setArguments(bundle)
                    .createPendingIntent()
            } else {
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                    pendingIntentFlag
                )
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_BIRTHDAY)
                .setSmallIcon(R.drawable.ic_baseline_cake_24)
                .setContentText(context.getString(R.string.birthday_notification, name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(contactId, NOTIFY_ID, builder.build())
        }
    }

}
