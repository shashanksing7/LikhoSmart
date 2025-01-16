package nishkaaminnovations.com.likhosmart.WorkShop.Viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nishkaaminnovations.com.likhosmart.databinding.FragmentLastPageBinding


class lastPageFragment(var lastPageListener: lastPageListener) : PageFragment() {

    private lateinit var binding:FragmentLastPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Inflate the layout using ViewBinding
        binding = FragmentLastPageBinding.inflate(inflater, container, false)
        binding.addImage.setOnClickListener{
            lastPageListener.addNewPage()
        }
        return binding.root

    }
}