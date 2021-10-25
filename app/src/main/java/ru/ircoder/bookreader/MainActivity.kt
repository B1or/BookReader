package ru.ircoder.bookreader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import ru.ircoder.bookreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val book: List<Chapter> by lazy(::readBook)
    private val ns: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.rvHeadings.layoutManager = LinearLayoutManager(this)
        binding.rvHeadings.adapter = HeadingsAdapter(book) { position ->
            onHeadingsItemClick(position)
        }
    }

    private fun readBook(): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        try {
            val parser: XmlPullParser = resources.getXml(R.xml.book)
            parser.next()
            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, ns, BOOK)
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                if (parser.name == CHAPTER) {
                    chapters.add(readChapter(parser))
                } else {
                    skip(parser)
                }
            }
        } catch (t: Throwable) {
            Toast.makeText(this, "Ошибка при загрузке XML-документа: $t", Toast.LENGTH_LONG).show()
        }
        return chapters
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChapter(parser: XmlPullParser): Chapter {
        parser.require(XmlPullParser.START_TAG, ns, CHAPTER)
        var heading: String? = null
        var image: Int? = null
        var body: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                HEADING ->
                    heading = readHeading(parser)
                IMAGE ->
                    image = readImage(parser)
                BODY ->
                    body = readBody(parser)
                else ->
                    skip(parser)
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, CHAPTER)
        return Chapter(heading, image, body)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readHeading(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, HEADING)
        val heading = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, HEADING)
        return heading
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImage(parser: XmlPullParser): Int {
        parser.require(XmlPullParser.START_TAG, ns, IMAGE)
        val imageName = readText(parser)
        Log.d(TAG, "imageName: $imageName")
        val imageResourceId = this.resources.getIdentifier(imageName, "drawable", this.packageName)
        Log.d(TAG, "imageResourceId: $imageResourceId")
        parser.require(XmlPullParser.END_TAG, ns, IMAGE)
        return imageResourceId
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readBody(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, BODY)
        var body = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == PARAGRAPH) {
                body += (if (body.isNotEmpty()) System.lineSeparator() else "") + "    " + readParagraph(parser)
            } else {
                skip(parser)
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, BODY)
        return body
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readParagraph(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, PARAGRAPH)
        val paragraph = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, PARAGRAPH)
        return paragraph
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun onHeadingsItemClick(position: Int) {
        Log.d(TAG, "book[position].image: ${book[position].image}")
        val intent = Intent(this, ChapterActivity::class.java).apply {
            putExtra(HEADING, book[position].heading)
            putExtra(IMAGE, book[position].image)
            putExtra(BODY, book[position].body)
        }
        startActivity(intent)
    }

    companion object {
        const val TAG = "BookReader"
        const val BOOK = "book"
        const val CHAPTER = "chapter"
        const val HEADING = "heading"
        const val IMAGE = "image"
        const val BODY = "body"
        const val PARAGRAPH = "paragraph"
    }
}