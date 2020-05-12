package fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
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
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.fragment_lens.*
import lib.*
import org.json.JSONArray
import org.json.JSONObject

class LensFragment: Fragment(){

    lateinit var encrypClass: Encryption
    lateinit var apiCall: ApiCall
    private lateinit var currentContext: Context
    private lateinit var rootView: View
    val listenerClass = Listeners()
    val constantClass = Constants()
    var currentBarcode = ""
    private var productCheckIndex = 0
    val barcodeFragmentLocal = BarcodeFragment(listenerClass)
    lateinit var scannerBtn: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_lens, container, false)
        currentBarcode = "05050179607031"
        //5057373701954
        scannerBtn= (rootView as ViewGroup).findViewById<Button>(R.id.startBarcodeScannerBtn)
        scannerBtn.setOnClickListener {
//            this.childFragmentManager.beginTransaction().replace(R.id.constraintLayoutContent, barcodeFragmentLocal).commit()
//            scannerBtn.visibility = View.INVISIBLE
            checkDb(currentBarcode)
//            checkApis(currentBarcode)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings = ImplementSettings(context as Context)
        settings.changeToPreference(constraintLayoutContent, "Lens")
        settings.changeTextColor(constraintLayoutContent)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentContext = context
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (data != null){
                currentBarcode = data!!.getStringExtra("gtin")
                requireActivity().runOnUiThread{
                    //change to the actual api calls
                    apiCalls(currentBarcode)
                }
            }
        }
    }

    private fun checkDb(gtin: String){
        val db = DatabaseConnection(context as Context, databaseListener, listenerClass, (context as Context).getString(R.string.databaseApiKey))
        db.getCertainProduct(gtin)
//        if (db.returnProduct())
    }

    private fun apiCalls(barcode: String) {
        if (this.childFragmentManager.fragments.size > 0){
            this.childFragmentManager.beginTransaction().remove(
                this.childFragmentManager.fragments[0]).commit()
        }
        updateText(barcode)
//        scannerBtn.visibility = View.VISIBLE
    }

    private var databaseListener = object: ApiChangeListener {
        override fun onApiChange(response: JSONObject) {
            Log.d("test", response.toString())
            if (response["error"] == true){
//                Send no product found
            }else{
                //get product from the response
                //getProduct(response)
            }
        }
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
        rootView.invalidate()
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
