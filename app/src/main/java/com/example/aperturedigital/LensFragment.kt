package com.example.aperturedigital

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import apiLib.ApiCall
import lib.Constants
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
    val listenerClass = Listeners()
    val constantClass = Constants()
    var currentBarcode = ""
    private var productCheckIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_lens, container, false)
        currentBarcode = "5057373701954"
        //5057373701954
        val scannerBtn = (rootView as ViewGroup).findViewById<Button>(R.id.startBarcodeScannerBtn)
        scannerBtn.setOnClickListener {
            //start barcode scanner
            checkApis(currentBarcode)

        }
        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentContext = context
    }

    private fun checkApis(barcode: String){
        //this checks through both apis
        if(productCheckIndex == 0){
            openFoodApi(barcode)
        }else if(productCheckIndex == 1){
            tescoApiCall(barcode)
        }else{
            updateText("No product was found")
        }

    }

    private fun openFoodApi(barcode: String){
        val params = HashMap<String, String>()
        params["gtin"] = barcode
        apiCall = ApiCall("world.openfoodfacts.org", params, currentContext, "", listenerClass)
        listenerClass.addApiChangeListener(listenerInp)

    }

    fun tescoApiCall(barcode: String){
        encrypClass = Encryption()
        val decryptedString = encrypClass.decryptString(getString(R.string.subKey), currentContext)
        val params = HashMap<String, String>()
        params["gtin"] = barcode
        apiCall = ApiCall("dev.tescolabs.com", params, currentContext, decryptedString, listenerClass)
//        apiCall.setApiChangeListener(listenerInp)
        listenerClass.addApiChangeListener(listenerInp)
    }

    private var listenerInp = object: lib.ApiChangeListener {
        override fun onApiChange(response: JSONObject) {
            var responseLocation = ""
            var finalData = mutableListOf<String>()
            var localPassed = false
            if (response.has("products")){
                if (response["products"].toString() == "[]"){
                    productCheckIndex++
                    checkApis(currentBarcode)
                }else{
                    finalData = getApiData(response)
                    responseLocation = "Tesco"
                }
            }else{
                if (!(response["product"] as JSONObject).has("product_name")){
                    //product was not found
                    productCheckIndex++
                    checkApis(currentBarcode)
                }else{
                    localPassed = true
                    finalData = getWorldFoodApiData(response)
                    responseLocation = "World"

                }
            }
            if(localPassed){
                var finalString: String = ""
                for (i in 0 until finalData.count()){
                    finalString += finalData[i] + "\n" + "\n"
                    Log.d(constantClass.DEBUGTAG, finalData[i])
                }
                updateText(finalString)
            }
        }
    }

    fun updateText(responseText: String){
        val barcodeText = TextView(currentContext)
        barcodeText.text = responseText
        barcodeText.textSize = 20f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (barcodeText.id == -1){
                barcodeText.id = View.generateViewId()
            }
        }
        (rootView as ViewGroup).removeAllViews()
        (rootView as ViewGroup).addView(barcodeText)
        barcodeText.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

        val set = ConstraintSet()
        set.clone((rootView as ViewGroup).findViewById<ConstraintLayout>(R.id.constraintLayoutContent))
        set.connect(barcodeText.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        set.connect(
            barcodeText.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        set.connect(
            barcodeText.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )
        set.connect(
            barcodeText.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )

        set.applyTo((rootView as ViewGroup).findViewById(R.id.constraintLayoutContent))
    }

    fun getWorldFoodApiData(response: JSONObject): MutableList<String>{
        val responseMinimal: JSONObject = response.get("product") as JSONObject
        val name = responseMinimal["product_name"].toString()
        val ingredients = responseMinimal["ingredients_text_en"].toString()
        val keyWords = responseMinimal["_keywords"].toString()
        return mutableListOf(name, ingredients, keyWords)
    }

    fun getApiData(response: JSONObject): MutableList<String>{
        val responseMinimal: JSONArray = response["products"] as JSONArray
        var pairs = JSONObject()
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
