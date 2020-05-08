package com.example.aperturedigital

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import apiLib.ApiCall
import apiLib.ApiChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import lib.Encryption
import lib.Listeners
import org.json.JSONArray
import org.json.JSONObject

class LensFragment: Fragment(){

    lateinit var encrypClass: Encryption
    lateinit var apiCall: ApiCall
    val debugTag = "DebugApi"
    private lateinit var currentContext: Context
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_lens, container, false)


//        tescoApiCall("5057373701954")
        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentContext = context
    }

    fun tescoApiCall(barcode: String){
        encrypClass = Encryption()
        val decryptedString = encrypClass.decryptString(getString(R.string.subKey), currentContext)
        val params = HashMap<String, String>()
        params["gtin"] = barcode
        apiCall = ApiCall("dev.tescolabs.com", params, currentContext, decryptedString)
        apiCall.setApiChangeListener(listenerInp)
    }

    private var listenerInp = object: ApiChangeListener {
        override fun onApiChange(apiCall: ApiCall, response: JSONObject) {
            val finalData = getApiData(response)
            var finalString: String = ""
            for (i in 0 until finalData.count()){
                finalString += finalData[i] + "\n" + "\n"
                Log.d(debugTag, finalData[i])
            }
            updateText(finalString)

        }
    }

    fun updateText(responseText: String){
        val barcodeText = TextView(currentContext)
        barcodeText.text = responseText
        (rootView as ViewGroup).addView(barcodeText)
    }

    fun getApiData(response: JSONObject): MutableList<String>{
        val responseMinimal: JSONArray = response["products"] as JSONArray
        var pairs: JSONObject = JSONObject()
        for(i in 0 until responseMinimal.length()){
            pairs = responseMinimal.getJSONObject(i)
        }
        /**
         * gtin -aka barcode num
         * description
         * brand -not so important
         * ingredients
         * productAttributes -> 1 to get to lifestyle
         */
        var finalData: MutableList<String> = mutableListOf()
        finalData.add(pairs["gtin"].toString())
        finalData.add(pairs["description"].toString())
        finalData.add(pairs["brand"].toString())
        finalData.add(pairs["ingredients"].toString())
        val lifeStyle = pairs["productAttributes"] as JSONArray
        var lifeStyleValue: Any
        if (lifeStyle.length() > 1){
            //Tesco api to find the lifestyle value is embedded about 8 times,
            //causing this monstrosity

            lifeStyleValue = lifeStyle[1] as JSONObject
            lifeStyleValue = lifeStyleValue["category"] as JSONArray
            lifeStyleValue = lifeStyleValue[0] as JSONObject
            lifeStyleValue = lifeStyleValue as JSONObject
            if(lifeStyleValue.has("lifestyle")){
                lifeStyleValue = lifeStyleValue["lifestyle"] as JSONArray
                lifeStyleValue = lifeStyleValue[0] as JSONObject
                lifeStyleValue = lifeStyleValue["lifestyle"] as JSONObject
                lifeStyleValue = lifeStyleValue["value"]
            }

            finalData.add(lifeStyleValue.toString())
        }
        return finalData
    }


}