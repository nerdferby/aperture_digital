package apiLib

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import lib.Listeners
import org.json.JSONObject
import java.lang.Exception


class CustomRequest(listeners: Listeners) {
    var baseUrl: String = ""
    var paramsFromCall: HashMap<String, String> = hashMapOf()
    lateinit var subKey: String
    val localListener = listeners

    lateinit var requestBody: String

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
                .appendEncodedPath(paramsFromCall["gtin"])
        }else{
            builder.scheme("https")
                .authority(baseUrl)
        }

        for ((k, v) in paramsFromCall) {
            if (gtinNeeded){
                builder.appendQueryParameter(k, v)
            }
        }
        return builder.build().toString()
    }

    fun requestDatabase(url: String, context: Context){
        /**
         * To do a post request it needs to be a string request because JsonObject does not work
         * for some reason.
         */
        val jsonBodyObj = JSONObject()
        jsonBodyObj.put("apiKey", subKey)
        requestBody = jsonBodyObj.toString()
        val queue = Volley.newRequestQueue(context)
        val jsonRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                try {
                    val convertedObject: JSONObject = JSONObject(response)
                    localListener.fireDatabaseChangeListener(convertedObject)
                }catch (e: Exception){
                    val response = JSONObject()
                    response.put("error", true)
                    localListener.fireDatabaseInsertListener(response)
                }
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {

            override fun getParams(): MutableMap<String, String> {
                val localParams = HashMap<String, String>()
                localParams.put("apiKey", subKey)
                if (paramsFromCall.contains("name")){
                    localParams.put("name", paramsFromCall["name"] as String)
                }
                if (paramsFromCall.count() > 2){
                    localParams.put("barcode", paramsFromCall["barcode"] as String)
                    localParams.put("name", paramsFromCall["name"] as String)
                    localParams.put("description", paramsFromCall["description"] as String)
                    localParams.put("source", paramsFromCall["source"] as String)
                    localParams.put("ingredients", paramsFromCall["ingredients"] as String)

                }

                return localParams
            }
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
