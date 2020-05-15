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
        currentBarcode = "5057373701954"
        //5057373701954
        //05050179607031
        //5057545618332
        scannerBtn= (rootView as ViewGroup).findViewById<Button>(R.id.startBarcodeScannerBtn)
        scannerBtn.setOnClickListener {
            this.childFragmentManager.beginTransaction().replace(R.id.constraintLayoutContent, barcodeFragmentLocal).commit()
            scannerBtn.visibility = View.INVISIBLE
//            checkDb(currentBarcode)
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

    private fun apiCalls(barcode: String) {
        if (this.childFragmentManager.fragments.size > 0){
            this.childFragmentManager.beginTransaction().remove(
                this.childFragmentManager.fragments[0]).commit()
        }
        checkApis(barcode)
//        scannerBtn.visibility = View.VISIBLE
    }

    private fun checkDb(gtin: String){
        val db = DatabaseConnection(context as Context, databaseListener, listenerClass, (context as Context).getString(R.string.databaseApiKey))
        db.getCertainProduct(gtin)
    }

    private fun checkApis(barcode: String){
        /**
         * This goes through the database then the apis to check for the product.
         * If none is found then return that no product was found
         */
        if(productCheckIndex == 0){
            checkDb(barcode)
        }else if(productCheckIndex == 1){
            openFoodApi(barcode)
        }else if(productCheckIndex == 2) {
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

    private fun addFoundProductDb(productDetails: MutableList<String>){
        val db = DatabaseConnection(context as Context, databaseListenerInsert, listenerClass, (context as Context).getString(R.string.databaseApiKey))
        db.addProduct(productDetails)
    }

    private var databaseListenerInsert = object: DatabaseChangeListener{
        override fun onDatabaseChange(response: JSONObject) {
            if (response["error"] == "false"){
                Log.d("databaseTest", "Product has been added into the database")
            }else{
                Log.d("databaseTest", "Error adding product")
            }
        }
    }




    private var databaseListener = object: DatabaseChangeListener {
        override fun onDatabaseChange(response: JSONObject) {
            Log.d("test", response.toString())
            var finalData = mutableListOf<String>()
            var localPassed = false

            if(response["error"] == true){
                //no product found
                productCheckIndex++
                checkApis(currentBarcode)
            }else{
                //get product from the response
                finalData = getProductFromDb(response)
                localPassed = true
            }

            if(localPassed){
                var finalString: String = ""
                for (i in 0 until finalData.count()){
                    finalString += finalData[i] + "\n" + "\n"
                    Log.d(constantClass.DEBUGTAG, finalData[i])
                }
                finalString += "\nDatabase"
                updateText(finalString)
            }

        }
    }

    private var listenerInp = object: lib.ApiChangeListener {
        override fun onApiChange(response: JSONObject) {
            var finalData = mutableListOf<String>()
            var localPassed = false
            if (response.has("products")){
                if (response["products"].toString() == "[]"){
                    productCheckIndex++
                    checkApis(currentBarcode)
                }else{
                    finalData = getApiData(response)
                    if (finalData[0] == ""){
                        productCheckIndex++
                        localPassed = false
                        checkApis(currentBarcode)
                    }else{
                        localPassed = true
                    }
                }
            }else{
                if (response.has("product")){
                    if (!(response["product"] as JSONObject).has("product_name")){
                        //product was not found
                        productCheckIndex++
                        checkApis(currentBarcode)
                    }else{
                        finalData = getWorldFoodApiData(response)
                        if (finalData[0] == ""){
                            productCheckIndex++
                            localPassed = false
                            checkApis(currentBarcode)
                        }else{
                            localPassed = true
                        }
                    }
                }else{
                    productCheckIndex++
                    localPassed = false
                    checkApis(currentBarcode)
                }

            }
            if(localPassed){
                /**
                 * If a product is found then add it to the database if it is not in there already
                 */
                var finalString: String = ""
                for (i in 0 until finalData.count()){
                    finalString += finalData[i] + "\n" + "\n"
                    Log.d(constantClass.DEBUGTAG, finalData[i])
                }
                finalString += "\nAPI"
                updateText(finalString)
                addFoundProductDb(mutableListOf(currentBarcode, finalData[0], "", finalData[1], finalData[2]))

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

    private fun getProductFromDb(response: JSONObject): MutableList<String>{
        val product = response["products"] as JSONObject
        val barcode = product["barcode"].toString()
        val name = product["name"].toString()
        val description = product["description"].toString()
        val source = product["source"].toString()

        return mutableListOf(barcode, name, description, source)
    }

    fun getWorldFoodApiData(response: JSONObject): MutableList<String>{
        val responseMinimal: JSONObject = response.get("product") as JSONObject
        if (responseMinimal.has("ingredients_text_en")){
            //currentBarcode
            //product_name
            //no description
            //brands
            //ingredients_text_en
            val name = responseMinimal["product_name"].toString()
            val brand = "World Food Api"
            val ingredients = responseMinimal["ingredients_text_en"].toString()

            //TODO(change Tesco to actual brand later)
//            addFoundProductDb(mutableListOf(name, ingredients, "Tesco"))
            return mutableListOf(name, brand, ingredients)
        }else{
            return mutableListOf("")
        }
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

        //add if no ingredients then no product found
        var finalData: MutableList<String> = mutableListOf()
        if (pairs.has("ingredients")){
            finalData.add(pairs["description"].toString()) // name
            finalData.add("Tesco API")
            finalData.add(pairs["ingredients"].toString())
        }else{
            finalData.add("")
        }

        //TODO(change Tesco to actual brand later)
//        addFoundProductDb(mutableListOf(currentBarcode, pairs["description"].toString(),
//            pairs["ingredients"].toString(), "Tesco"))
        return finalData
    }
}
