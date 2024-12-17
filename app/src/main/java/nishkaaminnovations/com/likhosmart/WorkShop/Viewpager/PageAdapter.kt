package nishkaaminnovations.com.likhosmart.WorkShop.Viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PageAdapter(activity: AppCompatActivity):FragmentStateAdapter(activity) {
    private val fragmentList = mutableListOf<PageFragment>()
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): PageFragment{
        return fragmentList[position]
    }
    // Add a fragment dynamically
    fun addFragment(fragment: PageFragment) {
        fragmentList.add(fragment)
        notifyItemInserted(fragmentList.size - 1)
    }

}
//package nishkaaminnovations.com.likhosmart.WorkShop.Viewpager
//
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentStatePagerAdapter
//
//class PageAdapter(
//    fragmentManager: FragmentManager
//) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
//
//    private val fragmentList = mutableListOf<Fragment>()
//
//    override fun getCount(): Int {
//        return fragmentList.size
//    }
//
//    override fun getItem(position: Int): Fragment {
//        return fragmentList[position]
//    }
//
//    // Add a fragment dynamically
//    fun addFragment(fragment: Fragment) {
//        fragmentList.add(fragment)
//        notifyDataSetChanged() // Notify the adapter of changes
//    }
//
//    // Remove a fragment dynamically
//    fun removeFragment(position: Int) {
//        if (position in 0 until fragmentList.size) {
//            fragmentList.removeAt(position)
//            notifyDataSetChanged()
//        }
//    }
//
//    // Replace a fragment at a specific position
//    fun replaceFragment(position: Int, fragment: Fragment) {
//        if (position in 0 until fragmentList.size) {
//            fragmentList[position] = fragment
//            notifyDataSetChanged()
//        }
//    }
//}
