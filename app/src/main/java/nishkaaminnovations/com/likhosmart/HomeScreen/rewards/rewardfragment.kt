package nishkaaminnovations.com.likhosmart.HomeScreen.rewards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentRewardfragmentBinding

class rewardfragment : Fragment() {

    private lateinit var binding: FragmentRewardfragmentBinding
    private var rewardedAd: RewardedAd? = null
    private val TAG = "RewardFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRewardfragmentBinding.inflate(inflater, container, false)

        // Load the reward ad when the fragment is created
        loadRewardAd()

      binding.showAdButton.setOnClickListener{
           showRewardAd() 
      }

        return binding.root
    }

    // Load the rewarded ad
    private fun loadRewardAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/5224354917", // Replace with your Ad Unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Ad was loaded.")
                    setAdCallbacks()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                }
            }
        )
    }

    // Show the rewarded ad
    private fun showRewardAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(requireActivity()) { rewardItem: RewardItem ->
                // User earned the reward
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward: $rewardType ($rewardAmount)")
                // Handle the reward (e.g., give points, unlock features)
            }
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

    // Set callbacks for ad events
    private fun setAdCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                rewardedAd = null
                // Reload the ad after it's dismissed
                loadRewardAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }
}
