package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth

class SignOutDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Are you sure?")
                .setIcon(R.drawable.ic_netology_48dp)
                .setCancelable(true)
                .setPositiveButton(R.string.no) { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton(
                    R.string.ok
                ) { _, _ ->
                    AppAuth.getInstance().clearAuth()
                    findNavController().navigate(R.id.action_signOutDialogFragment_to_feedFragment)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}