package apiLib

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.aperturedigital.R
import lib.Listeners
import org.json.JSONObject
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64.getMimeDecoder
import java.util.Base64.getMimeEncoder
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class CustomRequest(listeners: Listeners, context: Context) {
    var baseUrl: String = ""
    var paramsFromCall: HashMap<String, String> = hashMapOf()
    lateinit var subKey: String
    val localListener = listeners
    val appContext = context
    lateinit var aesKey: SecretKey
    var encrypted = false

    lateinit var requestBody: String

    val listenerClass= Listeners()
    var publicKey = ""
    var privateKey = ""



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
                //delete this line after tests are done with Test.php
//                .appendEncodedPath("Test.php")
        }

        for ((k, v) in paramsFromCall) {
            if (gtinNeeded){
                builder.appendQueryParameter(k, v)
            }
        }
        return builder.build().toString()
    }

    fun requestDatabaseKeys(url: String, context: Context) {
        val queue = Volley.newRequestQueue(context)
        val jsonRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                val convertedObject: JSONObject = JSONObject(response)
                //decrypt the response data.
                localListener.fireDatabaseChangeListener(convertedObject)
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val dataJson = HashMap<String, String>()
                dataJson.put("apiKey", subKey)

                paramsFromCall.forEach{
//                    data.put(it.key, it.value)
                    dataJson.put(it.key, it.value)
                }

                return dataJson
            }
        }
        queue.add(jsonRequest)
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
        val jsonRequest = @SuppressLint("NewApi")
        object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                try {
                    val convertedObject: JSONObject = JSONObject(response)

                    val prefs: SharedPreferences =
                        (context as Context).getSharedPreferences("publicKey", Context.MODE_PRIVATE)
                    var prvKey = prefs.getString("privateKey",  "") as String
                    prvKey = prvKey.replace("-----BEGIN PRIVATE KEY-----", "")
                    prvKey = prvKey.replace("-----END PRIVATE KEY-----", "")
                    val prvKeyBytes: ByteArray = Base64.decode(prvKey, DEFAULT)

                    val keySpec =
                        PKCS8EncodedKeySpec(prvKeyBytes)
                    val keyFactory = KeyFactory.getInstance("RSA")
//                val pubKey: RSAPublicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey
                    val prvKeyDecoded = keyFactory.generatePrivate(keySpec) //RSA KEY

                    val keyDecryptCipher = Cipher.getInstance("RSA/None/PKCS1Padding")
                    keyDecryptCipher.init(Cipher.PRIVATE_KEY, prvKeyDecoded)
                    val keyBlock = convertedObject["keyblock"].toString()
                    val tempByteArray = getMimeDecoder().decode(keyBlock)
                    val decryptedKey = keyDecryptCipher.doFinal(tempByteArray)
//                    val newDecryptedKey = unpadZerosToGetAesKey(decryptedKey)
//                    newDecryptedKey

                    val decryptedAesKey = SecretKeySpec(decryptedKey, "AES") //this is correct
                    val encryptedDataBytes = Base64.decode(convertedObject["data"].toString(), DEFAULT)

                    val iv = ByteArray(16)
                    val decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    decryptCipher.init(Cipher.DECRYPT_MODE, decryptedAesKey, IvParameterSpec(iv))
                    val decryptedData = decryptCipher.doFinal(encryptedDataBytes)

                    val decryptedRsponseData = Base64.encodeToString(decryptedData, DEFAULT) as String

                    val finalConvertedObject: JSONObject = JSONObject(decryptedRsponseData)
                    //decrypt the response data.
                    localListener.fireDatabaseChangeListener(finalConvertedObject)
                }catch (e: Exception){
                    val response = JSONObject()
                    response.put("error", true)
                    localListener.fireDatabaseInsertListener(response)
                }
            },
            Response.ErrorListener { error ->
                Log.d("A", "/post request fail! Error: ${error.printStackTrace()}")
            }) {

            @SuppressLint("NewApi")
            @RequiresApi(Build.VERSION_CODES.M)
            override fun getParams(): MutableMap<String, String> {
//                val keypair = generateKeyPair()
                val iv = ByteArray(16)
                val dataJson = JSONObject()
                dataJson.put("apiKey", subKey)
                paramsFromCall.forEach{
                    dataJson.put(it.key, it.value)
                }

                /**
                 * First ask for the servers public key.(or store it to reduce a call)
                 * then encrypt the data and encrypt the AES key with the servers public key
                 * then gets the response and has to decrypt using the private key
                 */

                val kgen: KeyGenerator = KeyGenerator.getInstance("AES")
                kgen.init(128) //set keysize, can be 128, 192, and 256
                aesKey = kgen.generateKey() //AES KEY

                val prefs: SharedPreferences =
                    (context as Context).getSharedPreferences("publicKey", Context.MODE_PRIVATE)

                val dataFinal = JSONObject()
                //this one is sent to the server
                var publicKey = prefs.getString("publicKey", "") as String
                //this one is not
                var serverPublicKey = context.getString(R.string.serverPublicKey)
                serverPublicKey = serverPublicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                serverPublicKey = serverPublicKey.replace("-----END PUBLIC KEY-----", "")

                val decodedKey = getMimeDecoder().decode(serverPublicKey)

                val keySpec =
                    X509EncodedKeySpec(decodedKey)
                val keyFactory = KeyFactory.getInstance("RSA")
//                val pubKey: RSAPublicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey


                val pubKey = keyFactory.generatePublic(keySpec) //RSA KEY

                dataFinal.put("data", dataJson.toString())
                dataFinal.put("publicKey", publicKey)
                val encryptText: ByteArray = dataFinal.toString().toByteArray(Charsets.UTF_8)

                //DATA Encryption
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, aesKey /*AES KEY*/, IvParameterSpec(iv))
                val ciphertext: ByteArray = cipher.doFinal(encryptText)

                //AES KEY Encryption
                val rsaCipher =
                    Cipher.getInstance("RSA/None/NoPadding")
                rsaCipher.init(Cipher.PUBLIC_KEY, pubKey)//RSA Server key
                val encryptedKey =
                    rsaCipher.doFinal(aesKey.encoded)

                var serverPrivateKey = context.getString(R.string.serverPrivate)
                serverPrivateKey = serverPrivateKey.replace("-----BEGIN PRIVATE KEY----- ", "")
                serverPrivateKey = serverPrivateKey.replace(" -----END PRIVATE KEY-----", "")
                val privateKeyDecoded: ByteArray = Base64.decode(serverPrivateKey, DEFAULT)
                val k =
                    PKCS8EncodedKeySpec(privateKeyDecoded)
                val factor = KeyFactory.getInstance("RSA")
                val prKey = factor.generatePrivate(k) //RSA KEY

                //DELETE THIS AND THE PRIVATE KEY ONCE TESTING IS DONE
                val decryptCipher = Cipher.getInstance("RSA/None/NoPadding")
                decryptCipher.init(Cipher.PRIVATE_KEY, prKey)
                val deCrypted = decryptCipher.doFinal(encryptedKey)
                var new = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new = getMimeEncoder().encodeToString(deCrypted)
                }
                val newDecryptedKey = unpadZerosToGetAesKey(deCrypted)

                val finalData: HashMap<String, String> = hashMapOf()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    finalData.put("data", getMimeEncoder().encodeToString(ciphertext))
                    finalData.put("keyblock", getMimeEncoder().encodeToString(encryptedKey))
//                    val phpEncoded = getMimeEncoder().encodeToString(ciphertext)
                }
//                finalData.put("data", encodeToString(ciphertext, DEFAULT))
//                finalData.put("keyblock", encodeToString(encryptedKey, DEFAULT))
                return finalData
            }
        }
        queue.add(jsonRequest)
    }

    fun unpadZerosToGetAesKey(byteIn: ByteArray): ByteArray {
        var i = 0
        while (byteIn[i].toInt() == 0) i++
        var len = byteIn.size - i
        len = if (len <= 16) 16 else if (len <= 24) 24 else 32
        return byteIn.copyOfRange(byteIn.size - len, byteIn.size)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)

        generator.initialize(1024)
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
