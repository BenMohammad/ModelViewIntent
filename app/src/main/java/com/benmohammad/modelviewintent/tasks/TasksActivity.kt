package com.benmohammad.modelviewintent.tasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.benmohammad.modelviewintent.R
import com.benmohammad.modelviewintent.stats.StatisticsActivity
import com.benmohammad.modelviewintent.util.addFragmentToActivity
import com.google.android.material.navigation.NavigationView

class TasksActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_act)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        if(navigationView != null) {
            setUpDrawerContent(navigationView)
        }

        if(supportFragmentManager.findFragmentById(R.id.contentFrame) == null) {
            addFragmentToActivity(supportFragmentManager, TasksFragment(), R.id.contentFrame)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {

                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener {
            item ->
            when(item.itemId) {
                R.id.list_navigation_menu_item -> {

                }

                R.id.statistics_navigation_menu_item -> {
                    val intent = Intent(this@TasksActivity, StatisticsActivity::class.java)
                    startActivity(intent)
                } else -> { }
            }
            item.isChecked = true
            drawerLayout.closeDrawers()
            true
        }
    }
}