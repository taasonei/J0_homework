package com.github.taasonei.j0_homework

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.github.taasonei.j0_homework.databinding.FragmentContactDetailsBinding
import com.github.taasonei.j0_homework.model.Contact
import com.github.taasonei.j0_homework.notification.IntentUtils
import com.github.taasonei.j0_homework.service.ContactService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ContactDetailsFragment : Fragment() {

    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

    private var contactService: ContactService? = null
    private val contactId: Int by lazy {
        requireArguments().getInt(ContactListFragment.CONTACT_ID_TAG)
    }
    private var contact: Contact? = null

    private val intentUtils = IntentUtils()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ContactService.ContactBinder
            contactService = binder.getService()

            viewLifecycleOwner.lifecycleScope.launch {
                contact = withContext(Dispatchers.IO) {
                    contactService?.getContactById(contactId)
                }
                setContactData(contact)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            contactService = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().bindService(
            Intent(activity, ContactService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

        val switch = binding.contactDetailsBirthdayReminder
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setAlarm()
            } else {
                cancelAlarm()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        requireActivity().unbindService(connection)
    }

    private fun setContactData(contact: Contact?) {
        if (contact != null) {
            hideProgressBar()
            binding.contactDetailsName.text = contact.name
            binding.contactDetailsPhoto.setImageResource(contact.photo)
            binding.contactDetailsPhone1.text = contact.phone.first()
            binding.contactDetailsPhone2.text = contact.phone.last()
            binding.contactDetailsEmail1.text = contact.email.first()
            binding.contactDetailsEmail2.text = contact.email.last()
            binding.contactDetailsBirthday.text =
                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(contact.birthday)
            binding.contactDetailsDescription.text = contact.description
            binding.contactDetailsBirthdayReminder.isChecked =
                intentUtils.isPendingIntentCreated(requireContext(), contact, contactId)
        }
    }

    private fun hideProgressBar() {
        binding.contactDetailsProgressBar.visibility = View.GONE
        binding.contactDetailsName.visibility = View.VISIBLE
        binding.contactDetailsPhoto.visibility = View.VISIBLE
        binding.contactDetailsPhone1.visibility = View.VISIBLE
        binding.contactDetailsPhone2.visibility = View.VISIBLE
        binding.contactDetailsEmail1.visibility = View.VISIBLE
        binding.contactDetailsEmail2.visibility = View.VISIBLE
        binding.contactDetailsBirthday.visibility = View.VISIBLE
        binding.contactDetailsBirthdayReminder.visibility = View.VISIBLE
        binding.contactDetailsDescription.visibility = View.VISIBLE
    }

    private fun setAlarm() {
        val contact = contact ?: return

        val alarmManager: AlarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            intentUtils.getPendingIntent(requireContext(), contact, contactId)

        val currentDate = LocalDate.now()
        var birthdayDate = LocalDate.of(
            currentDate.year,
            contact.birthday.month,
            contact.birthday.dayOfMonth
        )
        if (birthdayDate.isBefore(currentDate)) {
            birthdayDate = when {
                birthdayDate.month == Month.FEBRUARY && birthdayDate.dayOfMonth == 29 -> {
                    when {
                        birthdayDate.isLeapYear -> birthdayDate.plusYears(4)
                        else -> birthdayDate.plusYears(4L - birthdayDate.year % 4)
                    }
                }
                else -> birthdayDate.plusYears(1)
            }
        }

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            birthdayDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            pendingIntent
        )
    }

    private fun cancelAlarm() {
        val contact = contact ?: return

        val alarmManager: AlarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            intentUtils.getPendingIntent(requireContext(), contact, contactId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

}
