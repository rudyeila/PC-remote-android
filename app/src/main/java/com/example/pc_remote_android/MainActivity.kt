package com.example.pc_remote_android

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
import androidx.viewpager.widget.ViewPager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.pc_remote_android.controllers.MouseController
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
import kotlin.math.round
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val baseUrl = "http://192.168.178.23/"
    private lateinit var queue: RequestQueue
    private lateinit var httpClient: HTTPClient
    private lateinit var volumeViewModel: VolumeViewModel
    private lateinit var mouseController: MouseController


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GUI setup
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        // Late inits
        queue = Volley.newRequestQueue(this)
        httpClient = HTTPClient(this.baseUrl, this.queue)

        // Set up Volume ViewModel
        volumeViewModel = VolumeViewModel(httpClient, CustomErrorListener())

        // Set up mouse controller
        mouseController = MouseController(httpClient, CustomErrorListener())

        // Add live data observers
        volumeViewModel.getCurrentVolume().observe(this, { newVolume: Int ->
            Log.d("volumeObserver", "newVolume: ".plus(newVolume))
            seekBarVolume.progress = newVolume
            textViewVolume.text = newVolume.toString()
        })

        volumeViewModel.getCurrentMute().observe(this, { newMute: Boolean ->
            muteSwitch.isChecked = newMute
        })


        // Event listeners
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        muteSwitch.setOnCheckedChangeListener { _, isChecked -> volumeViewModel.setMute(isChecked) }

        textViewError.setOnClickListener() { view: View ->
            (view as TextView).text = ""
            Log.d("View On Click" , "View Clicked!")
        }

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (seekBar != null) {
//                    volumeViewModel.setVolume(progress)
//                    Thread.sleep(25);
//                }
                return
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    volumeViewModel.setVolume(seekBar.progress)
                }
                return
            }
        })

        mouseArea.setOnTouchListener { v, event ->
            val action: Int = MotionEventCompat.getActionMasked(event)
            val DEBUG_TAG = "MouseAreaTouchListener"
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(DEBUG_TAG, "Action was DOWN")
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.d(DEBUG_TAG, "Action was MOVE")
//                  Log.d(DEBUG_TAG, "OnTouch (x,y): (" + event.getX() + ", "  + event.getY())
                    if (event.historySize > 0) {
                        val lastX =
                            event.getHistoricalAxisValue(MotionEvent.AXIS_X, 0)
                        val lastY =
                            event.getHistoricalAxisValue(MotionEvent.AXIS_Y, 0)
                        val newX = event.getX()
                        val newY = event.getY()
                        val deltaX = (newX - lastX).roundToInt();
                        val deltaY = (newY - lastY).roundToInt()
                        Log.d(DEBUG_TAG, "LastX = ${lastX} and lastY = ${lastY} ")
                        Log.d(DEBUG_TAG, "NewX = ${newX} and newY = ${newY} ")
                        Log.d(
                            DEBUG_TAG,
                            "Delta (x,y): (" + deltaX + ", " + deltaY + ")"
                        )
                        mouseController.moveMouse(deltaX, deltaY);
                        Thread.sleep(30);
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(DEBUG_TAG, "Action was UP")
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    Log.d(DEBUG_TAG, "Action was CANCEL")
                    true
                }
                MotionEvent.ACTION_OUTSIDE -> {
                    Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element")
                    true
                }
                else -> super.onTouchEvent(event)
            }

        }

    }


    /** Add custom action to volume keys */
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

    /** Add custom action to volume keys */
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


    /** This error listener is used for the HTTP requests
     * It parses the error message out of the error and displays it on a textView for now
     * */
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
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
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
