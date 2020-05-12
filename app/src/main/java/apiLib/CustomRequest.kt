package apiLib

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import lib.Listeners
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class CustomRequest(listeners: Listeners) {
    var baseUrl: String = ""
    var params: HashMap<String, String> = hashMapOf()
    lateinit var subKey: String
    val localListener = listeners

    lateinit var requestBody: String

    val BOUNDARY = "AS24adije32MDJHEM9oMaGnKUXtfHq"
    val MULTIPART_FORMDATA = "multipart/form-data;boundary=" + BOUNDARY

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
        }else{
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

    fun requestDatabase(url: String, context: Context){
        val jsonBodyObj = JSONObject()
        jsonBodyObj.put("apiKey", subKey)
        requestBody = jsonBodyObj.toString()
        val queue = Volley.newRequestQueue(context)
//        val data = HashMap<String, String>()
//        data.put("apiKey", subKey)
        //JSONObject(data as Map<*, *>)
        val jsonRequest = object : JsonObjectRequest(Method.POST, url, jsonBodyObj,
            Response.Listener { response ->
                localListener.fireApiChangeListener(response)
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {


//            override fun getParams(): MutableMap<String, String> {
//                val params = HashMap<String, String>()
//                params.put("apiKey", subKey)
//                return params
//            }

//            override fun getBody(): ByteArray? {
//                try {
//                    return requestBody.toByteArray(Charset.forName("utf-8"))
//                } catch (error: UnsupportedEncodingException) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
//                    requestBody, "utf-8");
//                    return null;
//                 }
//            }

            override fun getBodyContentType(): String {
                return MULTIPART_FORMDATA
            }

//            override fun getHeaders(): MutableMap<String, String> {
//                val params: MutableMap<String, String> = HashMap()
//                params["Content-Type"] = "application/x-www-form-urlencoded"
//                return params
//            }
        }
        queue.add(jsonRequest)
    }

    fun request(url: String, context: Context) {
        val queue = Volley.newRequestQueue(context)
        val jsonRequest = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener { response ->
                localListener.fireApiChangeListener(response)
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                if (baseUrl == "dev.tescolabs.com") {
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
