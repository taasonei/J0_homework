package com.github.taasonei.j0_homework.notification

import android.app.AlarmManager
import android.content.Context
import com.github.taasonei.j0_homework.model.DetailedContact
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset

class AlarmRepository {
    companion object {
        private const val TWENTY_NINTH = 29
        private const val FOUR_YEARS = 4L
        private const val ONE_YEAR = 1L
    }

    private val intentUtils = IntentUtils()

    fun setAlarm(context: Context, contact: DetailedContact) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            intentUtils.getPendingIntent(context, contact)

        val currentDate = LocalDate.now()
        if (contact.birthday != null) {
            var birthdayDate = LocalDate.of(
                currentDate.year,
                contact.birthday.month,
                contact.birthday.dayOfMonth
            )
            if (birthdayDate.isBefore(currentDate)) {
                birthdayDate = when {
                    birthdayDate.month == Month.FEBRUARY && birthdayDate.dayOfMonth == TWENTY_NINTH -> {
                        when {
                            birthdayDate.isLeapYear -> birthdayDate.plusYears(FOUR_YEARS)
                            else -> birthdayDate.plusYears(FOUR_YEARS - birthdayDate.year % FOUR_YEARS)
                        }
                    }
                    else -> birthdayDate.plusYears(ONE_YEAR)
                }
            }

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                birthdayDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, contact: DetailedContact) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            intentUtils.getPendingIntent(context, contact)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
