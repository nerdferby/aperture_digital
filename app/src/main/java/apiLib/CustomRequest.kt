package apiLib

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.aperturedigital.R
import lib.Listeners
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class CustomRequest(listeners: Listeners, context: Context) {
    var baseUrl: String = ""
    var paramsFromCall: HashMap<String, String> = hashMapOf()
    lateinit var subKey: String
    val localListener = listeners
    val appContext = context

    lateinit var requestBody: String

    fun buildUrl(): String {
        val builder = Uri.Builder()
        var gtinNeeded = true
        if (baseUrl == "dev.tescolabs.com"){
            builder.scheme("https")
                .authority(baseUrl)
                .appendEncodedPath("product/")
        }else if(baseUrl == "world.openfoodfacts.org"){
            gtinNeeded = false
            builder.scheme("https")
                .authority(baseUrl)
                .appendEncodedPath("api")
                .appendEncodedPath("v0")
                .appendEncodedPath("product/")
                .appendEncodedPath(paramsFromCall["gtin"])
        }else{
            builder.scheme("https")
                .authority(baseUrl)
        }

        for ((k, v) in paramsFromCall) {
            if (gtinNeeded){
                builder.appendQueryParameter(k, v)
            }
        }
        return builder.build().toString()
    }

    fun requestDatabase(url: String, context: Context){
        /**
         * To do a post request it needs to be a string request because JsonObject does not work
         * for some reason.
         */
        val jsonBodyObj = JSONObject()
        jsonBodyObj.put("apiKey", subKey)
        requestBody = jsonBodyObj.toString()
        val queue = Volley.newRequestQueue(context)
        val jsonRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                try {
                    val convertedObject: JSONObject = JSONObject(response)
                    localListener.fireDatabaseChangeListener(convertedObject)
                }catch (e: Exception){
                    val response = JSONObject()
                    response.put("error", true)
                    localListener.fireDatabaseInsertListener(response)
                }
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {

            @RequiresApi(Build.VERSION_CODES.M)
            override fun getParams(): MutableMap<String, String> {
                val localParams = HashMap<String, String>()
                val keypair = generateKeyPair()
                val data = HashMap<String, String>()
                val r = SecureRandom()
                val iv = ByteArray(16)
                r.nextBytes(iv)
                data.put("apiKey", subKey)
                paramsFromCall.forEach{
                    data.put(it.key, it.value)
                }


                val kgen: KeyGenerator = KeyGenerator.getInstance("AES")
                kgen.init(128) //set keysize, can be 128, 192, and 256
                val key = kgen.generateKey()
                val prefs: SharedPreferences =
                    (context as Context).getSharedPreferences("publicKey", Context.MODE_PRIVATE)
                if (prefs.getString("publicKey", "") == ""){
                    prefs.edit().putString("publicKey", encodeToString(keypair.public.encoded, DEFAULT)).commit()
                }

                localParams.put("data", data.toString())
                val encryptText: ByteArray = localParams.toString().toByteArray(Charsets.UTF_8)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

                cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
                val ciphertext: ByteArray = cipher.doFinal(encryptText)

                val dataFinal = hashMapOf<String, String>()
                val publicKey = context.getString(R.string.publicKey)

                val decodedKey: ByteArray = Base64.decode(prefs.getString("publicKey", ""), DEFAULT)
                val keySpec =
                    X509EncodedKeySpec(decodedKey)
                val keyFactory = KeyFactory.getInstance("RSA")
                val pubKey = keyFactory.generatePublic(keySpec)

                dataFinal.put("data", Base64.encodeToString(ciphertext, DEFAULT))
                dataFinal.put("publicKey", publicKey)

                val rsaCipher =
                    Cipher.getInstance("RSA")
                rsaCipher.init(Cipher.PUBLIC_KEY, pubKey)
                val encryptedKey =
                    rsaCipher.doFinal(key.encoded)
                val decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

                decryptCipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
                val deCrypted = decryptCipher.doFinal(ciphertext)
                val finalData: HashMap<String, String> = hashMapOf()
                finalData.put("data", dataFinal.toString())
                finalData.put("keyblock", Base64.encodeToString(encryptedKey, DEFAULT))
                return finalData
            }
        }
        queue.add(jsonRequest)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)

        generator.initialize(2048, SecureRandom())
        return generator.genKeyPair()
    }

    fun request(url: String, context: Context) {
        val queue = Volley.newRequestQueue(context)
        val jsonRequest = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener { response ->
                localListener.fireApiChangeListener(response)
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                if (baseUrl == "dev.tescolabs.com") {
                    val headers = HashMap<String, String>()
                    headers["Ocp-Apim-Subscription-Key"] = subKey
                    return headers
                }
                return HashMap()
            }
        }

        queue.add(jsonRequest)
    }
}
