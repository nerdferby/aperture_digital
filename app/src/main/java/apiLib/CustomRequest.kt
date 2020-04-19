package apiLib

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CustomRequest(apiCall: ApiCall) {
    var baseUrl: String = ""
    var params: HashMap<String, String> = hashMapOf()
    val localApiCall: ApiCall = apiCall
    lateinit var subKey: String

    fun buildUrl(): String {
        val builder = Uri.Builder()
        builder.scheme("https")
            .authority(baseUrl)
            .appendEncodedPath("product/")
        for ((k, v) in params) {
            builder.appendQueryParameter(k, v)
        }
        return builder.build().toString()
    }

    fun request(url: String, context: Context){
        val queue = Volley.newRequestQueue(context)

        val jsonRequest = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener<JSONObject> {response ->
                localApiCall.fireApiChange(response)
            },
            Response.ErrorListener {error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Ocp-Apim-Subscription-Key"] = subKey
                return headers
            }
        }

        queue.add(jsonRequest)
    }
}
