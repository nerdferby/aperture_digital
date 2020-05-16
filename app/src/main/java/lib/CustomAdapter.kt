package lib

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.aperturedigital.R

class CustomAdapter(
    applicationContext: Context,
    var productList: MutableList<String>
) : BaseAdapter() {
    var context: Context? = null
    var inflter: LayoutInflater? = null

    init {
        this.context = applicationContext
        inflter = (LayoutInflater.from(applicationContext))
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflter?.inflate(R.layout.activity_listview, null) as View
//        val icon: ImageView = view.findViewById(R.id.icon) as ImageView
        val prefs: SharedPreferences =
            context!!.getSharedPreferences("userSettings", Context.MODE_PRIVATE)
        val colorTheme = prefs.getInt("theme", 0)
        if(colorTheme == 1){
            view.findViewById<TextView>(R.id.textView).setTextColor(Color.BLACK)
        }else if(colorTheme == 2){
            view.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(context as Context, R.color.darkModeWhite))

        }else if(colorTheme == 3){
            view.findViewById<TextView>(R.id.textView).setTextColor(Color.BLACK)
        }
        view.findViewById<TextView>(R.id.textView).text = productList[position]
//        icon.setImageResource(flags.get(i))
        return view
    }

    override fun getItem(position: Int): Any? {

        return productList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return productList.count()
    }
}