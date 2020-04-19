package apiLib

import android.content.Context
import org.json.JSONObject

class ApiCall(baseUrl: String, paramsPassed: HashMap<String, String>, context: Context, subKey: String) {
    var request: CustomRequest = CustomRequest(this)
    private val key: String = subKey
    private val activityContext: Context = context
    private val url: String = baseUrl
    private val prams: HashMap<String, String> = paramsPassed

    var onApiChangeListener: ApiChangeListener? = null

    init {
        apiRequest()
    }

    private fun apiRequest(){
        request.baseUrl = url
        request.subKey = key
        request.params = prams
        val url = request.buildUrl()
        request.request(url, activityContext)
    }

    fun setApiChangeListener(listenerImp: ApiChangeListener){
        onApiChangeListener = listenerImp
    }

    fun fireApiChange(response: JSONObject){
        onApiChangeListener?.onApiChange(this, response)
    }
}