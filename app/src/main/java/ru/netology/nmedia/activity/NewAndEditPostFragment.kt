package ru.netology.nmedia.activity

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
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

        binding.content.focusAndShowKeyboard()

        if (arguments?.longArg == 0L) {
            binding.topAppBar.setTitle(R.string.new_post)
            if (arguments?.textArg.isNullOrBlank()) {
                if (!prefs?.getString("saved", null).isNullOrBlank()) {
                    prefs!!.getString("saved", null).let {
                        binding.content.setText(it)
                        binding.content.showTheKeyboardNow()
                        println("text loaded")
                    }
                } else {
                    binding.content.setHint(R.string.post_text)
                    binding.content.showTheKeyboardNow()
                }
            } else
                binding.content.setText(arguments?.textArg.orEmpty())
        } else {
            binding.topAppBar.setTitle(R.string.post_edit)
            binding.content.setText(arguments?.textArg.orEmpty())
        }

        binding.save.setOnClickListener() {
            val text = binding.content.text?.toString()
            if (!text.isNullOrBlank()) {
                viewModel.changeContentAndSave(text)
                viewModel.loadPosts()
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
        }
        binding.cancel.setOnClickListener() {
            viewModel.cancel()
            prefs?.edit()?.clear()?.apply()
            findNavController().navigateUp()
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }
        return binding.root
    }
}