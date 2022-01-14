package com.github.taasonei.j0_homework.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.taasonei.j0_homework.R
import com.github.taasonei.j0_homework.databinding.FragmentContactDetailsBinding
import com.github.taasonei.j0_homework.model.DetailedContact
import com.github.taasonei.j0_homework.notification.IntentUtils
import com.github.taasonei.j0_homework.viewmodel.ContactDetailsViewModel
import com.github.taasonei.j0_homework.viewmodel.ModelFactory
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ContactDetailsFragment : Fragment() {
    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

    private val contactId: String by lazy {
        requireArguments().getString(ContactListFragment.CONTACT_ID_TAG)!!
    }

    private val viewModel by lazy {
        ViewModelProvider(this, ModelFactory(requireActivity().application, contactId)).get(
            ContactDetailsViewModel::class.java
        )
    }

    private val intentUtils = IntentUtils()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startObservingContact() else ContactPermissionDialog().show(
            childFragmentManager,
            ContactPermissionDialog.TAG
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        checkPermissionGranted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startObservingContact() {
        viewModel.contact.observe(viewLifecycleOwner, { value -> setContactData(value) })
        val switch = binding.contactDetailsBirthdayReminder
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setAlarm()
            } else {
                viewModel.cancelAlarm()
            }
        }
    }

    private fun setContactData(contact: DetailedContact?) {
        if (contact != null) {
            hideProgressBar()
            binding.apply {
                contactDetailsName.text = contact.name

                val phone = contact.phone
                contactDetailsPhone1.text = if (phone.isNotEmpty()) phone.first() else ""
                contactDetailsPhone2.text =
                    if (phone.isNotEmpty() && phone.size > 1) phone.last() else ""

                val email = contact.email
                contactDetailsEmail1.text = if (email.isNotEmpty()) email.first() else ""
                contactDetailsEmail2.text =
                    if (email.isNotEmpty() && email.size > 1) email.last() else ""

                if (contact.photo.isNotBlank()) {
                    contactDetailsPhoto.setImageURI(Uri.parse(contact.photo))
                } else {
                    contactDetailsPhoto.setImageResource(R.drawable.example_avatar)
                }

                contactDetailsDescription.text = contact.description

                if (contact.birthday != null) {
                    contactDetailsBirthday.text =
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(contact.birthday)
                    contactDetailsBirthdayReminder.isEnabled = true
                } else {
                    contactDetailsBirthday.text = ""
                    contactDetailsBirthdayReminder.isEnabled = false
                }

                contactDetailsBirthdayReminder.isChecked =
                    intentUtils.isPendingIntentCreated(requireContext(), contact)
            }
        }
    }

    private fun hideProgressBar() {
        binding.apply {
            contactDetailsProgressBar.visibility = View.GONE
            contactDetailsName.visibility = View.VISIBLE
            contactDetailsPhoto.visibility = View.VISIBLE
            contactDetailsPhone1.visibility = View.VISIBLE
            contactDetailsPhone2.visibility = View.VISIBLE
            contactDetailsEmail1.visibility = View.VISIBLE
            contactDetailsEmail2.visibility = View.VISIBLE
            contactDetailsBirthday.visibility = View.VISIBLE
            contactDetailsBirthdayReminder.visibility = View.VISIBLE
            contactDetailsDescription.visibility = View.VISIBLE
        }
    }

    private fun checkPermissionGranted() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> startObservingContact()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_CONTACTS
            ) -> ContactPermissionDialog().show(childFragmentManager, ContactPermissionDialog.TAG)
            else -> requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

}
