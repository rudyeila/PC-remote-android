package com.example.pc_remote_android.networking

import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class HTTPClient(private val baseUrl: String, private val queue: RequestQueue) {

    /** A generic function that makes a GET request to the specified resource URL */
    fun get(
        /** The URL to the required resource. Will be appeneded to the baseURL that is passed in the constructor.
         * Make sure that that this resource URL does NOT start with a slash '/'
         * */
        resourceUrl: String,

        /** The response handler - pass null if you don't want to handle the response. In that case it will be simply logged as a debug message to the logcat. */
        responseListener: Response.Listener<org.json.JSONObject>?,

        /** The error handler - pass null if you don't want to handle the errors. */
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

    /** A generic function that makes a POST request to the specified resource URL. Optionally takes posts a JSON body. */
    fun post(
        /** The URL to the required resource. Will be appeneded to the baseURL that is passed in the constructor.
         * Make sure that that this resource URL does NOT start with a slash '/'
         * */
        resourceUrl: String,

        /** The JSON body of the request. Pass null if you don't want to pass a body. */
        jsonObject: JSONObject?,

        /** The response handler - pass null if you don't want to handle the response. In that case it will be simply logged as a debug message to the logcat. */
        responseListener: Response.Listener<org.json.JSONObject>?,

        /** The error handler - pass null if you don't want to handle the errors. */
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
