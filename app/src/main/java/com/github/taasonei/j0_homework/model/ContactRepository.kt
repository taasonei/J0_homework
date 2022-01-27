package com.github.taasonei.j0_homework.model

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class ContactRepository {
    private val contactUri = ContactsContract.Contacts.CONTENT_URI
    private val contactFilterUri = ContactsContract.Contacts.CONTENT_FILTER_URI
    private val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    private val emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
    private val dataUri = ContactsContract.Data.CONTENT_URI

    private val orderByNameASC = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

    private val contactProjection: Array<String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.HAS_PHONE_NUMBER,
        ContactsContract.Contacts.PHOTO_URI
    )
    private val phoneProjection: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    private val emailProjection: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.Email.ADDRESS
    )
    private val birthdayProjection: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.Event.START_DATE,
    )
    private val descriptionProjection: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.Note.NOTE
    )

    private val contactWithNumberSelection = "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = ?"
    private val currentContactSelection =
        "$contactWithNumberSelection AND ${ContactsContract.Contacts._ID} = ?"
    private val phoneSelection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
    private val emailSelection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
    private val birthdaySelection = "${ContactsContract.Data.CONTACT_ID} = ? AND " +
            "${ContactsContract.Data.MIMETYPE} = ? AND " +
            "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
    private val descriptionSelection = "${ContactsContract.Data.CONTACT_ID} = ? AND " +
            "${ContactsContract.CommonDataKinds.Note.MIMETYPE} = ?"

    fun getContacts(context: Context, searchString: String?): Flow<List<ShortContact>> = flow {
        val contactList = mutableListOf<ShortContact>()
        val contentUri: Uri =
            if (searchString.isNullOrEmpty()) contactUri else Uri.withAppendedPath(
                contactFilterUri,
                Uri.encode(searchString)
            )
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                contentUri,
                contactProjection,
                contactWithNumberSelection,
                arrayOf("1"),
                orderByNameASC
            )
            if (cursor != null && cursor.moveToFirst()) {
                val indexId = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val indexName =
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val indexPhoto = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                do {
                    val id = cursor.getString(indexId)
                    val name = cursor.getString(indexName)
                    val photo = cursor.getStringOrNull(indexPhoto) ?: ""
                    val phone = getPhoneNumbers(context, id).first()
                    contactList.add(
                        ShortContact(
                            id = id,
                            name = name,
                            phone = phone,
                            photo = photo,
                        )
                    )
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }
        emit(contactList)
    }

    fun getContactById(context: Context, id: String): Flow<DetailedContact?> = flow {
        var contact: DetailedContact? = null
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                contactUri,
                contactProjection,
                currentContactSelection,
                arrayOf("1", id),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val indexName =
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val indexPhoto = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                val name = cursor.getString(indexName)
                val photo = cursor.getStringOrNull(indexPhoto) ?: ""
                val phone = getPhoneNumbers(context, id)
                val email = getEmails(context, id)
                val birthday = getBirthday(context, id)
                val description = getDescription(context, id)
                contact = DetailedContact(
                    id = id,
                    name = name,
                    photo = photo,
                    birthday = birthday,
                    phone = phone,
                    email = email,
                    description = description
                )
            }
        } finally {
            cursor?.close()
        }
        emit(contact)
    }

    private fun getPhoneNumbers(context: Context, id: String): List<String> {
        val phones = mutableListOf<String>()
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                phoneUri,
                phoneProjection,
                phoneSelection,
                arrayOf(id),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    phones.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }
        return phones
    }

    private fun getEmails(context: Context, id: String): List<String> {
        val emails = mutableListOf<String>()
        var cursor: Cursor? = null
        try {
            cursor =
                context.contentResolver.query(
                    emailUri,
                    emailProjection,
                    emailSelection,
                    arrayOf(id),
                    null
                )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val email = cursor.getStringOrNull(0)
                    if (!email.isNullOrEmpty()) {
                        emails.add(email)
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }
        return emails
    }

    private fun getBirthday(context: Context, id: String): LocalDate? {
        var birthday: String? = null
        val selectionArgs = arrayOf(
            id,
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString(),
        )
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                dataUri,
                birthdayProjection,
                birthdaySelection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                birthday = cursor.getStringOrNull(0)
            }
        } finally {
            cursor?.close()
        }
        return if (birthday.isNullOrEmpty()) {
            null
        } else {
            LocalDate.parse(birthday)
        }
    }

    private fun getDescription(context: Context, id: String): String {
        var description = ""
        val selectionArgs = arrayOf(
            id,
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
        )
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                dataUri,
                descriptionProjection,
                descriptionSelection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                description = cursor.getStringOrNull(0) ?: ""
            }
        } finally {
            cursor?.close()
        }
        return description
    }

}
