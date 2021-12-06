package com.purewhite.nekogallery.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.purewhite.nekogallery.R
import kotlin.math.abs

// Using cat image API: "http://placekitten.com/$width/$height"
class PictureFragment : Fragment() {
    private var imageView : ImageView? = null
    // Limit: w in [270, 1080], h in [480, 1920]
    private var width = 540
    private var height = 960
    private lateinit var translationGestureDetector : GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private val distMin = 50
    private val wMin = 270
    private val wMax = 1080
    private val hMin = 480
    private val hMax = 1920

    private var xLast = 0.0F
    private var yLast = 0.0F
    private var spanOrig = 0.0F
    private var scaleOrig = 0.0F

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_picture, container, false)
        imageView = view.findViewById(R.id.image_item)
        imageView?.let {
            Glide.with(requireActivity())
                .load("https://imgsa.baidu.com/forum/w%3D580/sign=1de09de1a886c91708035231f93d70c6/58d9a9ec8a136327eafabb2a9e8fa0ec08fac79e.jpg")
                .apply(RequestOptions().circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(it)
        }
        translationGestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent?) = true

            override fun onShowPress(e: MotionEvent?) {}

            override fun onSingleTapUp(e: MotionEvent?) = true

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean = true

            override fun onLongPress(e: MotionEvent?) {}

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val dx = e2!!.x - e1!!.x
                val dy = e1!!.y - e2!!.y
                Log.d("PicFrag::movement", "$dx, $dy")
                if (dx * dx + dy * dy > distMin) {
                    if (abs(dx) > abs(dy)) {
                        if (dx > 0) {
                            // Right
                            if (width < wMax)
                                ++width
                        } else {
                            // Left
                            if (width > wMin)
                                --width
                        }
                    } else {
                        if (dy > 0) {
                            // Up
                            if (height < hMax)
                                ++height
                        } else {
                            // Down
                            if (height > hMin)
                                --height
                        }
                    }
                    // Load image using given API
                    imageView?.let {
                        Glide.with(requireActivity())
                            .load("http://placekitten.com/$width/$height")
                            .apply(RequestOptions().circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(it)
                    }

                }
                return true
            }

        })
        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                detector?.let { it ->
                    val dx = it.focusX - xLast
                    val dy = it.focusY - yLast
                    xLast = it.focusX
                    yLast = it.focusY
                    val rate = it.currentSpan / spanOrig

                    Log.d("PicFrag::translation", "$dx, $dy")
                    Log.d("PicFrag::scale", rate.toString())

                    imageView?.let { iv->
                        iv.scaleX = scaleOrig * rate
                        iv.scaleY = scaleOrig * rate
                        iv.x += dx
                        iv.y += dy
                    }
                }
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                detector?.let {
                    xLast = it.focusX
                    yLast = it.focusY
                    spanOrig = it.currentSpan
                    scaleOrig = imageView!!.scaleX
                }
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {}

        })

        view.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.pointerCount) {
                1 -> translationGestureDetector.onTouchEvent(event)
                2 -> scaleGestureDetector.onTouchEvent(event)
                else -> true
            }
        }
        return view
    }

}