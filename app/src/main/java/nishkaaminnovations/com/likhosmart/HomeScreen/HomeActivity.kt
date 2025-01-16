package nishkaaminnovations.com.likhosmart.HomeScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.appwrite.Appwrite
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
        enableEdgeToEdge()
        homeBinding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding?.root)

        // Get the NavHostFragment and the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

            val navController: NavController = navHostFragment.navController

            // Set up the BottomNavigationView with the NavController
            val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_navigation)
            bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            when (destination.id) {
                R.id.workShop -> bottomNavView.visibility = View.GONE
                R.id.documentsFragment -> bottomNavView.visibility = View.GONE
                R.id.userProfile -> bottomNavView.visibility = View.GONE
                R.id.searchview -> bottomNavView.visibility = View.GONE
                R.id.editUserProfile -> bottomNavView.visibility = View.GONE
                R.id.changePassword -> bottomNavView.visibility = View.GONE
                R.id.smartpoints -> bottomNavView.visibility = View.GONE
                R.id.folderFragment->bottomNavView.visibility = View.GONE
                R.id.smartink -> {
                    bottomNavView.visibility = View.GONE
                }
                else -> bottomNavView.visibility = View.VISIBLE
            }
        }

    }

}