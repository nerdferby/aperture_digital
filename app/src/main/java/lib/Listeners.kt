package lib

import org.json.JSONObject

class Listeners {
    private val onApiChangeListeners = mutableListOf<ApiChangeListener>()
    private val onBarcodeChangeListener = mutableListOf<BarcodeChangeListener>()

    fun addApiChangeListener(listener: ApiChangeListener){
        onApiChangeListeners.add(listener)
    }

    fun fireApiChangeListener(response: JSONObject){
        onApiChangeListeners.forEach{
            it.onApiChange(response)
        }
    }

    fun addBarcodeChangeListener(listener: BarcodeChangeListener){
        onBarcodeChangeListener.add(listener)
    }

    fun fireBarcodeChange(response: String){
        onBarcodeChangeListener.forEach{
            it.onBarcode(response)
        }
    }
}


interface ApiChangeListener {
    fun onApiChange(response: JSONObject)
}

interface BarcodeChangeListener {
    fun onBarcode(response: String)
}