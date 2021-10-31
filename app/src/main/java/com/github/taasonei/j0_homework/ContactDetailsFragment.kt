package com.github.taasonei.j0_homework

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
import com.github.taasonei.j0_homework.service.ContactService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactDetailsFragment : Fragment() {

    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

    private var contactService: ContactService? = null
    private var contactId: Int? = null
    private var contact: Contact? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ContactService.ContactBinder
            contactService = binder.getService()

            viewLifecycleOwner.lifecycleScope.launch {
                contact = withContext(Dispatchers.IO) {
                    contactService?.getContactById(arguments?.getInt(ContactListFragment.CONTACT_ID_TAG))
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

        contactId = arguments?.getInt(ContactListFragment.CONTACT_ID_TAG)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().bindService(
            Intent(activity, ContactService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

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
            binding.contactDetailsDescription.text = contact.description
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
        binding.contactDetailsDescription.visibility = View.VISIBLE
    }

}
