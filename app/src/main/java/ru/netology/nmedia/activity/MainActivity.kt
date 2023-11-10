package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.shareById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onCancelEdit(post: Post) {
                viewModel.cancelEdit()
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

        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                binding.content.setText(post.content)
                binding.content.focusAndShowKeyboard()
                binding.group.visibility = VISIBLE
                binding.editText.text = post.content
            }
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.changeContentAndSave(text)
            binding.content.setText("")
            binding.content.clearFocus()
            binding.group.visibility = GONE
            AndroidUtils.hideKeyboard(it)
        }

        binding.cancel.setOnClickListener {
            viewModel.cancelEdit()
            binding.content.text.clear()
            binding.content.clearFocus()
            binding.group.visibility = GONE
            AndroidUtils.hideKeyboard(it)
        }
    }
}