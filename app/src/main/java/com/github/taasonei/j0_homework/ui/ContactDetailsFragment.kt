package com.github.taasonei.j0_homework.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.taasonei.j0_homework.R
import com.github.taasonei.j0_homework.databinding.FragmentContactDetailsBinding
import com.github.taasonei.j0_homework.model.DetailedContact
import com.github.taasonei.j0_homework.notification.IntentUtils
import com.github.taasonei.j0_homework.viewmodel.ContactDetailsViewModel
import com.github.taasonei.j0_homework.viewmodel.ModelFactory
import com.github.taasonei.j0_homework.viewmodel.State
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contactDetailsReload.setOnClickListener { startObservingContact() }
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
        viewModel.loadContact()
        viewModel.contact
            .onEach { state ->
                when (state) {
                    is State.Success<DetailedContact?> -> setContactData(state.data)
                    is State.Error -> {
                        hideProgressBar()
                        binding.contactDetailsReload.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is State.Loading -> showProgressBar()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

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
            contactDetailsReload.visibility = View.GONE
        }
    }

    private fun showProgressBar() {
        binding.apply {
            contactDetailsProgressBar.visibility = View.VISIBLE
            contactDetailsName.visibility = View.GONE
            contactDetailsPhoto.visibility = View.GONE
            contactDetailsPhone1.visibility = View.GONE
            contactDetailsPhone2.visibility = View.GONE
            contactDetailsEmail1.visibility = View.GONE
            contactDetailsEmail2.visibility = View.GONE
            contactDetailsBirthday.visibility = View.GONE
            contactDetailsBirthdayReminder.visibility = View.GONE
            contactDetailsDescription.visibility = View.GONE
            contactDetailsReload.visibility = View.GONE
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
