package fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import barcodescanner.BarcodeScanner
import com.example.aperturedigital.R
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture
import lib.Constants
import lib.Listeners


class BarcodeFragment(listeners: Listeners): Fragment() {
    lateinit var rootView: View
    lateinit var barcodeCapture: Fragment
    lateinit var currentContext: Context
    var listenerClass = listeners
    var constantClass = Constants()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_barcode_scanner, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startBarcode()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentContext = context

    }

    private fun startBarcode(){

        val barcodeCapture = this.childFragmentManager.fragments[0] as BarcodeCapture

        val scanner =
            BarcodeScanner(currentContext, (barcodeCapture as BarcodeCapture?), listenerClass)
        listenerClass.addBarcodeChangeListener(barcodeListenerInp)
    }

    private var barcodeListenerInp = object:
        lib.BarcodeChangeListener {
        override fun onBarcode(response: String) {
            Log.d(constantClass.DEBUGTAG, response)

            this@BarcodeFragment.childFragmentManager.beginTransaction().remove(
                this@BarcodeFragment.childFragmentManager.fragments[0])
            activity!!.runOnUiThread{
                (rootView as ViewGroup).removeAllViews()
            }
            val intent = Intent(context, BarcodeFragment::class.java)
            intent.putExtra("gtin", response)
            parentFragment!!.onActivityResult(targetRequestCode, RESULT_OK, intent)
            fragmentManager!!.popBackStack()
        }
    }
}