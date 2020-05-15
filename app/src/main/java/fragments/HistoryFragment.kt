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

class HistoryFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings = ImplementSettings(context as Context)
        settings.changeToPreference(constraintLayoutHistoryContent, "History")
        settings.changeTextColor(constraintLayoutHistoryContent)

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