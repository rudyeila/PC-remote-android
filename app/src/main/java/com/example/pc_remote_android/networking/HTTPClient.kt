package com.example.pc_remote_android.networking

import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class HTTPClient(private val baseUrl: String, private val queue: RequestQueue) {

    fun get(
        resourceUrl: String,
        responseListener: Response.Listener<org.json.JSONObject>?,
        errorListener: Response.ErrorListener?
    ) {
        val url: String = baseUrl.plus(resourceUrl)
        Log.d("GET", resourceUrl)
        Log.d("GET", url)

        val getRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response -> // display response
                Log.d("Response", response.toString())
                responseListener?.onResponse(response)
            }, errorListener
        )

        // add it to the RequestQueue
        queue.add(getRequest)
    }


    fun post(
        resourceUrl: String,
        jsonObject: JSONObject?,
        responseListener: Response.Listener<org.json.JSONObject>?,
        errorListener: Response.ErrorListener?
    ) {
        val url: String = baseUrl.plus(resourceUrl)
        Log.d("POST", resourceUrl)
        Log.d("POST", url)
        Log.d("JSON", jsonObject.toString())

        val postRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response -> // display response
                Log.d("Response", response.toString())
                responseListener?.onResponse(response)
            }, errorListener
        )
        // add it to the RequestQueue
        queue.add(postRequest)
    }

}
