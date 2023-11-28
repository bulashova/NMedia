package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        val newAndEditPostContract =
            registerForActivityResult(NewAndEditPostActivityContract()) { result ->
                result ?: return@registerForActivityResult
                viewModel.changeContentAndSave(result)
            }

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

                if (chooser.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                    viewModel.shareById(post.id)
                } else {
                    Snackbar.make(
                        binding.root, R.string.no_apps,
                        BaseTransientBottomBar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.ok) {
                            finish()
                        }.show()
                }
            }

            override fun onEdit(post: Post) {
                newAndEditPostContract.launch(post.content)
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

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Snackbar.make(
                        binding.root, R.string.no_apps,
                        BaseTransientBottomBar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.ok) {
                            finish()
                        }.show()
                }
            }
        }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            val newPost = adapter.currentList.size < posts.size
            adapter.submitList(posts) {
                if (newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.newPostButton.setOnClickListener {
            viewModel.cancel()
            newAndEditPostContract.launch("")
        }
    }
}