package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewAndEditPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class NewAndEditPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNewAndEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.setText(intent?.getStringExtra(Intent.EXTRA_TEXT))
        binding.content.focusAndShowKeyboard()

        binding.topAppBar.setTitle(if (intent?.getStringExtra(Intent.EXTRA_TEXT)
                .isNullOrBlank()) R.string.new_post else R.string.post_edit)

        if (intent?.getStringExtra(Intent.EXTRA_TEXT)
                .isNullOrBlank()) binding.content.setHint(R.string.post_text)

        binding.save.setOnClickListener() {
            val text = binding.content.text?.toString()
            if (!text.isNullOrBlank()) {
                setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
                finish()
            } else {
                Snackbar.make(
                    binding.root, R.string.error_empty_content,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        finish()
                    }.show()
            }
            setResult(RESULT_CANCELED)
        }

        binding.cancel.setOnClickListener() {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}