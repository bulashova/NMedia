package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewAndEditPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.util.AndroidUtils.showTheKeyboardNow
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.util.AndroidUtils

class NewAndEditPostFragment : Fragment() {

    companion object {
        var Bundle.longArg: Long by LongArg
        var Bundle.textArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val prefs = context?.getSharedPreferences("saved", MODE_PRIVATE)
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (arguments?.longArg == 0L) {
                    val text = requireActivity().findViewById<EditText>(R.id.content)
                    with(prefs?.edit()) {
                        this?.putString("saved", text.text.toString())
                        this?.apply()
                        println("text saved")
                    }
                }
                findNavController().navigateUp()
            }
        })

        val viewModel by activityViewModels<PostViewModel>()
        val binding = FragmentNewAndEditPostBinding.inflate(layoutInflater, container, false)

        binding.edit.focusAndShowKeyboard()

        if (arguments?.longArg == 0L) {
            binding.topAppBar.setTitle(R.string.new_post)
            if (arguments?.textArg.isNullOrBlank()) {
                if (!prefs?.getString("saved", null).isNullOrBlank()) {
                    prefs!!.getString("saved", null).let {
                        binding.edit.setText(it)
                        binding.edit.showTheKeyboardNow()
                        println("text loaded")
                    }
                } else {
                    binding.edit.setHint(R.string.post_text)
                    binding.edit.showTheKeyboardNow()
                }
            } else
                binding.edit.setText(arguments?.textArg.orEmpty())
        } else {
            binding.topAppBar.setTitle(R.string.post_edit)
            binding.edit.setText(arguments?.textArg.orEmpty())
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

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            val text = binding.edit.text?.toString()
                            if (!text.isNullOrBlank()) {
                                viewModel.changeContentAndSave(text)
                                viewModel.loadPosts()
                                AndroidUtils.hideKeyboard(requireView())
                                findNavController().navigateUp()
                            } else {
                                Snackbar.make(
                                    binding.root, R.string.error_empty_content,
                                    BaseTransientBottomBar.LENGTH_INDEFINITE
                                )
                                    .setAction(android.R.string.ok) {
                                        findNavController().navigateUp()
                                    }.show()
                            }
                            prefs?.edit()?.clear()?.apply()
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner
        )

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .galleryOnly()
                .createIntent(pickPhotoLauncher::launch)

        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .cameraOnly()
                .createIntent(pickPhotoLauncher::launch)

        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        binding.removePhoto.setOnClickListener {
            viewModel.dropPhoto()
        }
        viewModel.photo.observe(viewLifecycleOwner) {
            binding.photoContainer.isVisible = it.uri != null
            binding.photo.setImageURI(it.uri)
        }

        return binding.root
    }
}