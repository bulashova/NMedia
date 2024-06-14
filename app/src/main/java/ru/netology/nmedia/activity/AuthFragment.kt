package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_authFragment_to_feedFragment)
            }
        })

        val viewModel by activityViewModels<SignInViewModel>()
        val binding = FragmentAuthBinding.inflate(layoutInflater, container, false)

        with(binding) {
            login.setText("student")
            pass.setText("secret")
            enter.setOnClickListener {
                val login = requireNotNull(login.text).toString()
                val pass = requireNotNull(pass.text).toString()
                viewModel.updateUser(login, pass)
                findNavController().navigate(R.id.action_authFragment_to_feedFragment)
            }
            back.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        findNavController().navigate(R.id.action_authFragment_to_feedFragment)
                    }
                    .show()
            }
        }

        return binding.root
    }
}