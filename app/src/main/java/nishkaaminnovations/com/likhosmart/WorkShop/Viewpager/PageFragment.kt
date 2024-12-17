package nishkaaminnovations.com.likhosmart.WorkShop.Viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.CustomLayout

/*
THis class will represent the individual page of our app
 */
class PageFragment(var pageNumber:Int?=null,var pageLoaded:Boolean=false) : Fragment() {
    /*
    Variable to represent the custom layout.
     */
    lateinit var layout:CustomLayout

    /*
    Variable to represent the view
     */
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        rootView=inflater.inflate(R.layout.fragment_page, container, false)
        layout=rootView.findViewById(R.id.pageLayout)
        return rootView
    }

}