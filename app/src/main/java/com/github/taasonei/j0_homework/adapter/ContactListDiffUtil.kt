package com.github.taasonei.j0_homework.adapter

import androidx.recyclerview.widget.DiffUtil
import com.github.taasonei.j0_homework.model.ShortContact

class ContactListDiffUtil : DiffUtil.ItemCallback<ShortContact>() {
    override fun areItemsTheSame(oldItem: ShortContact, newItem: ShortContact): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ShortContact, newItem: ShortContact): Boolean {
        return oldItem.name == newItem.name && oldItem.phone == newItem.phone
    }

}