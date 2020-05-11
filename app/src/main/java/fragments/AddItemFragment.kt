package fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.fragment_add_item.*

class AddItemFragment(val barcode: String, val itemName: String, val ingredients: Array<String>): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_item, container, false)

        return rootView
    }

    override fun onStart() {
        add_item_name.text = itemName

        add_item_confirm.setOnClickListener {
            //Add item to database
        }
        super.onStart()
    }
}