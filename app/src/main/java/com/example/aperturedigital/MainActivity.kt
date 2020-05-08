package com.example.aperturedigital

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import apiLib.ApiCall
import apiLib.ApiChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import lib.Encryption
import lib.Listeners
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    /**
     * For Critical version(V.1)
     * TODO: Tidy up Api connection
     * TODO: Get modules working instead of Directories
     * TODO: Fix the return of the api to allow for every product return
     * TODO: Connect to a database so it reduces the amount of api calls
     * TODO: Barcode scanner
     * TODO: Vegan determine algorithm
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnNavigationItemSelectedListener(navListener)
        bottomNav.selectedItemId = R.id.nav_lens
        supportFragmentManager.beginTransaction().replace(R.id.constraintLayoutFragment,
        LensFragment()).commit()
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener(object: (MenuItem) ->
    Boolean {
        override fun invoke(item: MenuItem): Boolean {
            var selectedFragment: Fragment? = null
            when(item.itemId){
                R.id.nav_history -> {
                    selectedFragment = HistoryFragment()
                }
                R.id.nav_lens -> {
                    selectedFragment = LensFragment()
                }
                R.id.nav_news -> {
                    selectedFragment = NewsFragment()
                }
                R.id.nav_search -> {
                    selectedFragment = SearchFragment()
                }
                R.id.nav_settings -> {
                    selectedFragment = SettingsFragment()
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.constraintLayoutFragment,
                selectedFragment!!).commit()
            return true
        }
    })

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
