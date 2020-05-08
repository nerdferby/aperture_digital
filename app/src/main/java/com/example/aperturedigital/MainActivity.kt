package com.example.aperturedigital

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import apiLib.ApiCall
import apiLib.ApiChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import lib.Encryption
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    /**
     * For Critical version(V.1)
     * TODO: Tidy up Api connection
     * TODO: Get modules working instead of Directories
     * TODO: Fix the return of the api to allow for every product return
     * TODO: Connect to a database so it reduces the amount of api calls
     * TODO: Barcode scanner
     * TODO: Vegan determine algorithm
     */

    lateinit var apiCall: ApiCall
    val debugTag = "DebugApi"

    lateinit var encrypClass: Encryption

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        tescoApiCall("5057373701954") //TODO: change to barcode scan return later
        val lens = findViewById<ImageButton>(R.id.LensBtn)
        lens.setOnClickListener {
            Log.d("test", "Lens Clicked")
        }
    }

    fun tescoApiCall(barcode: String){
        encrypClass = Encryption()

        val decryptedString = encrypClass.decryptString(getString(R.string.subKey), this)

        val params = HashMap<String, String>()
        val barcodeNum = barcode

        params.put("gtin", barcodeNum)
        apiCall = ApiCall("dev.tescolabs.com", params, this, decryptedString)
        apiCall.setApiChangeListener(listenerInp)

    }

    private var listenerInp = object: ApiChangeListener {
        override fun onApiChange(apiCall: ApiCall, response: JSONObject) {
            val finalData = getApidata(response)
            var finalString: String = ""
            for (i in 0 until finalData.count()){
                finalString += finalData[i] + "\n" + "\n"
                Log.d(debugTag, finalData[i])
            }
            updateText(finalString)
//            val text: TextView = findViewById(R.id.textView)
//            text.text = finalString
        }
    }

    fun updateText(responseText: String){
        val barcodeText = TextView(applicationContext)
        barcodeText.text = responseText
        constraintLayout2.addView(barcodeText)
    }

    fun getApidata(response: JSONObject): MutableList<String>{
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
        var lifeStylevalue: Any
        if (lifeStyle.length() > 1){
            //Tesco api to find the lifestyle value is embedded about 8 times,
            //causing this monstrosity

            lifeStylevalue = lifeStyle[1] as JSONObject
            lifeStylevalue = lifeStylevalue["category"] as JSONArray
            lifeStylevalue = lifeStylevalue[0] as JSONObject
            lifeStylevalue = lifeStylevalue as JSONObject
            if(lifeStylevalue.has("lifestyle")){
                lifeStylevalue = lifeStylevalue["lifestyle"] as JSONArray
                lifeStylevalue = lifeStylevalue[0] as JSONObject
                lifeStylevalue = lifeStylevalue["lifestyle"] as JSONObject
                lifeStylevalue = lifeStylevalue["value"]
            }


            finalData.add(lifeStylevalue.toString())
        }
        return finalData
    }
}
