package barcodescanner

import android.content.Context
import android.util.SparseArray
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic
import com.google.android.gms.vision.barcode.Barcode
import lib.Listeners
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever


class BarcodeScanner(context: Context, capture: BarcodeCapture?, listeners: Listeners): BarcodeRetriever {

//    var onResponseListener: ResponseListener? = null

    val contextNeeded = context
    var barcodeCapture: BarcodeCapture? = capture
    val localListeners = listeners

    init {
        barcodeCapture!!.setRetrieval(this)

    }

    override fun onRetrieved(barcode: Barcode) {
//        Log.d("test", "Barcode read: " + barcode.displayValue)
        localListeners.fireBarcodeChange(barcode.displayValue)
//        barcodeCapture!!.onDestroy()
        barcodeCapture!!.pause()


    }

    override fun onRetrievedMultiple(
        closetToClick: Barcode?,
        barcode: MutableList<BarcodeGraphic>?
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBitmapScanned(sparseArray: SparseArray<Barcode>) {
        // when image is scanned and processed
    }

    override fun onRetrievedFailed(reason: String) {
        // in case of failure
    }
    override fun onPermissionRequestDenied() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }




}