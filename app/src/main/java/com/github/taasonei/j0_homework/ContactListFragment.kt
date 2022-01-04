package com.github.taasonei.j0_homework

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.taasonei.j0_homework.databinding.FragmentContactListBinding
import com.github.taasonei.j0_homework.model.ShortContact
import com.github.taasonei.j0_homework.service.ContactService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager

class ContactListFragment : Fragment() {

    companion object {
        const val CONTACT_ID_TAG = "CONTACT_ID"
    }

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private var contactId: String? = null
    private val defaultPhoto = R.drawable.example_avatar

    private var contactService: ContactService? = null
    private var contactList: List<ShortContact>? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            bindService()
        } else {
            showAlertDialog()
        }
    }

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
        if (contactService != null) {
            requireActivity().unbindService(connection)
        }
    }

    private fun setContactData(contacts: List<ShortContact>?) {
        if (!contacts.isNullOrEmpty()) {
            hideProgressBar()
            contactId = contacts.first().id
            binding.contactItemName.text = contacts.first().name
            binding.contactItemPhone.text = contacts.first().phone
            if (contacts.first().photo.isNotBlank()) {
                binding.contactItemPhoto.setImageURI(Uri.parse(contacts.first().photo))
            } else {
                binding.contactItemPhoto.setImageResource(defaultPhoto)
            }
        }
    }

    private fun hideProgressBar() {
        binding.contactListProgressBar.visibility = View.GONE
        binding.contactCardView.visibility = View.VISIBLE
    }

    private fun bindService() {
        requireActivity().bindService(
            Intent(activity, ContactService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun checkPermissionGranted() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> bindService()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                READ_CONTACTS
            ) -> showAlertDialog()
            else -> requestPermissionLauncher.launch(READ_CONTACTS)
        }
    }

    private fun showAlertDialog() {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(requireContext())
            builder.apply {
                setMessage(getString(R.string.read_contacts_permission_message))
                setCancelable(false)
                setPositiveButton(
                    getString(R.string.settings_button)
                ) { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireActivity().packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                setNegativeButton(
                    getString(R.string.exit_button)
                ) { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finishAndRemoveTask()
                }
            }
            builder.create()
        }
        alertDialog.show()
    }

}
