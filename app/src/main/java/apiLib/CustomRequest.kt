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
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64.getMimeEncoder
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class CustomRequest(listeners: Listeners, context: Context) {
    var baseUrl: String = ""
    var paramsFromCall: HashMap<String, String> = hashMapOf()
    lateinit var subKey: String
    val localListener = listeners
    val appContext = context
    lateinit var aesKey: SecretKey

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
                    val iv = ByteArray(16)
                    val decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    decryptCipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(iv))
                    val deCrypted = decryptCipher.doFinal(response.toByteArray(Charsets.UTF_8))


                    val convertedObject: JSONObject = JSONObject(response)
                    //decrypt the response data.
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
                val keypair = generateKeyPair()
                val data = HashMap<String, String>()
                val r = SecureRandom()
                val iv = ByteArray(16)
//                r.nextBytes(iv)
                data.put("apiKey", subKey)
                paramsFromCall.forEach{
                    data.put(it.key, it.value)
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

                if (prefs.getString("publicKey", "") == ""){
                    prefs.edit().putString("publicKey", encodeToString(keypair.public.encoded, DEFAULT)).commit()
                }


//                val encryptText: ByteArray = data.toString().toByteArray(Charsets.UTF_8)
//                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
//
//                cipher.init(Cipher.ENCRYPT_MODE, key /*AES KEY*/, IvParameterSpec(iv))
//                val ciphertext: ByteArray = cipher.doFinal(encryptText)

                val dataFinal = hashMapOf<String, String>()
                val publicKey = prefs.getString("publicKey", "") as String
//                publicKey = context.getString(R.string.serverPublicKey)
                var serverPublicKey = context.getString(R.string.serverPublicKey)
                serverPublicKey = serverPublicKey.replace("-----BEGIN PUBLIC KEY----- ", "")
                serverPublicKey = serverPublicKey.replace(" -----END PUBLIC KEY-----", "")
                val decodedKey: ByteArray = Base64.decode(serverPublicKey, DEFAULT)
                val keySpec =
                    X509EncodedKeySpec(decodedKey)
                val keyFactory = KeyFactory.getInstance("RSA")
//                val pubKey: RSAPublicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey


                val pubKey = keyFactory.generatePublic(keySpec) //RSA KEY

                dataFinal.put("data", JSONObject(data as Map<*, *>).toString())
                dataFinal.put("publicKey", serverPublicKey)
                val sendData = JSONObject(dataFinal as Map<*, *>).toString()
                val encryptText: ByteArray = sendData.toByteArray(Charsets.UTF_8)

                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, aesKey /*AES KEY*/, IvParameterSpec(iv))
                val ciphertext: ByteArray = cipher.doFinal(encryptText)

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

                val testKey = "kZzFu4s3x91svg+lo9+vKMzcRNY4s2yf8YgdZV0dJvHqOoPckSdji8PlxJ2uo/4hk/zOyVeJXQjN\n" +
                        "f2vXCb/rszDmtSRDPyveCfENAl/PlffHJjd8btpSyorDUrTI7tCZaX1sXzzP9u9bZ8kY8bVeq+It\n" +
                        "8d1ac95uwsVp/Ir1VROdrHmcaSsUNZGZxrLuqzEFxjRkfSRLV/a0W0qBV9lwLoZWckzUp3wzlRWL\n" +
                        "r+W3NOdKRz88DXC838Cwx/s++jGQqyBFjhTPgs9zDuA1qP9S6QFZsf0JkpvrQ9XGuJr8smHmcZ41\n" +
                        "/ykxQ/w/Peu9UVEWZYYBcDxmnq3fPuUS/warug=="
                val decryptCipher = Cipher.getInstance("RSA/None/NoPadding")
                decryptCipher.init(Cipher.PRIVATE_KEY, prKey)
                val deCrypted = decryptCipher.doFinal(Base64.decode(testKey, DEFAULT))
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
