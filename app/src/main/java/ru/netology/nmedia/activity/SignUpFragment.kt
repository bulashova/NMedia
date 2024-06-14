package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel

class SignUpFragment : Fragment() {

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

        val viewModel by activityViewModels<SignUpViewModel>()
        val binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        with(binding) {
            avatar.setImageResource(R.drawable.ic_baseline_account_circle_48)
            name.setText("NoName")
            login.setText("noname")
            pass.setText("secret")
            retryPass.setText("secret")
            register.setOnClickListener {
                val login = requireNotNull(login.text).toString()
                val pass = requireNotNull(pass.text).toString()
                val name = requireNotNull(name.text).toString()
                viewModel.registerWithPhoto(login, pass, name)
                findNavController().navigate(R.id.action_signUpFragment_to_feedFragment)
            }
            back.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .galleryOnly()
                .createIntent(pickPhotoLauncher::launch)

        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.avatar.setImageURI(it.uri)
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        findNavController().navigate(R.id.action_signUpFragment_to_feedFragment)
                    }
                    .show()
            }
        }

        return binding.root

    }
}