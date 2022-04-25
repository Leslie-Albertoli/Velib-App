package com.example.velib_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private var myMapFragment: MapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentManager = this.supportFragmentManager
        myMapFragment = fragmentManager.findFragmentById(R.id.fragment_map) as MapFragment?
    }
}
