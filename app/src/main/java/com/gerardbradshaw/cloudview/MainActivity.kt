package com.gerardbradshaw.cloudview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import kotlin.math.max

class MainActivity : AppCompatActivity() {
  private lateinit var cloudView: com.gerardbradshaw.library.CloudView

  private lateinit var cloudCountView: TextView
  private lateinit var cloudCountSeekBar: SeekBar

  private lateinit var minSizeView: TextView
  private lateinit var minSizeSeekBar: SeekBar

  private lateinit var maxSizeView: TextView
  private lateinit var maxSizeSeekBar: SeekBar

  private lateinit var passTimeEditText: EditText
  private lateinit var varianceTimeEditText: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    locateViews()
    initViewDefaults()
    initListeners()

    minSizeView.text = cloudView.minCloudSize.toString()
    maxSizeView.text = cloudView.maxCloudSize.toString()

    cloudView.startAnimation()
  }

  private fun locateViews() {
    cloudView = findViewById(R.id.cloud_view)

    cloudCountSeekBar = findViewById(R.id.cloud_count_seek_bar)
    cloudCountView = findViewById(R.id.cloud_count_text_view)

    minSizeSeekBar = findViewById(R.id.min_size_seek_bar)
    minSizeView = findViewById(R.id.min_size_text_view)

    maxSizeSeekBar = findViewById(R.id.max_size_seek_bar)
    maxSizeView = findViewById(R.id.max_size_text_view)

    passTimeEditText = findViewById(R.id.pass_time_edit_text)
    varianceTimeEditText = findViewById(R.id.pass_variance_edit_text)
  }

  private fun initViewDefaults() {
    cloudView.setDefaults()

    cloudCountSeekBar.progress = cloudView.cloudCount
    cloudCountView.text = "${cloudView.cloudCount}"

    minSizeSeekBar.progress = cloudView.minCloudSize
    minSizeView.text = "${cloudView.minCloudSize}"

    maxSizeSeekBar.progress = cloudView.maxCloudSize
    maxSizeView.text = "${cloudView.maxCloudSize}"

    passTimeEditText.hint = "${cloudView.basePassTimeMs}"
    varianceTimeEditText.hint = "${cloudView.passTimeVarianceMs }"
  }

  private fun initListeners() {
    cloudCountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        cloudCountView.text = "$progress"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        cloudView.stopAnimation()
      }
      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        cloudView.setCloudCount(seekBar?.progress ?: 0)
      }
    })

    findViewById<Button>(R.id.start_button).setOnClickListener {
      cloudView.startAnimation()
    }

    findViewById<Button>(R.id.stop_button).setOnClickListener {
      cloudView.stopAnimation()
    }

    findViewById<EditText>(R.id.pass_time_edit_text).addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        cloudView.stopAnimation()
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val time = max(1, Integer.parseInt(s?.toString() ?: "1"))
        cloudView.setBasePassTime(time)
      }

      override fun afterTextChanged(s: Editable?) {
      }
    })

    findViewById<EditText>(R.id.pass_variance_edit_text).addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        cloudView.stopAnimation()
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val time = max(1, Integer.parseInt(s?.toString() ?: "1"))
        cloudView.setPassTimeVariance(time)
      }

      override fun afterTextChanged(s: Editable?) {
      }
    })


    findViewById<SeekBar>(R.id.min_size_seek_bar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        minSizeView.text = "$progress"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        cloudView.stopAnimation()
      }
      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        cloudView.minCloudSize = seekBar?.progress ?: 0
      }
    })

    findViewById<SeekBar>(R.id.max_size_seek_bar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        maxSizeView.text = "$progress"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
//        cloudView.stopAnimation()
      }
      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        cloudView.maxCloudSize = seekBar?.progress ?: 1
      }
    })
  }

  companion object {
    private const val TAG = "GGG MainActivity"
  }
}