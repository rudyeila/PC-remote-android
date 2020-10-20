package com.example.pc_remote_android.controllers

import android.util.Log
import com.android.volley.Response
import com.example.pc_remote_android.networking.HTTPClient
import org.json.JSONObject

class MouseController(
    private val httpClient: HTTPClient,
    private val errorListener: Response.ErrorListener
) {

    private val mousePositionRoute = "mouse/position"

    /** Attempts to POST the server and set a new volume value on the computer */
    fun moveMouse(x: Int, y: Int, absolute: Boolean = false) {
        Log.d("moveMouse URL: ", mousePositionRoute)
        val jsonBody = JSONObject()
        jsonBody.put("x", x)
        jsonBody.put("y", y)

        this.httpClient.post(mousePositionRoute, jsonBody, { response ->
            Log.d("Response", response.toString())
        }, errorListener)
    }

}
