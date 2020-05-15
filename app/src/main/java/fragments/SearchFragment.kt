package fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.aperturedigital.R
import kotlinx.android.synthetic.main.fragment_search.*
import lib.*
import org.json.JSONObject

class SearchFragment: Fragment() {
    lateinit var searchBar: SearchView
    var productNameList: MutableList<String> = mutableListOf()
    val listenerClass = Listeners()

    lateinit var from: Array<String>
    lateinit var to: IntArray
    lateinit var cursorAdapter: CursorAdapter

    //restart cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settings = ImplementSettings(context as Context)
        settings.changeToPreference(constraintLayoutSearchContent, "Search")
        settings.changeTextColor(constraintLayoutSearchContent)
        searchBar = view.findViewById(R.id.searchTxtBar)

        searchBar.setOnQueryTextListener(textChange)
        from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        to = intArrayOf(R.id.item_label)
        cursorAdapter = SimpleCursorAdapter(context, R.layout.suggestion_layout, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)

        searchBar.suggestionsAdapter = cursorAdapter


        checkDb()

        searchBar.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                hideKeyboard()
                hideSystemUI()
                val cursor = searchBar.suggestionsAdapter.getItem(position) as Cursor
                val selection = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                searchBar.setQuery(selection, false)

                //get that certain product
                // Do something with selection
                return true
            }
        })

        searchBar.setOnCloseListener(onClose)
    }

    val onClose = object: SearchView.OnCloseListener{
        override fun onClose(): Boolean {
            hideSystemUI()
            return true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)

    }

    private fun checkDb(){
        val db = DatabaseConnection(context as Context, databaseListener, listenerClass, (context as Context).getString(R.string.databaseApiKey))
        db.getAllProducts()
    }

    private fun searchProducts(product_name: String){
        val db = DatabaseConnection(context as Context, databaseSearchListeners, listenerClass, (context as Context).getString(R.string.databaseApiKey))
        db.searchProducts(product_name)
    }

    private val textChange = object: SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String): Boolean {
            hideKeyboard()
            hideSystemUI()
            searchProducts(query)

            //get product close to the query

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
            newText?.let {
                productNameList.forEachIndexed { index, suggestion ->
                    if (suggestion.contains(newText, true))
                        cursor.addRow(arrayOf(index, suggestion))
                }
            }

            cursorAdapter.changeCursor(cursor)
            return true
        }
    }

    private var databaseListener = object: DatabaseChangeListener {
        override fun onDatabaseChange(response: JSONObject) {
            val products = response["products"] as JSONObject
            products.keys().forEach {
                val product = products.get(it) as JSONObject
                productNameList.add(product["name"].toString())
            }
        }
    }

    private var databaseSearchListeners = object: DatabaseChangeListener {
        override fun onDatabaseChange(response: JSONObject) {
            val products = response["products"] as JSONObject
            val productsList = mutableListOf<String>()
            products.keys().forEach {
                val product = products.get(it) as JSONObject
                productsList.add(product["name"].toString())
            }
            val customAdapter = CustomAdapter(context as Context, productsList)
            val listViewProduct = view!!.findViewById<ListView>(R.id.productList)
            listViewProduct.adapter = customAdapter

            cursorAdapter.cursor.close()

        }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Fragment.hideKeyboard() {
        view?.let {
            activity?.hideKeyboard(it)
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUI() {
        requireActivity().window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}