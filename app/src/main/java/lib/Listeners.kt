package lib

class Listeners {
    private val onCallBackMainListener = mutableListOf<CallbackToMainListener>()

    fun addCallBackToMain(listener: CallbackToMainListener){
        onCallBackMainListener.add(listener)
    }

    fun fireCallBackToMain(response: String){
        onCallBackMainListener.forEach{
            it.updateText(response)
        }
    }

    interface CallbackToMainListener{
        fun updateText(text: String)
    }

}