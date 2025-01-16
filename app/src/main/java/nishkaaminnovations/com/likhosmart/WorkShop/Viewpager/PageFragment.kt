package nishkaaminnovations.com.likhosmart.WorkShop.Viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.cl

/*
THis class will represent the individual page of our app
 */
open class PageFragment(var pageNumber:Int?=null,var pageLoaded:Boolean=false) : Fragment() {
    /*
    Variable to represent the custom layout.
     */
  lateinit var layout:cl

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
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = layout.measuredWidth
                val height = layout.measuredHeight
                layout.setContentSize(width.toFloat(), height.toFloat())
            }
        })
        return rootView
    }
    open fun getlayout():cl{
        return layout
    }

}