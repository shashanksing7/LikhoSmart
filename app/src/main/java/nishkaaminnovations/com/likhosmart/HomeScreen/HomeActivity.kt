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



//        getDataByDate()
//
//       /*
//          setting up Recycler View
//        */
//
//        val numberOfColumns = 2 // Change this number to set how many columns you want in your grid
//        homeBinding?.docRecycler?.layoutManager = GridLayoutManager(this, numberOfColumns)
//        docRecyclerAdapter = DocRecyclerAdapter(documentList)
//        homeBinding?.docRecycler?.adapter = docRecyclerAdapter
//        initializeChipGroup()

    }
//    /*
//    Method to initialize chip group
//     */
//    // Method to initialize chip group
//    private fun initializeChipGroup() {
//        homeBinding?.chipGroup?.setOnCheckedStateChangeListener { group, checkIds ->
//            if (checkIds.isNotEmpty()) {
//                val chipId = group.findViewById<Chip>(checkIds.first())
//
//                // Sort list based on selected chip
//                when (chipId.text) {
//                    chipIds.CHIP_DATE.chipTYpe -> {
//                        // Sort by date in descending order (latest first)
//                        documentList.sortByDescending { it.createdDate }
//                    }
//                    chipIds.CHIP_NAME.chipTYpe -> {
//                        // Sort by name alphabetically
//                        documentList.sortBy { it.name }
//                    }
//                    chipIds.CHIP_TYPE.chipTYpe -> {
//                        // Sort by type, you can define custom sorting logic if needed
//                        documentList.sortBy { it.docType }
//                    }
//                }
//                // Notify adapter of the data change after sorting
//                docRecyclerAdapter.notifyDataSetChanged()
//            }
//        }
//    }
//    /*
// Method to get data from the database by sorting according to date and
// always add a specific item at the beginning.
// */
//    private fun getDataByDate() {
//        viewModel.getDataByDate().observe(this) { doctList ->
//            documentList.clear()  // Clear the existing list
//
//            // Add the new item at the beginning
//            val firstItem = docModel("Create",DocType.Create_New)
//            documentList.add(0, firstItem)
//
//            // Add the remaining items
//            documentList.addAll(doctList)
//
//            // Notify adapter of data change
//            docRecyclerAdapter.notifyDataSetChanged()
//        }
//    }

}