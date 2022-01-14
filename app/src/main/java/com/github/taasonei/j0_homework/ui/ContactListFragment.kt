package com.github.taasonei.j0_homework.ui

import android.Manifest.permission.READ_CONTACTS
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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.taasonei.j0_homework.R
import com.github.taasonei.j0_homework.databinding.FragmentContactListBinding
import com.github.taasonei.j0_homework.model.ShortContact
import com.github.taasonei.j0_homework.viewmodel.ContactListViewModel

class ContactListFragment : Fragment() {
    companion object {
        const val CONTACT_ID_TAG = "CONTACT_ID"
    }

    private val viewModel by viewModels<ContactListViewModel>()

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private var contactId: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startObservingContacts() else ContactPermissionDialog().show(
            childFragmentManager,
            ContactPermissionDialog.TAG
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardView = binding.contactCardView
        cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CONTACT_ID_TAG, contactId)
            findNavController().navigate(
                R.id.action_contactListFragment_to_contactDetailsFragment,
                bundle
            )
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissionGranted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startObservingContacts() {
        viewModel.contacts.observe(viewLifecycleOwner, { list -> setContactData(list) })
    }

    private fun setContactData(contacts: List<ShortContact>?) {
        if (!contacts.isNullOrEmpty()) {
            hideProgressBar()
            val firstContact = contacts.first()
            contactId = firstContact.id
            binding.apply {
                contactItemName.text = firstContact.name
                contactItemPhone.text = firstContact.phone
                if (firstContact.photo.isNotBlank()) {
                    contactItemPhoto.setImageURI(Uri.parse(firstContact.photo))
                } else {
                    contactItemPhoto.setImageResource(R.drawable.example_avatar)
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.apply {
            contactListProgressBar.visibility = View.GONE
            contactCardView.visibility = View.VISIBLE
        }
    }

    private fun checkPermissionGranted() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> startObservingContacts()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                READ_CONTACTS
            ) -> ContactPermissionDialog().show(childFragmentManager, ContactPermissionDialog.TAG)
            else -> requestPermissionLauncher.launch(READ_CONTACTS)
        }
    }

}
