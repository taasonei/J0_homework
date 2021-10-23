package com.github.taasonei.j0_homework

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.taasonei.j0_homework.databinding.FragmentContactDetailsBinding

class ContactDetailsFragment : Fragment() {

    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

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
        binding.contactDetailsName.text = getString(R.string.example_name)
        binding.contactDetailsPhoto.setImageResource(R.drawable.example_avatar)
        binding.contactDetailsPhone1.text = getString(R.string.example_phone)
        binding.contactDetailsPhone2.text = getString(R.string.example_phone)
        binding.contactDetailsEmail1.text = getString(R.string.example_email)
        binding.contactDetailsEmail2.text = getString(R.string.example_email)
        binding.contactDetailsDescription.text = getString(R.string.example_long_description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
