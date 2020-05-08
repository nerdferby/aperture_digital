package lib

import org.json.JSONObject

class Listeners {
    private val onApiChangeListeners = mutableListOf<ApiChangeListener>()

    fun addApiChangeListener(listener: ApiChangeListener){
        onApiChangeListeners.add(listener)
    }

    fun fireApiChangeListener(response: JSONObject){
        onApiChangeListeners.forEach{
            it.onApiChange(response)
        }
    }
}


interface ApiChangeListener {
    fun onApiChange(response: JSONObject)
}
