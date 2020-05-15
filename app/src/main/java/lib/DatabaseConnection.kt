package lib

import android.content.Context
import android.util.Log
import apiLib.ApiCall
import org.json.JSONObject

class DatabaseConnection(context: Context, listener: DatabaseChangeListener, listeners: Listeners, apiKey: String) {
    var mainContext: Context = context
    val dbListener = listener
    val localListeners = listeners
    val key = apiKey

    fun getCertainProduct(gtin: String){
        val params = HashMap<String, String>()

        params.put("op", "getCertainProduct")
        params.put("barcode", gtin)
        //change this to the actual database
        val apiCall = ApiCall("gpm.digiavit.co.uk", params, mainContext, key, localListeners)
        localListeners.addDatabaseChangeListener(dbListener)
//        apiCall.setApiChangeListener(dbListener)
    }

    fun addProduct(productDetails: MutableList<String>){
        val params = HashMap<String, String>()

        params.put("op", "addNewProduct")
        params.put("barcode", productDetails[0])
        params.put("name", productDetails[1])
        params.put("description", "")
        params.put("source", productDetails[3])
        params.put("ingredients", productDetails[4])
        //change this to the actual database
        val apiCall = ApiCall("gpm.digiavit.co.uk", params, mainContext, key, localListeners)
        localListeners.addDatabaseInsertListener(dbListener)
//        apiCall.setApiChangeListener(dbListener)
    }

}