package ru.ircoder.bookreader

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import ru.ircoder.bookreader.MainActivity.Companion.TAG
import ru.ircoder.bookreader.databinding.FragmentContentsBinding
import ru.ircoder.bookreader.databinding.HeadingsItemBinding
import java.io.IOException

class ContentsFragment : Fragment() {
    private lateinit var binding: FragmentContentsBinding
    private val model: SharedViewModel by activityViewModels()
    private lateinit var book: List<Chapter>
    private val ns: String? = null
    private lateinit var mAdView : AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contents, container, false)
        binding.viewModel = model
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        book = readBook()
        binding.rvHeadings.layoutManager = LinearLayoutManager(this.context)
        model.textSize.observe(viewLifecycleOwner, {
            Log.d(TAG, "observe textSize: $it")
            binding.rvHeadings.adapter = HeadingsAdapter(book) { position ->
                onHeadingsItemClick(position)
            }
        })
        mAdView = binding.avContents
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val bookmark = menu.findItem(R.id.bookmarkItemMenu)
        bookmark.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "item selected")
        return when (item.itemId) {
            R.id.scaleUpItemMenu -> {
                if (model.textSize.value != null && model.textSize.value!! < 5) model.textSize.value = model.textSize.value!!.plus(1)
                Log.d(TAG, "textSize: ${model.textSize.value}")
                true
            }
            R.id.scaleDownItemMenu -> {
                if (model.textSize.value != null && model.textSize.value!! > 1) model.textSize.value = model.textSize.value!!.minus(1)
                Log.d(TAG, "textSize: ${model.textSize.value}")
                true
            }
            R.id.dayNightModeItemMenu -> {
                if (model.nightMode.value != null) model.setNightMode(!model.nightMode.value!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    private fun readBook(): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        try {
            val resourceBook = this.resources
            val parser: XmlPullParser = resourceBook.getXml(R.xml.book)
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
            Toast.makeText(this.context, "Ошибка при загрузке XML-документа: $t", Toast.LENGTH_LONG).show()
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
        val resourceBook = this.resources
        val packageNameBook = this.context?.packageName
        val imageResourceId = resourceBook.getIdentifier(imageName, "drawable", packageNameBook)
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
        val bundle = Bundle().apply {
            putString(HEADING, book[position].heading)
            book[position].image?.let { putInt(IMAGE, it) }
            putString(BODY, book[position].body)
        }
        findNavController().navigate(R.id.action_contentsFragment_to_chapterFragment, bundle)
    }

    inner class HeadingsAdapter(private val chapters: List<Chapter>, private val onItemClicked: (position: Int) -> Unit): RecyclerView.Adapter<HeadingsAdapter.HeadingsViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadingsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = HeadingsItemBinding.inflate(inflater)
            binding.tvItemHeading.textSize =
                when (model.textSize.value) {
                    1 -> 10f
                    2 -> 12f
                    3 -> 14f
                    4 -> 16f
                    5 -> 20f
                    else -> 14f
                }
            return HeadingsViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HeadingsViewHolder, position: Int) {
            holder.bind(chapters[position])
            holder.itemView.setOnClickListener {
                onItemClicked(position)
            }
        }

        override fun getItemCount(): Int = chapters.size

        inner class HeadingsViewHolder(private val binding: HeadingsItemBinding): RecyclerView.ViewHolder(binding.root) {
            fun bind(chapter: Chapter) {
                with(binding) {
                    tvItemHeading.text = chapter.heading
                }
            }
        }
    }

    companion object {
        const val BOOK = "book"
        const val CHAPTER = "chapter"
        const val HEADING = "heading"
        const val IMAGE = "image"
        const val BODY = "body"
        const val PARAGRAPH = "paragraph"
    }
}