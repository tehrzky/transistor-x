package org.y20k.transistor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import org.y20k.transistor.core.Category

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_category -> showAddCategoryDialog()
                R.id.manage -> showManageCategories()
                else -> filterStations(menuItem.title.toString())
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun filterStations(category: String) {
        // Implement station filtering
    }

    private fun showAddCategoryDialog() {
        // Implement dialog
    }

    private fun showManageCategories() {
        // Implement management
    }
}
