package ru.ircoder.bookreader

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import ru.ircoder.bookreader.ContentsFragment.Companion.BODY
import ru.ircoder.bookreader.ContentsFragment.Companion.HEADING
import ru.ircoder.bookreader.ContentsFragment.Companion.IMAGE
import ru.ircoder.bookreader.databinding.FragmentChapterBinding

class ChapterFragment : Fragment() {
    private lateinit var binding: FragmentChapterBinding
    private val model: SharedViewModel by activityViewModels()
    private var heading: String? = null
    private var image: Int? = null
    private var body: String? = null
    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            heading = it.getString(HEADING)
            image = it.getInt(IMAGE)
            body = it.getString(BODY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chapter, container, false)
        binding.viewModel = model
        binding.lifecycleOwner = this
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvHeading.text = heading
        binding.tvBody.text = body
        image?.let { binding.ivImage.setImageResource(it) }
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val positionRatio = if (model.positionRatio.value == null || model.positionRatio.value == 0f) sharedPref.getFloat(heading, 0f) else model.positionRatio.value!!
        binding.svChapter.post {
            val scrollPosition = (positionRatio * (binding.svChapter.getChildAt(0).height - binding.svChapter.height)).toInt()
            binding.svChapter.scrollTo(0, scrollPosition)
        }
        model.textSize.observe(viewLifecycleOwner, { textSize ->
            Log.d(MainActivity.TAG, "observed, textSize: $textSize")
            when (textSize) {
                1 -> {
                    binding.tvHeading.textSize = 14f
                    binding.tvBody.textSize = 10f
                }
                2 -> {
                    binding.tvHeading.textSize = 16f
                    binding.tvBody.textSize = 12f
                }
                3 -> {
                    binding.tvHeading.textSize = 20f
                    binding.tvBody.textSize = 14f
                }
                4 -> {
                    binding.tvHeading.textSize = 24f
                    binding.tvBody.textSize = 16f
                }
                5 -> {
                    binding.tvHeading.textSize = 34f
                    binding.tvBody.textSize = 20f
                }
            }
        })
        mAdView = binding.avChapter
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.bookmarkItemMenu -> {
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return true
                with (sharedPref.edit()) {
                    val positionRatio = binding.svChapter.scrollY.toFloat() / (binding.svChapter.getChildAt(0).height - binding.svChapter.height)
                    putFloat(heading, positionRatio)
                    apply()
                }
                Toast.makeText(this.context, R.string.bookmark, Toast.LENGTH_SHORT).show()
                true
            }
            R.id.scaleUpItemMenu -> {
                if (model.textSize.value != null && model.textSize.value!! < 5) model.textSize.value = model.textSize.value!!.plus(1)
                true
            }
            R.id.scaleDownItemMenu -> {
                if (model.textSize.value != null && model.textSize.value!! > 1) model.textSize.value = model.textSize.value!!.minus(1)
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
        model.positionRatio.value = binding.svChapter.scrollY.toFloat() / (binding.svChapter.getChildAt(0).height - binding.svChapter.height)
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param heading Parameter 1.
         * @param image Parameter 2.
         * @param body Parameter 3.
         * @return A new instance of fragment ChapterFragment.
         */
        @JvmStatic
        fun newInstance(heading: String?, image: Int?, body: String?) =
            ChapterFragment().apply {
                arguments = Bundle().apply {
                    putString(HEADING, heading)
                    if (image != null) {
                        putInt(IMAGE, image)
                    }
                    putString(BODY, body)
                }
            }
    }
}