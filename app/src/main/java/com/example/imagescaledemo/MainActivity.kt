package com.example.imagescaledemo

import android.annotation.SuppressLint
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.translationMatrix
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor: Float = 1.0f
    private lateinit var mImageView: AppCompatImageView
    private var minScale = -6.0f
    private val maxScale = 6.0f
    private var click = true
    private var lastX = 0f
    private var lastY = 0f
    private var mode = 0
    private var startDistance = 0f
    private var sumScale = 0f
    private var preScale = 0f
    private var startMatrix = Matrix()
    private var lastScaleFactor = 1f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mImageView = findViewById(R.id.iv_example)
        mScaleGestureDetector = ScaleGestureDetector(this, ScaleGestureListener())
        mImageView.setOnTouchListener { _, event ->
            mImageView.scaleType = ImageView.ScaleType.MATRIX
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录单指按下的位置
                    lastX = event.x
                    lastY = event.y
                    mode = 1
                    startMatrix.set(mImageView.imageMatrix)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // 记录双指按下的位置和距离
                    startDistance = getDistance(event)
                    if (startDistance > 10f) {
                        startMatrix.set(mImageView.imageMatrix)
                        mode = 2
                    }
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mode == 1) {
                        // 单指拖动
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        mImageView.imageMatrix = startMatrix.apply {
//                            translationMatrix(dx,dy)
                            postTranslate(dx, dy)
                        }
                        lastX = event.x
                        lastY = event.y
                    } else if (mode == 2) {
                        // 双指缩放
                        val currentDistance = getDistance(event)
                        if (currentDistance > 10f) {
                            val scale = currentDistance / startDistance
                            if (scale * lastScaleFactor > 1.0f) {
                                if (sumScale >= maxScale || sumScale <= minScale) {
                                    return@setOnTouchListener true
                                }
                                sumScale += scale
                                mImageView.imageMatrix = startMatrix.apply {
                                    postScale(scale, scale, getMidX(event), getMidY(event))
                                    lastScaleFactor *= scale
                                }
                            } else {
                                sumScale -= scale
                            }
                        }
                    }
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = 0
                    sumScale = 1.0f
                    return@setOnTouchListener true
                }

                else -> return@setOnTouchListener true

            }

        }
    }


    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            mScaleFactor = mScaleFactor.coerceIn(0.1f, 5.0f)
            mImageView.scaleX = mScaleFactor
            mImageView.scaleY = mScaleFactor
            return true
        }
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return sqrt(dx * dx + dy * dy)
    }

    private fun getMidX(event: MotionEvent): Float {
        return (event.getX(0) + event.getX(1)) / 2
    }

    private fun getMidY(event: MotionEvent): Float {
        return (event.getY(0) + event.getY(1)) / 2
    }
}