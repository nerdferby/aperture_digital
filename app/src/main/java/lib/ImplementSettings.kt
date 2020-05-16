package lib

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.suggestion_layout.view.*
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

    private fun checkComponents(component: Any, colorTheme: Int){
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
        else if(component is SearchView){
            if ((component.getChildAt(0) as ViewGroup).getChildAt(0) != null){
                if(colorTheme == 1){
                    ((component.getChildAt(0) as ViewGroup).getChildAt(0) as TextView).setTextColor(Color.BLACK)

                }else if(colorTheme == 2){
                    ((component.getChildAt(0) as ViewGroup).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))

                }else if(colorTheme == 3){
                    ((component.getChildAt(0) as ViewGroup).getChildAt(0) as TextView).setTextColor(Color.BLACK)

                }
            }
        }
        else if(component is RadioGroup){
            for (radio in 0.. (component as RadioGroup).childCount){
                if (component.getChildAt(radio) != null){
                    if(colorTheme == 1){
                        (component.getChildAt(radio) as RadioButton).setTextColor(Color.BLACK)
                    }else if(colorTheme == 2){
                        (component.getChildAt(radio) as RadioButton).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
                    }else if(colorTheme == 3){
                        (component.getChildAt(radio) as RadioButton).setTextColor(Color.BLACK)
                    }
                }
            }

        }
//        else if (component is ListView){
//            if(colorTheme == 1){
//                val view = (LayoutInflater.from(currentContext)).inflate(R.layout.activity_listview, null) as View
//                view.findViewById<TextView>(R.id.textView).setTextColor(Color.BLACK)
//            }else if(colorTheme == 2){
//                val view = (LayoutInflater.from(currentContext)).inflate(R.layout.activity_listview, null) as ViewGroup
//                if (view.childCount > 1){
//                    (view.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(currentContext, R.color.darkModeWhite))
//                }
//            }else if(colorTheme == 3){
//                val view = (LayoutInflater.from(currentContext)).inflate(R.layout.activity_listview, null) as View
//                view.findViewById<TextView>(R.id.textView).setTextColor(Color.BLACK)
//            }
//        }
    }

    fun changeTextColor(layout: ConstraintLayout){
        val colorTheme = prefs.getInt("theme", 0)
        for (i in 0.. layout.childCount){
            //change the objects text color
            val component = layout.getChildAt(i)
            if (component is ConstraintLayout){
                for (x in 0 .. (component as ViewGroup).childCount){
                    if (component.getChildAt(x) != null){
                        checkComponents(component.getChildAt(x), colorTheme)
                    }
                }
            }

            if (component != null){
                checkComponents(component, colorTheme)
            }
        }
    }

}