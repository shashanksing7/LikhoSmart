package nishkaaminnovations.com.likhosmart.HomeScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private val viewModel:DocumentViewModel by viewModels { DocumentViewModelFactory(application) }
    private lateinit var docRecyclerAdapter: DocRecyclerAdapter
    private var documentList: MutableList<docModel> = mutableListOf() // Proper initialization
    private var homeBinding:ActivityHomeBinding?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        homeBinding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding?.root)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE // Hide the bottom navigation

        // Get the NavHostFragment and the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

            val navController: NavController = navHostFragment.navController

            // Set up the BottomNavigationView with the NavController
            val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_navigation)
            bottomNavView.setupWithNavController(navController)

    }

}