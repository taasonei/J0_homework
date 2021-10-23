package com.github.taasonei.j0_homework

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.taasonei.j0_homework.databinding.FragmentContactListBinding

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

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
            bundle.putInt(CONTACT_ID_TAG, 1)
            findNavController().navigate(R.id.action_contactListFragment_to_contactDetailsFragment, bundle)
        }
        binding.contactItemPhoto.setImageResource(R.drawable.example_avatar)
        binding.contactItemName.text = getString(R.string.example_name)
        binding.contactItemPhone.text = getString(R.string.example_phone)
    }

    companion object {
        const val CONTACT_ID_TAG = "CONTACT_ID"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
