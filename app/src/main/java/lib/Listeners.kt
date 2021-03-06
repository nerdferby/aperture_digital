package lib

import org.json.JSONObject

class Listeners {
    private val onApiChangeListeners = mutableListOf<ApiChangeListener>()
    private val onBarcodeChangeListener = mutableListOf<BarcodeChangeListener>()
    private val onDatabaseListeners = mutableListOf<DatabaseChangeListener>()
    private val onDatabaseVeganListeners = mutableListOf<DatabaseChangeListener>()
    private val onDatabaseInsertListeners = mutableListOf<DatabaseChangeListener>()

    fun addApiChangeListener(listener: ApiChangeListener){
        onApiChangeListeners.add(listener)
    }

    fun fireApiChangeListener(response: JSONObject){
        onApiChangeListeners.forEach{
            it.onApiChange(response)
        }
    }

    fun addDatabaseChangeListener(listener: DatabaseChangeListener){
        onDatabaseListeners.add(listener)
    }

    fun fireDatabaseChangeListener(response: JSONObject){
        onDatabaseListeners.forEach{
            it.onDatabaseChange(response)
        }
//        onDatabaseListeners[onDatabaseListeners.count()-1].onDatabaseChange(response)
    }

    fun addVeganDatabaseChangeListener(listener: DatabaseChangeListener){
        onDatabaseVeganListeners.add(listener)
    }

    fun fireVeganDatabaseChangeListener(response: JSONObject){
//        onDatabaseVeganListeners.forEach{
//            it.onDatabaseChange(response)
//        }
        onDatabaseVeganListeners[onDatabaseVeganListeners.count()-1].onDatabaseChange(response)
    }

    fun addDatabaseInsertListener(listener: DatabaseChangeListener){
        onDatabaseInsertListeners.add(listener)
    }

    fun fireDatabaseInsertListener(response: JSONObject){
        onDatabaseInsertListeners.forEach{
            it.onDatabaseChange(response)
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
interface DatabaseChangeListener {
    fun onDatabaseChange(response: JSONObject)
}

interface BarcodeChangeListener {
    fun onBarcode(response: String)
}