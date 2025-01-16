package nishkaaminnovations.com.likhosmart.HomeScreen.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType
import nishkaaminnovations.com.likhosmart.HomeScreen.Documents.docRecyclerCallBack
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentSearchviewBinding

class searchview : Fragment(),searchedItemClickListener{
    private lateinit var binding:FragmentSearchviewBinding
    /*
 Required variables
  */
    private val viewModel: DocumentViewModel by viewModels {
        DocumentViewModelFactory(
            requireActivity().application
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         binding=FragmentSearchviewBinding.inflate(inflater,container,false)
        /*
        Setting the viewmodel and searchedItemClickListener
         */
        binding.likhosearchview.setViewmodel(viewModel)
        binding.likhosearchview.setSearchItemClickListener(this)
        return binding.root
    }

    /*
    This method will be used to open the user selected document form search in workshop.
     */
    override fun itemClicked(docModel: docModel) {
        val action=searchviewDirections.actionSearchviewToWorkShop2(docModel)
        findNavController().navigate(action)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }
}