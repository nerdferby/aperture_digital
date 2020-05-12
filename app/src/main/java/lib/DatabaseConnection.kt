package lib

import android.content.Context
import apiLib.ApiCall


class DatabaseConnection(context: Context, listener: ApiChangeListener, listeners: Listeners, apiKey: String) {
    var mainContext: Context = context
    val dbListener = listener
    val localListeners = listeners
    val key = apiKey

    fun getCertainProduct(gtin: String){
        val params = HashMap<String, String>()

        params.put("op", "getCertainProduct")
        params.put("gtin", gtin)

        val apiCall = ApiCall("runningmonsters.co.uk", params, mainContext, key, localListeners)
        localListeners.addApiChangeListener(dbListener)
//        apiCall.setApiChangeListener(dbListener)
    }

}