package lib

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.aperturedigital.R

class CustomAdapter(
    applicationContext: Context,
    var productList: MutableList<String>
) : BaseAdapter() {
    var context: Context? = null
    var inflter: LayoutInflater? = null

    init {
        this.context = context
        inflter = (LayoutInflater.from(applicationContext))
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflter?.inflate(R.layout.activity_listview, null) as View
//        val icon: ImageView = view.findViewById(R.id.icon) as ImageView
        view.findViewById<TextView>(R.id.textView).setText(productList[position])
//        icon.setImageResource(flags.get(i))
        return view
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return productList.count()
    }
}