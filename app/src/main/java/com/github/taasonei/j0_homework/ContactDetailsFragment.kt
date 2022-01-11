package com.github.taasonei.j0_homework

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.github.taasonei.j0_homework.databinding.FragmentContactDetailsBinding
import com.github.taasonei.j0_homework.model.DetailedContact
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

    companion object {
        private const val TWENTY_NINTH = 29
        private const val FOUR_YEARS = 4L
        private const val ONE_YEAR = 1L
    }

    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

    private var contactService: ContactService? = null
    private val contactId: String by lazy {
        requireArguments().getString(ContactListFragment.CONTACT_ID_TAG)!!
    }

    private var contact: DetailedContact? = null

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

    private fun setContactData(contact: DetailedContact?) {
        if (contact != null) {
            hideProgressBar()
            binding.contactDetailsName.text = contact.name

            val phone = contact.phone
            binding.contactDetailsPhone1.text = if (phone.isNotEmpty()) phone.first() else ""
            binding.contactDetailsPhone2.text =
                if (phone.isNotEmpty() && phone.size > 1) phone.last() else ""

            val email = contact.email
            binding.contactDetailsEmail1.text = if (email.isNotEmpty()) email.first() else ""
            binding.contactDetailsEmail2.text =
                if (email.isNotEmpty() && email.size > 1) email.last() else ""

            if (contact.photo.isNotBlank()) {
                binding.contactDetailsPhoto.setImageURI(Uri.parse(contact.photo))
            } else {
                binding.contactDetailsPhoto.setImageResource(R.drawable.example_avatar)
            }

            binding.contactDetailsDescription.text = contact.description

            if (contact.birthday != null) {
                binding.contactDetailsBirthday.text =
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(contact.birthday)
                binding.contactDetailsBirthdayReminder.isEnabled = true
            } else {
                binding.contactDetailsBirthday.text = ""
                binding.contactDetailsBirthdayReminder.isEnabled = false
            }

            binding.contactDetailsBirthdayReminder.isChecked =
                intentUtils.isPendingIntentCreated(requireContext(), contact)
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
            intentUtils.getPendingIntent(requireContext(), contact)

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

    private fun cancelAlarm() {
        val contact = contact ?: return

        val alarmManager: AlarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            intentUtils.getPendingIntent(requireContext(), contact)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

}
