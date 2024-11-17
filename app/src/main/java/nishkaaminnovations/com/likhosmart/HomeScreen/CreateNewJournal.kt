package nishkaaminnovations.com.likhosmart.HomeScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentCreateNewJournalBinding

/*
This class will be used to create an ew journal by the user
 */
class CreateNewJournal : Fragment() {

    /*
    Variable representing the binding
     */
    private var binding:FragmentCreateNewJournalBinding?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentCreateNewJournalBinding.inflate(inflater,container,false)
        return binding?.root
    }


}