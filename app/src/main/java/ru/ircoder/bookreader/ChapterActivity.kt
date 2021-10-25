package ru.ircoder.bookreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import ru.ircoder.bookreader.MainActivity.Companion.BODY
import ru.ircoder.bookreader.MainActivity.Companion.HEADING
import ru.ircoder.bookreader.MainActivity.Companion.IMAGE
import ru.ircoder.bookreader.MainActivity.Companion.TAG
import ru.ircoder.bookreader.databinding.ActivityChapterBinding

class ChapterActivity : AppCompatActivity() {
    private val heading by lazy(::readHeading)
    private val image by lazy(::readImage)
    private val body by lazy(::readBody)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityChapterBinding = DataBindingUtil.setContentView(this, R.layout.activity_chapter)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tvHeading.text = heading
        binding.tvBody.text = body
        Log.d(TAG, "image: $image")
        image?.let { binding.ivImage.setImageResource(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun readHeading() = intent.extras?.getString(HEADING)

    private fun readImage() = intent.extras?.getInt(IMAGE)

    private fun readBody() = intent.extras?.getString(BODY)
}