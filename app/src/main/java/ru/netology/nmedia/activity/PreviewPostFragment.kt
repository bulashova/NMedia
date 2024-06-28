package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPreviewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.glide.load
import ru.netology.nmedia.glide.loadCircleCrop
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class PreviewPostFragment : Fragment() {

    companion object {
        var Bundle.longArg: Long by LongArg
        var Bundle.textArg: String? by StringArg
        const val BASE_URL = "http://10.0.2.2:9999/"
    }

    private val Fragment.packageManager get() = activity?.packageManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_previewPostFragment_to_feedFragment)
            }
        })

        val viewModel by activityViewModels<PostViewModel>()
        val binding = FragmentPreviewPostBinding.inflate(layoutInflater, container, false)

        fun onPlayVideo(post: Post) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoURL))
            if (packageManager?.let { intent.resolveActivity(it) } != null) {
                startActivity(intent)
            } else {
                Snackbar.make(
                    binding.root, R.string.no_apps,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.ok) {
                        findNavController().navigateUp()
                    }.show()
            }
        }

        val id = requireArguments().longArg
        val post = viewModel.data.value?.posts?.find { it.id == id }

        if (post == null) {
            findNavController().navigateUp()
        } else {
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                avatar.loadCircleCrop("${BASE_URL}avatars/${post.authorAvatar}")

                if (!post.videoURL.isNullOrBlank()) {
                    group.visibility = View.VISIBLE
                    video.setImageResource(R.drawable.ic_youtube)
                    videoTitle.text = post.videoTitle
                    play.setOnClickListener {
                        onPlayVideo(post)
                    }
                    video.setOnClickListener {
                        onPlayVideo(post)
                    }
                    videoTitle.setOnClickListener {
                        onPlayVideo(post)
                    }
                } else group.visibility = View.GONE

                if (post.attachment != null) {
                    attachment.visibility = View.VISIBLE
                    attachment.load("${BASE_URL}media/${post.attachment.url}")
                    attachment.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_previewPostFragment_to_previewAttachmentFragment,
                            Bundle().apply {
                                textArg = requireNotNull(post.attachment).url
                            })
                    }
                } else attachment.visibility = View.GONE

                like.isChecked = post.likedByMe
                like.text = Count.formatCount(post.likes)
                share.text = post.shares?.let { Count.formatCount(it) }
                view.text = post.views?.let { Count.formatCount(it) }

                like.setOnClickListener {
                    viewModel.likeById(post.id)
                    findNavController().navigateUp()
                }

                share.setOnClickListener {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val chooser =
                        Intent.createChooser(intent, getString(R.string.sharing_title))

                    if (packageManager?.let {
                            chooser.resolveActivity(
                                it
                            )
                        } != null) {
                        startActivity(chooser)
                    } else {
                        Snackbar.make(
                            binding.root, R.string.no_apps,
                            BaseTransientBottomBar.LENGTH_INDEFINITE
                        )
                            .setAction(R.string.ok) {
                                findNavController().navigateUp()
                            }.show()
                    }
                }

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.edit -> {
                                    findNavController().navigate(
                                        R.id.action_previewPostFragment_to_newAndEditPostFragment,
                                        Bundle().apply {
                                            textArg = post.content
                                            longArg = post.id
                                        })
                                    viewModel.edit(post)
                                    true
                                }

                                R.id.remove -> {
                                    viewModel.removeById(post.id)
                                    findNavController().navigateUp()
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
        return binding.root
    }
}