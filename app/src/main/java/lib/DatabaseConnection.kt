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
        params.put("description", productDetails[2])
        params.put("source", productDetails[3])
        //change this to the actual database
        val apiCall = ApiCall("gpm.digiavit.co.uk", params, mainContext, key, localListeners)
        localListeners.addDatabaseChangeListener(listenerLocal)
//        apiCall.setApiChangeListener(dbListener)
    }

    private val listenerLocal = object : DatabaseChangeListener{
        override fun onDatabaseChange(response: JSONObject) {
            if (response["error"] == "false"){
                Log.d("databaseTest", "Product has been added into the database")
            }else{
                Log.d("databaseTest", "Error adding product")
            }
        }

    }

}