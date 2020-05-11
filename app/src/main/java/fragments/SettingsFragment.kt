package fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment: Fragment() {
    lateinit var radioColor: RadioButton
    lateinit var radioDark: RadioButton
    lateinit var radioLight: RadioButton
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        radioColor = view.findViewById(R.id.radioColor)
        radioDark = view.findViewById(R.id.radioDark)
        radioLight = view.findViewById(R.id.radioLight)
        saveBtn.setOnClickListener {
            //1=dark, 2=light
            var theme = 1
            if (radioColor.isChecked){
                theme = 1
            }else if(radioDark.isChecked){
                theme = 2
            }else if(radioLight.isChecked){
                theme = 3
            }
            val editor: Editor =
                (context as Context).getSharedPreferences("userSettings", MODE_PRIVATE).edit()
            editor.putInt("theme", theme)
            editor.commit()
        }
    }





//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//        setPreferencesFromResource(R.layout.fragment_settings, rootKey)
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
////        return super.onCreateView(inflater, container, savedInstanceState)
//        return inflater.inflate(R.layout.fragment_settings, container, false)
//    }


}