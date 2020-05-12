package lib

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.aperturedigital.R
import org.w3c.dom.Text

class ImplementSettings(context: Context) {
    val currentContext = context
    val prefs: SharedPreferences =
        currentContext.getSharedPreferences("userSettings", Context.MODE_PRIVATE)

    fun changeToPreference(layout: ConstraintLayout, fragment: String){
        changeLayoutColor(layout, fragment)

    }

    private fun changeLayoutColor(layout: ConstraintLayout, fragment: String){
        val colorTheme = prefs.getInt("theme", 0)
        if(colorTheme == 1){
            var tempColor = 0
            when(fragment){
                "Lens"->{tempColor = Color.parseColor("#29b4cc")}
                "History"->{tempColor = Color.parseColor("#c73cfa")}
                "News"->{tempColor = (Color.parseColor("#e3a71b"))}
                "Search"->{tempColor = (Color.parseColor("#76d41e"))}
                "Settings"->{tempColor = (Color.parseColor("#f54963"))}
            }
            layout.setBackgroundColor(tempColor)
        }else if(colorTheme == 2){
            layout.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.darkModeBlack))
        }else if(colorTheme == 3){
            layout.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
        }
    }

    fun changeTextColor(layout: ConstraintLayout){
        val colorTheme = prefs.getInt("theme", 0)
        for (i in 0.. layout.childCount){
            //change the objects text color
            val component = layout.getChildAt(i)

            if (component is Button){
                if(colorTheme == 1){
                    (component as Button).setTextColor(Color.BLACK)
                    component.setBackgroundColor(Color.TRANSPARENT)
                }else if(colorTheme == 2){
                    (component as Button).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
                    (component as Button).setBackgroundColor(Color.TRANSPARENT)
                }else if(colorTheme == 3){
                    (component as Button).setTextColor(Color.BLACK)
                    (component as Button).setBackgroundColor(Color.TRANSPARENT)
                }
            }else if(component is TextView){
                if(colorTheme == 1){
                    (component as TextView).setTextColor(Color.BLACK)
                    component.setBackgroundColor(Color.TRANSPARENT)
                }else if(colorTheme == 2){
                    (component as TextView).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
                    (component as TextView).setBackgroundColor(Color.TRANSPARENT)
                }else if(colorTheme == 3){
                    (component as TextView).setTextColor(Color.BLACK)
                    (component as TextView).setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

}