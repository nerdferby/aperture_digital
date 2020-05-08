package com.example.aperturedigital

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import apiLib.ApiCall
import apiLib.ApiChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import lib.Encryption
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    /**
     * For Critical version(V.1)
     * TODO: Tidy up Api connection
     * TODO: Get modules working instead of Directories
     * TODO: Fix the return of the api to allow for every product return
     * TODO: Connect to a database so it reduces the amount of api calls
     * TODO: Barcode scanner
     * TODO: Vegan determine algorithm
     */

    lateinit var apiCall: ApiCall
    val debugTag = "DebugApi"

    lateinit var encrypClass: Encryption

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnNavigationItemSelectedListener(navListener)

        supportFragmentManager.beginTransaction().replace(R.id.constraintLayoutMainContent,
        LensFragment()).commit()
//        tescoApiCall("5057373701954") //TODO: change to barcode scan return later
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener(object: (MenuItem) ->
    Boolean {
        override fun invoke(item: MenuItem): Boolean {
            var selectedFragment: Fragment? = null
            when(item.itemId){
                R.id.nav_history -> {
                    selectedFragment = HistoryFragment()
                }
                R.id.nav_lens -> {
                    selectedFragment = LensFragment()
                }
                R.id.nav_news -> {
                    selectedFragment = NewsFragment()
                }
                R.id.nav_search -> {
                    selectedFragment = SearchFragment()
                }
                R.id.nav_settings -> {
                    selectedFragment = SettingsFragment()
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.constraintLayoutMainContent,
                selectedFragment!!).commit()
            return true
        }
    })

    fun tescoApiCall(barcode: String){
        encrypClass = Encryption()

        val decryptedString = encrypClass.decryptString(getString(R.string.subKey), this)

        val params = HashMap<String, String>()
        val barcodeNum = barcode

        params.put("gtin", barcodeNum)
        apiCall = ApiCall("dev.tescolabs.com", params, this, decryptedString)
        apiCall.setApiChangeListener(listenerInp)

    }

    private var listenerInp = object: ApiChangeListener {
        override fun onApiChange(apiCall: ApiCall, response: JSONObject) {
            val finalData = getApiData(response)
            var finalString: String = ""
            for (i in 0 until finalData.count()){
                finalString += finalData[i] + "\n" + "\n"
                Log.d(debugTag, finalData[i])
            }
            updateText(finalString)
//            val text: TextView = findViewById(R.id.textView)
//            text.text = finalString
        }
    }

    fun updateText(responseText: String){
        val barcodeText = TextView(applicationContext)
        barcodeText.text = responseText
        constraintLayoutMainContent.addView(barcodeText)
    }

    fun getApiData(response: JSONObject): MutableList<String>{
        val responseMinimal: JSONArray = response["products"] as JSONArray
        var pairs: JSONObject = JSONObject()
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
