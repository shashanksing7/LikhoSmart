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
    // Add a fragment dynamically at the second-to-last position
    fun addFragmentAtSecondLast(fragment: PageFragment) {
        // Ensure there's at least one fragment in the list
        val insertPosition = if (fragmentList.isNotEmpty()) fragmentList.size - 1 else 0
        // Add the new fragment at the second-to-last position
        fragmentList.add(insertPosition, fragment)
        // Notify the adapter about the insertion
        notifyItemInserted(insertPosition)
    }

}
