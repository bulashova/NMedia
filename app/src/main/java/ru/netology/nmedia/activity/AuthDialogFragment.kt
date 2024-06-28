package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R

@AndroidEntryPoint
class AuthDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.sign_in_dialog)
                .setIcon(R.drawable.ic_netology_48dp)
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { _, _ ->
                    findNavController().navigate(R.id.action_authDialogFragment_to_authFragment)
                }
                .setNegativeButton(
                    R.string.no
                ) { _, _ ->
                    findNavController().navigateUp()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}