package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val viewModel by activityViewModels<PostViewModel>()
        val binding = FragmentNewAndEditPostBinding.inflate(layoutInflater, container, false)

        binding.content.focusAndShowKeyboard()
        binding.content.setText(arguments?.textArg.orEmpty())

        binding.topAppBar.setTitle(
            if (arguments?.textArg.orEmpty()
                    .isBlank() || arguments?.longArg == 0L
            ) R.string.new_post
            else
                R.string.post_edit
        )

        if (arguments?.textArg.orEmpty().isBlank()) {
            binding.content.setHint(R.string.post_text)
            binding.content.showTheKeyboardNow()
        }

        binding.save.setOnClickListener() {
            val text = binding.content.text?.toString()
            if (!text.isNullOrBlank()) {
                viewModel.changeContentAndSave(text)
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
        }

        binding.cancel.setOnClickListener() {
            viewModel.cancel()
            findNavController().navigateUp()
        }
        return binding.root
    }
}