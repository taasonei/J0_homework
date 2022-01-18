package com.github.taasonei.j0_homework.adapter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.taasonei.j0_homework.R
import com.github.taasonei.j0_homework.databinding.ContactListItemBinding
import com.github.taasonei.j0_homework.model.ShortContact
import com.github.taasonei.j0_homework.ui.ContactListFragment

class ContactAdapter :
    ListAdapter<ShortContact, ContactAdapter.ContactViewHolder>(ContactListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            ContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener { view ->
            val bundle = Bundle()
            bundle.putString(ContactListFragment.CONTACT_ID_TAG, getItem(position).id)
            view.findNavController().navigate(
                R.id.action_contactListFragment_to_contactDetailsFragment,
                bundle
            )
        }
    }

    class ContactViewHolder(private val binding: ContactListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: ShortContact) {
            binding.apply {
                contactItemName.text = contact.name
                contactItemPhone.text = contact.phone
                if (contact.photo.isNotBlank()) {
                    contactItemPhoto.setImageURI(Uri.parse(contact.photo))
                } else {
                    contactItemPhoto.setImageResource(R.drawable.example_avatar)
                }
            }
        }
    }

}
