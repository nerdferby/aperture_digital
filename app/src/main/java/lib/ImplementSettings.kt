package lib

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.aperturedigital.R

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
                    (component as Button).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
                    (component as Button).setBackgroundColor(Color.TRANSPARENT)
                }else if(colorTheme == 2){
                    //light mode color
//                    (component as Button).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
                }

            }
        }
    }
}