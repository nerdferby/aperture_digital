package apiLib

import android.content.Context
import lib.Listeners
import org.json.JSONObject

class ApiCall(baseUrl: String, paramsPassed: HashMap<String, String>, context: Context, subKey: String, listenerClass: Listeners, vegan: Boolean=false) {
    private val listenerClassLocal = listenerClass
    var request: CustomRequest = CustomRequest(listenerClassLocal, context)
    private val key: String = subKey
    private val activityContext: Context = context
    private val url: String = baseUrl
    private val prams: HashMap<String, String> = paramsPassed
    init {
        if (baseUrl == "gpm.digiavit.co.uk" && !vegan){
            databaseRequest()
        }else if(baseUrl == "gpm.digiavit.co.uk" && vegan){
            veganRequest()
        }
        else{
            apiRequest()
        }
    }

    private fun apiRequest(){
        request.baseUrl = url
        request.subKey = key
        request.paramsFromCall = prams
        val url = request.buildUrl()
        request.request(url, activityContext)
    }

    private fun databaseRequest(){
        request.baseUrl = url
        request.subKey = key
        request.paramsFromCall = prams
        val url = request.buildUrl()
        if (prams.contains("keypairRequest")){
            request.requestDatabaseKeys(url, activityContext)
        }else{
            request.requestDatabase(url, activityContext)
        }
    }
    private fun veganRequest(){
        request.baseUrl = url
        request.subKey = key
        request.paramsFromCall = prams
        val url = request.buildUrl()

        request.requestVeganDatabase(url, activityContext)

    }
}