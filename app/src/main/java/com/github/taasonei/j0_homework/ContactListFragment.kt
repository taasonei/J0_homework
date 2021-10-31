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
import androidx.navigation.fragment.findNavController
import com.github.taasonei.j0_homework.databinding.FragmentContactListBinding
import com.github.taasonei.j0_homework.model.Contact
import com.github.taasonei.j0_homework.service.ContactService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private var contactId: Int? = null

    private var contactService: ContactService? = null
    private var contactList: List<Contact>? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ContactService.ContactBinder
            contactService = binder.getService()

            viewLifecycleOwner.lifecycleScope.launch {
                contactList = withContext(Dispatchers.IO) {
                    contactService?.getContactList()
                }
                setContactData(contactList)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            contactService = null
        }
    }

    companion object {
        const val CONTACT_ID_TAG = "CONTACT_ID"
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

        requireActivity().bindService(
            Intent(activity, ContactService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

        val cardView = binding.contactCardView
        cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(CONTACT_ID_TAG, contactId ?: -1)
            findNavController().navigate(
                R.id.action_contactListFragment_to_contactDetailsFragment,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        requireActivity().unbindService(connection)
    }

    private fun setContactData(contacts: List<Contact>?) {
        if (!contacts.isNullOrEmpty()) {
            hideProgressBar()
            contactId = contacts.indexOf(contacts.first())
            binding.contactItemName.text = contacts.first().name
            binding.contactItemPhone.text = contacts.first().phone.first()
            binding.contactItemPhoto.setImageResource(contacts.first().photo)
        }
    }

    private fun hideProgressBar() {
        binding.contactListProgressBar.visibility = View.GONE
        binding.contactCardView.visibility = View.VISIBLE
    }

}
