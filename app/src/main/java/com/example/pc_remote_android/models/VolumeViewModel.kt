package com.example.pc_remote_android.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.example.pc_remote_android.networking.HTTPClient
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import kotlin.math.roundToInt

class VolumeViewModel(
    private val httpClient: HTTPClient, private val errorListener: Response.ErrorListener
) : ViewModel() {

    private val currentVolume: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val currentMute: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    private val volumeLevelUrl = "system/volume/level"
    private val muteUrl = "system/volume/mute"

    init {
        // fetch data initially
        sync()
    }

    /** Returns the LiveData object wrapping the volume value */
    fun getCurrentVolume(): LiveData<Int> {
        return currentVolume
    }

    /** Returns the LiveData object wrapping the mute value */
    fun getCurrentMute(): LiveData<Boolean> {
        return currentMute
    }

    /** Syncs the volume model by fetching the latest data from the network*/
    private fun sync() {
        fetchMute()
        fetchVolume()
    }

    private fun fetchVolume()  {
        Log.d("fetchVolume ", volumeLevelUrl)
        httpClient.get(
            volumeLevelUrl,
            { response -> // display response
                Log.d("Response", response.toString())
                val volumeValue = parseVolumeResponse(response)
                currentVolume.value = volumeValue
            }, errorListener
        )
    }


    /** Attempts to POST the server and set a new volume value on the computer */
    fun setVolume(newVolume: Int) {
        Log.d("setVolume URL: ", volumeLevelUrl)
        val jsonBody = JSONObject()
        jsonBody.put("volume", (validateVolumeValue(newVolume) / 100.0))

        this.httpClient.post(volumeLevelUrl, jsonBody, { response ->
            Log.d("Response", response.toString())
            val volumeValue = parseVolumeResponse(response)
            currentVolume.value = volumeValue
        }, errorListener)
    }

    private fun fetchMute() {
        Log.d("fetchMute ", muteUrl)
        httpClient.get(
            muteUrl,
            { response -> // display response
                Log.d("Response", response.toString())
                val muteValue = parseMuteResponse(response)
                currentMute.value = muteValue
            }, errorListener
        )
    }

    /** Attempts to POST the server and set a new mute value on the computer */
    fun setMute(newMute: Boolean) {
        Log.d("setMute URL: ", muteUrl)
        val jsonBody = JSONObject()
        jsonBody.put("mute", newMute)

        this.httpClient.post(muteUrl, jsonBody, { response ->
            Log.d("Response", response.toString())
            val newMuteValue = parseMuteResponse(response)
            currentMute.value = newMuteValue
        }, errorListener)
    }

    fun toggleMute() {
        Log.d("toggleMute", "Toggle mute ")
        val oldMute = getCurrentMute().value
        val newMute = !(oldMute)!!
        setMute(newMute)
    }

    /** Attempts to POST the server and increase the current volume value by a specific offset, which defaults to 10. */
    fun increaseVolume(offset: Int = 10, tensOnly: Boolean = true) {
        var oldValue = currentVolume.value

        if (oldValue == null) {
            fetchVolume()
            return
        }

        // round to nearest 10
        if (tensOnly) {
            oldValue = (oldValue / 10.0).roundToInt() * 10
        }

        val newValue = validateVolumeValue(oldValue + offset)

        // set new value
        setVolume(newValue)
    }

    /** Attempts to POST the server and decrease the current volume value by a specific offset, which defaults to 10. */
    fun decreaseVolume(offset: Int = 10, tensOnly: Boolean = true) {
        var oldValue = currentVolume.value

        if (oldValue == null) {
            fetchVolume()
            return
        }

        // round to nearest 10
        if (tensOnly) {
            oldValue = (oldValue / 10.0).roundToInt() * 10
        }

        val newValue = validateVolumeValue(oldValue - offset)

        // set new value
        setVolume(newValue)
    }

    private fun parseMuteResponse(response: JSONObject): Boolean? {
        try {
            return response.getBoolean("mute")
        } catch (err: JSONException) {
            Log.e(
                "fetchMute",
                "JSONException: ".plus("A JSON parsing error occurred while retrieving the mute status over the network")
            )
        }
        return null
    }

    private fun validateVolumeValue(newValue: Int) : Int {
        var newVolume = newValue
        if (newVolume < 0) {
            newVolume = 0
        } else if (newVolume > 100) {
            newVolume = 100
        }
        return newVolume
    }

    private fun parseVolumeResponse(response: JSONObject): Int? {
        try {
            return (response.getDouble("volume") * 100).roundToInt()
        } catch (err: IllegalArgumentException) {
            Log.e(
                "fetchVolume",
                "Illegal Argument: ".plus("The returned value is not a number.")
            )
        }
        catch (err: JSONException) {
            Log.e(
                "fetchVolume",
                "JSONException: ".plus("A JSON parsing error occurred while retrieving volume level over the network")
            )
        } catch (err: Exception) {
            Log.e(
                "fetchVolume",
                "General Error: ".plus("An error occurred while retrieving volume data.")
            )
        }
        return null
    }

}
