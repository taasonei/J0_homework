package com.github.taasonei.j0_homework.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.github.taasonei.j0_homework.R

class ContactPermissionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.read_contacts_permission_message))
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.settings_button)
            ) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireActivity().packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton(
                getString(R.string.exit_button)
            ) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finishAndRemoveTask()
            }
            .create()
    }

    companion object {
        const val TAG = "ContactPermissionDialog"
    }
}