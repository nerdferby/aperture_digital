package apiLib

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import lib.Listeners

class CustomRequest(listeners: Listeners) {
    var baseUrl: String = ""
    var params: HashMap<String, String> = hashMapOf()
    lateinit var subKey: String
    val localListener = listeners

    fun buildUrl(): String {
        val builder = Uri.Builder()
        var gtinNeeded = true
        if (baseUrl == "dev.tescolabs.com"){
            builder.scheme("https")
                .authority(baseUrl)
                .appendEncodedPath("product/")
        }else if(baseUrl == "world.openfoodfacts.org"){
            gtinNeeded = false
            builder.scheme("https")
                .authority(baseUrl)
                .appendEncodedPath("api")
                .appendEncodedPath("v0")
                .appendEncodedPath("product/")
                .appendEncodedPath(params["gtin"])
        }

        else{
            builder.scheme("https")
                .authority(baseUrl)
        }

        for ((k, v) in params) {
            if (gtinNeeded){
                builder.appendQueryParameter(k, v)
            }
        }
        return builder.build().toString()
    }

    fun request(url: String, context: Context){
        val queue = Volley.newRequestQueue(context)

        val jsonRequest = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener { response ->
                localListener.fireApiChangeListener(response)
            },
            Response.ErrorListener {error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                if (subKey != ""){
                    val headers = HashMap<String, String>()
                    headers["Ocp-Apim-Subscription-Key"] = subKey
                    return headers
                }
                return HashMap()
            }
        }

        queue.add(jsonRequest)
    }
}
