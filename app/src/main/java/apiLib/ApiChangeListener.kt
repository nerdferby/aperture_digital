package apiLib

import org.json.JSONObject

interface ApiChangeListener {
    fun onApiChange(apiCall: ApiCall, response: JSONObject)
}