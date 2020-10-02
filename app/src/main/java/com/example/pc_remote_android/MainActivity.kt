package com.example.pc_remote_android

//

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.pc_remote_android.models.VolumeViewModel
import com.example.pc_remote_android.networking.HTTPClient
import com.example.pc_remote_android.ui.main.SectionsPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class MainActivity : AppCompatActivity() {
    private val baseUrl = "http://192.168.178.23/"
    private lateinit var queue: RequestQueue
    private lateinit var httpClient: HTTPClient
    private lateinit var volumeViewModel: VolumeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        queue = Volley.newRequestQueue(this)
        httpClient = HTTPClient(this.baseUrl, this.queue)
//        volumeController = VolumeController(httpClient, volumeModel, CustomErrorListener())
//        volumeController.sync()
        volumeViewModel = VolumeViewModel(httpClient, CustomErrorListener())

        volumeViewModel.getCurrentVolume().observe(this, { newVolume: Int ->
            Log.d("volumeObserver", "newVolume: ".plus(newVolume))
            seekBarVolume.progress = newVolume
            textViewVolume.text = newVolume.toString()
        })

        volumeViewModel.getCurrentMute().observe(this, { newMute: Boolean ->
            muteSwitch.isChecked = newMute
        })

        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        // val viewPager: ViewPager = findViewById(R.id.view_pager)
        // viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        // tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        muteSwitch.setOnCheckedChangeListener { _, isChecked ->  volumeViewModel.setMute(isChecked)}

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var isTracking: Boolean = false
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                textViewVolume.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTracking = true
                return
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTracking = false
                if (seekBar != null) {
                    volumeViewModel.setVolume(seekBar.progress)
                }
                return
            }
        })

    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("onKeyUp", keyCode.toString())

        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                volumeViewModel.increaseVolume()
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeViewModel.decreaseVolume()
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }


    private fun getCurrentSeekBarValue(): Int {
        return seekBarVolume.progress
    }

    private fun setSeekBarValue(newValue: Int) {
        var newVolume: Int = newValue;

        // make sure newValue is between 0 and 100
        if (newVolume > 100) {
            newVolume = 100
        } else if (newVolume < 0) {
            newVolume = 0
        }
        // Update seekbar value
        seekBarVolume.progress = newVolume
    }


    inner class CustomErrorListener : Response.ErrorListener {
        /** This listener handles the HTTP errors in the context of the main activity */
        private fun parseVolleyError(error: VolleyError) {
            try {
                Log.d("parseVolleyError", error.toString())
                val responseBody = String(error.networkResponse.data, Charsets.UTF_8)
                val data = JSONObject(responseBody)
                val errors = data.getJSONArray("errors")
                val jsonMessage = errors.getJSONObject(0)
                val message = jsonMessage.getString("message")
                Log.e("getErrorResponse", message)
                textViewError.text = message
            } catch (e: JSONException) {
                Log.e("getErrorResponse", "JSONException".plus(e.toString()))
                textViewError.text = e.toString()
            } catch (error: UnsupportedEncodingException) {
                Log.e("getErrorResponse", "UnsupportedEncodingException".plus(error.toString()))
                textViewError.text = error.toString()
            } catch (err: java.lang.Exception) {
                Log.e("getErrorResponse", err.toString())
                textViewError.text = err.toString()
            }
        }

        override fun onErrorResponse(error: VolleyError?) {
            if (error == null) {
                Log.d("onErrorResponse", "error is null...")
            } else {
                parseVolleyError(error);
            }
        }

    }
}
