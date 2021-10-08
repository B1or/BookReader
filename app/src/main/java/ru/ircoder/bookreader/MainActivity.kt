package ru.ircoder.bookreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val ns: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            val parser: XmlPullParser = resources.getXml(R.xml.book)
            readBook(parser)
        } catch (t: Throwable) {
            Toast.makeText(this, "Ошибка при загрузке XML-документа: $t", Toast.LENGTH_LONG).show()
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBook(parser: XmlPullParser): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        parser.require(XmlPullParser.START_TAG, ns, "book")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "chapter") {
                chapters.add(readChapter(parser))
            } else {
                skip(parser)
            }
        }
        return chapters
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChapter(parser: XmlPullParser): Chapter {
        parser.require(XmlPullParser.START_TAG, ns, "chapter")
        var heading: String? = null
        var body: String? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "heading" ->
                    heading = readHeading(parser)
                "body" ->
                    body = readBody(parser)
                else ->
                    skip(parser)
            }
        }
        return Chapter(heading, body)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readHeading(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "heading")
        val heading = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "heading")
        return heading
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readBody(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "body")
        val body = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "body")
        return body
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
}