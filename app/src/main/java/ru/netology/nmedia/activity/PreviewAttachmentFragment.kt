package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPreviewAttachmentBinding
import ru.netology.nmedia.glide.load
import ru.netology.nmedia.util.StringArg

@AndroidEntryPoint
class PreviewAttachmentFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
        const val BASE_URL = "http://10.0.2.2:9999/"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_previewAttachmentFragment_to_feedFragment)
            }
        })

        val binding = FragmentPreviewAttachmentBinding.inflate(layoutInflater, container, false)
        val imageUrl = requireArguments().textArg

        if (imageUrl.isNullOrBlank()) {
            findNavController().navigateUp()
        } else {
            with(binding) {
                attachment.load("${BASE_URL}media/$imageUrl")
                back.setOnClickListener {
                    findNavController().navigateUp()
                }
            }
        }
        return binding.root
    }
}