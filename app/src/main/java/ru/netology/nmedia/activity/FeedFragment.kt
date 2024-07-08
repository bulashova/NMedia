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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewAndEditPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PreviewPostFragment.Companion.longArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
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
                if (appAuth.authStateFlow.value.token != null) {
                    viewModel.likeById(post)
                } else {
                    findNavController().navigate(R.id.action_feedFragment_to_authDialogFragment)
                }
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

            override fun onRetrySave(post: Post) {
                Snackbar.make(
                    binding.root,
                    R.string.description_post_not_saved_on_the_server,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.retry) {
                        viewModel.retrySave(post)
                    }
                    .show()
            }

            override fun onPreviewAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_previewAttachmentFragment,
                    Bundle().apply {
                        textArg = requireNotNull(post.attachment).url
                    })
            }
        }
        )

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        viewModel.refreshPosts()
                    }
                    .show()
            }
        }

        binding.retry.setOnClickListener {
            viewModel.loadPosts()
        }

//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            Log.d("FeedFragment", "Newer count: $it")
//            if (it > 0) {
//                binding.recentEntries.visibility = View.VISIBLE
//                binding.recentEntries.setOnClickListener {
//                    viewModel.loadHiddenPosts()
//                    binding.recentEntries.visibility = View.GONE
//                }
//            }
//        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })

        binding.newPostButton.setOnClickListener {
            if (appAuth.authStateFlow.value.token != null) {
                viewModel.cancel()
                findNavController().navigate(R.id.action_feedFragment_to_newAndEditPostFragment,
                    Bundle().apply {
                        longArg = 0L
                    })
            } else
                findNavController().navigate(R.id.action_feedFragment_to_authDialogFragment)
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }
        return binding.root
    }
}