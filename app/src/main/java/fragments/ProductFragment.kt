package fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.fragment_product.*
import lib.DatabaseChangeListener
import lib.DatabaseConnection
import lib.ImplementSettings
import lib.Listeners
import org.json.JSONObject


class ProductFragment: Fragment() {
    lateinit var rootView: View
    lateinit var currentContext: Context
    var productName = ""
    var productBarcode = ""
    val veganListenerClass = Listeners()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_product, container, false)
        productName = requireArguments().getString("productName") as String
        productBarcode = requireArguments().getString("productBarcode") as String
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textViewProduct = view.findViewById<TextView>(R.id.textViewProductName)
        val settings = ImplementSettings(context as Context)
        settings.changeToPreference(constraintLayoutProductContent, "Search")
        settings.changeTextColor(constraintLayoutProductContent)
        textViewProduct.text = productName
        checkVeganDb(productBarcode)
        view.findViewById<TextView>(R.id.textViewVegan).text = ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentContext = context
    }

    private fun checkVeganDb(barcode: String){
        val db = DatabaseConnection(context as Context, databaseVeganListener, veganListenerClass, (context as Context).getString(R.string.databaseApiKey))
        db.isVegan(barcode)
    }

    private val databaseVeganListener = object: DatabaseChangeListener{
        @SuppressLint("NewApi")
        override fun onDatabaseChange(response: JSONObject) {
            val textViewVegan = view!!.findViewById<TextView>(R.id.textViewVegan)
            val backgroundLayout = rootView.findViewById<ConstraintLayout>(R.id.constraintLayoutProductContent)
            if (response["data"].toString() == "IS_VEGAN"){
                textViewVegan.text = "Vegan"
                backgroundLayout.setBackgroundColor(context!!.getColor(R.color.veganColor))
            }else if(response["data"].toString() == "NOT_VEGAN"){
                textViewVegan.text  = "Not Vegan"
                backgroundLayout.setBackgroundColor(context!!.getColor(R.color.notVeganColor))
            }else{
                textViewVegan.text = "We are not sure if this product is vegan"
            }


        }
    }
}