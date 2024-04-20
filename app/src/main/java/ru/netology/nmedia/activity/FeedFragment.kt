package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewAndEditPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PreviewPostFragment.Companion.longArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {
    private val Fragment.packageManager get() = activity?.packageManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels<PostViewModel>()

        viewModel.loadPosts()

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
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

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_newAndEditPostFragment,
                    Bundle().apply {
                        textArg = post.content
                        longArg = post.id
                    })
                viewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onCancel(post: Post) {
                viewModel.cancel()
            }

            override fun onPlayVideo(post: Post) {
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

            override fun onPreview(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_previewPostFragment,
                    Bundle().apply {
                        longArg = post.id
                    })
            }
        }
        )

        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.errorGroup.isVisible = state.error
            binding.progress.isVisible = state.loading
            binding.emptyText.isVisible = state.empty
        }

        binding.retry.setOnClickListener {
            viewModel.loadPosts()
        }

        viewModel.data.observe(viewLifecycleOwner) {
            val newPost = adapter.currentList.size < viewModel.data.value?.posts?.size!!
            adapter.submitList(viewModel.data.value?.posts) {
                if (newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.newPostButton.setOnClickListener {
            viewModel.cancel()
            findNavController().navigate(R.id.action_feedFragment_to_newAndEditPostFragment,
                Bundle().apply {
                    longArg = 0L
                })
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
            binding.swipeRefresh.isRefreshing = false
        }
        return binding.root
    }
}