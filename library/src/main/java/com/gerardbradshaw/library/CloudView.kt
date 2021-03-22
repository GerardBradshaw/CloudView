package com.gerardbradshaw.library

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class CloudView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var isDrawn = false
  private var isAnimating = false
  private var requestedSizeRange = 300..500
  private val imageViews = HashSet<ImageView>()

  private var imageResId: Int? = R.drawable.ic_cloud
  private var imageBitmap: Bitmap? = null
  private var imageDrawable: Drawable? = null


  /**
   * Sets all values to their defaults. This is not necessary as a first step if no values have
   * been changed.
   */
  fun setDefaults() {
    val wasAnimating = isAnimationRequested
    stopAnimationsUntilRespawn()

    imageResId = R.drawable.ic_cloud
    imageBitmap = null
    imageDrawable = null
    requestedSizeRange = 300..500
    setCloudCount(10)

    if (wasAnimating) startAnimation()
  }

  /**
   * The maximum number of clouds seen in the view at once. Note clouds are redrawn on change.
   */
  var cloudCount: Int = 10
    set(value) {
      field = value
      if (isDrawn) respawnClouds()
    }

  private fun respawnClouds() {
    resetClouds(cloudCount)
    if (isAnimationRequested) forceStartAnimation()
  }

  private fun resetClouds(cloudCount: Int) {
    removeAllViews()
    imageViews.clear()
    for (i in 0 until cloudCount) {
      val cloud = ImageView(context, null)

      val resId = imageResId
      val bitmap = imageBitmap
      val drawable = imageDrawable

      when {
        resId != null -> cloud.setImageResource(imageResId!!)
        bitmap != null -> cloud.setImageBitmap(bitmap)
        drawable != null -> cloud.setImageDrawable(drawable)
        else -> cloud.setImageResource(R.drawable.ic_cloud)
      }

      cloud.x = width.toFloat()
      cloud.y = height * Random.nextFloat() - cloud.height

      val dimen = Random.nextInt(requestedSizeRange.first, requestedSizeRange.last)
      addView(cloud, LayoutParams(dimen, dimen))
      imageViews.add(cloud)
    }
  }

  /**
   * The maximum size of a cloud, in pixels. Clouds are randomly sized between [minCloudSize] and
   * maxCloudSize. Note that new cloud sizes are calculated and shown immediately.
   */
  var maxCloudSize: Int
    get() = requestedSizeRange.last
    set(value) {
      setSizeRange(requestedSizeRange.first..value)
    }

  /**
   * The mimimum size of a cloud, in pixels. Clouds are randomly sized between minCloudSize and
   * [maxCloudSize]. Note that new cloud sizes are calculated and shown immediately.
   */
  var minCloudSize: Int
    get() = requestedSizeRange.first
    set(value) {
      setSizeRange(value..requestedSizeRange.last)
    }

  /**
   * The time, in milliseconds, for the fastest cloud to cross the entire view. The slowest cloud
   * will pass the entire view in basePassTimeMs + [passTimeVarianceMs]. Note that clouds currently
   * crossing the view are not updated.
   */
  var basePassTimeMs: Int = 10000
    set(value) {
      if (value <= 0) throw IllegalArgumentException()
      else field = value
    }

  /**
   * The variance, in milliseconds, between the fastest and slowest possible clouds to cross the
   * entire view. A variance of 0 means all clouds will move at the same speed. Note that clouds
   * currently crossing the view are not updated.
   */
  var passTimeVarianceMs: Int = 2000
    set(value) {
      if (value < 0) throw IllegalArgumentException()
      else field = value
    }

  /**
   * True if cloud animations are on, false otherwise.
   */
  var isAnimationRequested = false
    private set

  /**
   * When enabled, clouds will fade in over 1 second when entering the view.
   */
  var isFadeInEnabled = false

  init {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        if (isDrawn) {
          respawnClouds()
          viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
      }
    })
  }

  /**
   * Setter for [cloudCount] which returns the view for chaining.
   */
  fun setCloudCount(n: Int): CloudView {
    cloudCount = n
    return this
  }

  /**
   * Sets [minCloudSize] and [maxCloudSize] in one function call. Returns the view for chaining.
   */
  fun setSizeRange(range: IntRange): CloudView {
    if (range != requestedSizeRange) {
      val min = min(range.first, range.last)
      val max = max(range.first, range.last)
      if (min == max) throw IllegalArgumentException()

      requestedSizeRange = max(min, 0)..max(max, 0)
    }

    if (isDrawn) randomizeCloudSizes()

    return this
  }

  private fun randomizeCloudSizes() {
    for (view in imageViews) {
      view.updateLayoutParams {
        val dimen = Random.nextInt(requestedSizeRange.first, requestedSizeRange.last)
        this.width = dimen
        this.height = dimen
      }
    }
  }

  /**
   * Setter for [maxCloudSize] which returns the view for chaining.
   */
  fun setMaxSize(max: Int): CloudView {
    return if (max < requestedSizeRange.first) setSizeRange(max..max)
    else setSizeRange(requestedSizeRange.first..max)
  }

  /**
   * Setter for [minCloudSize] which returns the view for chaining.
   */
  fun setMinSize(min: Int): CloudView {
    return if (min > requestedSizeRange.last) setSizeRange(min..min)
    else setSizeRange(min..requestedSizeRange.last)
  }

  /**
   * Setter for [basePassTimeMs] which returns the view for chaining.
   */
  fun setBasePassTime(timeMs: Int): CloudView {
    basePassTimeMs = timeMs
    return this
  }

  /**
   * Setter for [varianceMs] which returns the view for chaining.
   */
  fun setPassTimeVariance(varianceMs: Int): CloudView {
    passTimeVarianceMs = varianceMs
    return this
  }

  /**
   * Sets a custom image to be used instead of the default cloud.
   */
  fun setImage(bitmap: Bitmap): CloudView {
    imageBitmap = bitmap
    imageResId = null
    imageDrawable = null

    restartAnimation()
    return this
  }

  /**
   * Sets a custom image to be used instead of the default cloud.
   */
  fun setImage(resId: Int): CloudView {
    imageResId = resId
    imageBitmap = null
    imageDrawable = null

    restartAnimation()
    return this
  }

  /**
   * Sets a custom image to be used instead of the default cloud.
   */
  fun setImage(drawable: Drawable): CloudView {
    imageDrawable = drawable
    imageBitmap = null
    imageResId = null

    restartAnimation()
    return this
  }

  private fun restartAnimation() {
    val wasAnimating = isAnimationRequested
    stopAnimationsUntilRespawn()
    if (wasAnimating) startAnimation()
  }

  /**
   * Starts moving clouds across the view.
   */
  fun startAnimation() {
    isAnimationRequested = true
    if (isAnimating) return
    if (isDrawn) forceStartAnimation()
  }

  private fun forceStartAnimation() {
    if (isDrawn) {
      if (imageViews.isEmpty()) {
        Log.e(TAG, "forceStartAnimation: no cloud views; nothing to animate")
        return
      }

      animateClouds()
    }
  }

  private fun animateClouds() {
    isAnimating = true
    for (image in imageViews) {
      animateCloud(image)
    }
  }

  private fun animateCloud(cloud: ImageView) {
    cloud.animation = null

    cloud.x = width.toFloat()
    cloud.y = height * Random.nextFloat()

    val cloudWidth = cloud.layoutParams.width.toFloat()

    ObjectAnimator.ofFloat(cloud, "translationX", -cloudWidth).apply {
      duration = (basePassTimeMs + passTimeVarianceMs * Random.nextFloat()).toLong()
      startDelay = ((basePassTimeMs + passTimeVarianceMs) * Random.nextFloat()).toLong()
      interpolator = LinearInterpolator()

      doOnStart {
        if (isFadeInEnabled) {
          with(AlphaAnimation(0.0f, 1.0f)) {
            this.duration = 1000
            cloud.startAnimation(this)
          }
        }
      }

      doOnEnd {
        if (imageViews.contains(cloud) && isAnimating) {
          animateCloud(cloud)
        }
      }

      start()
    }
  }

  /**
   * Stops the clouds from animating and clears them from view.
   */
  fun stopAnimations() {
    resetClouds(cloudCount)
    isAnimationRequested = false
    isAnimating = false
  }

  private fun stopAnimationsUntilRespawn() {
    resetClouds(cloudCount)
    isAnimating = false
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    isDrawn = true
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (isDrawn) stopAnimationsUntilRespawn()
  }

  companion object {
    private const val TAG = "GGG CloudView"
  }
}