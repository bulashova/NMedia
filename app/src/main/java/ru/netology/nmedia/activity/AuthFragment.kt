package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.SignInViewModel

class AuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModel by activityViewModels<SignInViewModel>()
        val binding = FragmentAuthBinding.inflate(layoutInflater, container, false)

        with(binding) {
            login.setText("student")
            pass.setText("secret")
            enter.setOnClickListener {
                val login = requireNotNull(login.text).toString()
                val pass = requireNotNull(pass.text).toString()
                viewModel.updateUser(login, pass)
            }
            back.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        findNavController().navigateUp()
                    }
                    .show()
            }
        }

        viewModel.auth.observe(viewLifecycleOwner) {
            if (viewModel.authenticated)
                findNavController().navigateUp()
        }
        return binding.root
    }
}