package fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.fragment_history.*
import lib.ImplementSettings
import localdatabase.LocalDBOpenHelper
import localdatabase.Product

class HistoryFragment: Fragment() {
    /**
     * At the moment this creating a SQLite (local) database and reading from it to get the scanned
     * history of the user.
     * Currently the database is not set up fully.
     */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDb()

        val settings = ImplementSettings(context as Context)
        settings.changeToPreference(constraintLayoutHistoryContent, "History")
        settings.changeTextColor(constraintLayoutHistoryContent)

    }

    //Move this to the lensFragment with the response from the api
//    fun addToDb(){
//        val dbHandler = LocalDBOpenHelper(context as Context, null)
//        val product = Product("1","gtin","Description","Ingredients","Lifestyle")
//        dbHandler.addProduct(product)
//    }

    private fun viewDb(){
        var finalString: String = ""
        val dbHandler = LocalDBOpenHelper(context as Context, null)
        val cursor = dbHandler.getAllProducts()
        cursor!!.moveToFirst()
        if (cursor.count > 0){
            for (col in 0 until cursor.columnCount){
                finalString += cursor.getString(col) + " "
            }
            while (cursor.moveToNext()){
                for (col in 0 .. cursor.columnCount){
                    finalString += cursor.getString(col) + " "
                }
                finalString += "\n"
            }
        }

        cursor.close()
        updateText(finalString)
    }

    private fun updateText(text: String){
        val historyText = TextView(context as Context)
        historyText.textSize = 20f
        historyText.text = text


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (historyText.id == -1){
                historyText.id = View.generateViewId()
            }
        }
        (view as ViewGroup).removeAllViews()
        (view as ViewGroup).addView(historyText)
        historyText.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

        val set = ConstraintSet()
        set.clone((view as ViewGroup).findViewById<ConstraintLayout>(R.id.constraintLayoutHistoryContent))
        set.connect(historyText.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        set.connect(
            historyText.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        set.connect(
            historyText.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )
        set.connect(
            historyText.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        set.applyTo((view as ViewGroup).findViewById(R.id.constraintLayoutHistoryContent))
    }
}