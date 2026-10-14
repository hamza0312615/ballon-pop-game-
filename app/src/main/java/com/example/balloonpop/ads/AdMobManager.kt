package com.example.balloonpop.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMobManager manages Google Mobile Ads SDK lifecycle.
 * Handles App Open, Interstitial, and Rewarded Video Ads.
 * Includes automatic fallback to Google AdMob sample units if production
 * ad units are still pending approval or return ERROR_CODE_NO_FILL (code 3).
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // --- Production Ad Units provided by user ---
    const val APP_ID = "ca-app-pub-5098386592651454~7436641428"
    const val PROD_APP_OPEN_ID = "ca-app-pub-5098386592651454/3949831719"
    const val PROD_INTERSTITIAL_ID = "ca-app-pub-5098386592651454/7208129455"
    const val PROD_REWARDED_ID = "ca-app-pub-5098386592651454/4581966114"

    // --- Official Google Sample / Test Ad Unit IDs (Guaranteed 100% fill rate for verification) ---
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private val mainHandler = Handler(Looper.getMainLooper())

    var isInitialized = false
        private set

    // Status tracking for in-app diagnostics
    var lastAppOpenStatus: String = "Not loaded"
        private set
    var lastInterstitialStatus: String = "Not loaded"
        private set
    var lastRewardedStatus: String = "Not loaded"
        private set

    // Automatic fallback when live unit returns No Fill (Error Code 3)
    var fallbackToTestOnNoFill = true

    // Cached Ad instances
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenWaitCallback: (() -> Unit)? = null

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false
    private var pendingRewardedShowCallback: ((Activity, (RewardItem?) -> Unit, () -> Unit) -> Unit)? = null

    private var isShowingAd = false

    fun initialize(context: Context, onInitComplete: (() -> Unit)? = null) {
        if (isInitialized) {
            onInitComplete?.invoke()
            return
        }

        Thread {
            try {
                val reqConfig = RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                    .build()
                MobileAds.setRequestConfiguration(reqConfig)

                MobileAds.initialize(context.applicationContext) { initStatus ->
                    isInitialized = true
                    Log.d(TAG, "AdMob MobileAds initialized successfully: ${initStatus.adapterStatusMap}")

                    Log.d(TAG, "AdMob MobileAds initialized successfully (lazy on-demand loading enabled)")

                    mainHandler.post {
                        onInitComplete?.invoke()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MobileAds", e)
                lastAppOpenStatus = "Init error: ${e.localizedMessage}"
                mainHandler.post {
                    onInitComplete?.invoke()
                }
            }
        }.start()
    }

    // =========================================================================
    // 1. APP OPEN AD
    // =========================================================================
    fun loadAppOpenAd(context: Context, isFallback: Boolean = false) {
        if (appOpenAd != null || isAppOpenLoading) return
        isAppOpenLoading = true

        val adUnitId = if (isFallback) TEST_APP_OPEN_ID else PROD_APP_OPEN_ID
        lastAppOpenStatus = "Loading ($adUnitId)..."
        Log.d(TAG, "Loading App Open Ad using ID: $adUnitId")

        try {
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                adUnitId,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        isAppOpenLoading = false
                        appOpenAd = ad
                        lastAppOpenStatus = "Ready (${if (isFallback) "Test" else "Live"})"
                        Log.d(TAG, "App Open Ad loaded successfully ($adUnitId)")

                        val waiter = appOpenWaitCallback
                        appOpenWaitCallback = null
                        waiter?.invoke()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isAppOpenLoading = false
                        appOpenAd = null
                        lastAppOpenStatus = "Failed: ${error.code} (${error.message})"
                        Log.w(TAG, "App Open Ad failed ($adUnitId): code=${error.code}, message=${error.message}")

                        if (!isFallback && fallbackToTestOnNoFill && (error.code == AdRequest.ERROR_CODE_NO_FILL || error.code == 0 || error.code == 3)) {
                            Log.i(TAG, "Falling back to Google Test App Open ad unit...")
                            loadAppOpenAd(context, isFallback = true)
                        } else {
                            val waiter = appOpenWaitCallback
                            appOpenWaitCallback = null
                            waiter?.invoke()
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            isAppOpenLoading = false
            Log.w(TAG, "App Open Ad load exception", t)
            val waiter = appOpenWaitCallback
            appOpenWaitCallback = null
            waiter?.invoke()
        }
    }

    /**
     * Shows the App Open ad. If the ad is still loading, waits up to timeoutMillis before
     * giving up and proceeding to avoid hanging the splash screen.
     */
    fun showAppOpenAd(
        activity: Activity,
        timeoutMillis: Long = 3000L,
        onDismiss: () -> Unit = {}
    ) {
        if (isShowingAd) {
            onDismiss()
            return
        }

        val ad = appOpenAd
        if (ad != null) {
            displayAppOpenAd(activity, ad, onDismiss)
            return
        }

        if (isAppOpenLoading) {
            Log.d(TAG, "App Open Ad still loading, waiting up to ${timeoutMillis}ms...")
            var hasTriggered = false

            val timeoutRunnable = Runnable {
                if (!hasTriggered) {
                    hasTriggered = true
                    appOpenWaitCallback = null
                    Log.d(TAG, "App Open Ad wait timed out, continuing")
                    onDismiss()
                }
            }
            mainHandler.postDelayed(timeoutRunnable, timeoutMillis)

            appOpenWaitCallback = {
                mainHandler.removeCallbacks(timeoutRunnable)
                if (!hasTriggered) {
                    hasTriggered = true
                    val loadedAd = appOpenAd
                    if (loadedAd != null) {
                        displayAppOpenAd(activity, loadedAd, onDismiss)
                    } else {
                        onDismiss()
                    }
                }
            }
        } else {
            // Not loading and not available, reload for next time and continue
            loadAppOpenAd(activity.applicationContext)
            onDismiss()
        }
    }

    private fun displayAppOpenAd(activity: Activity, ad: AppOpenAd, onDismiss: () -> Unit) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad dismissed")
                isShowingAd = false
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                isShowingAd = false
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad showing")
                isShowingAd = true
            }
        }
        ad.show(activity)
    }

    // =========================================================================
    // 2. INTERSTITIAL AD (Game Over / Stage Clear)
    // =========================================================================
    fun loadInterstitialAd(context: Context, isFallback: Boolean = false) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adUnitId = if (isFallback) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
        lastInterstitialStatus = "Loading ($adUnitId)..."
        Log.d(TAG, "Loading Interstitial Ad using ID: $adUnitId")

        try {
            val request = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        isInterstitialLoading = false
                        interstitialAd = ad
                        lastInterstitialStatus = "Ready (${if (isFallback) "Test" else "Live"})"
                        Log.d(TAG, "Interstitial Ad loaded ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isInterstitialLoading = false
                        interstitialAd = null
                        lastInterstitialStatus = "Failed: ${error.code} (${error.message})"
                        Log.w(TAG, "Interstitial Ad failed ($adUnitId): code=${error.code}, message=${error.message}")

                        if (!isFallback && fallbackToTestOnNoFill && (error.code == AdRequest.ERROR_CODE_NO_FILL || error.code == 0 || error.code == 3)) {
                            Log.i(TAG, "Falling back to Google Test Interstitial unit...")
                            loadInterstitialAd(context, isFallback = true)
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            isInterstitialLoading = false
            Log.w(TAG, "Interstitial Ad load exception", t)
        }
    }

    fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit = {}) {
        if (isShowingAd) {
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    isShowingAd = false
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    isShowingAd = false
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showing")
                    isShowingAd = true
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not ready, preloading for next occasion")
            loadInterstitialAd(activity.applicationContext)
            onDismiss()
        }
    }

    // =========================================================================
    // 3. REWARDED AD (Free Spin, Shop Rewards, Revives)
    // =========================================================================
    fun loadRewardedAd(
        context: Context,
        isFallback: Boolean = false,
        onLoaded: (() -> Unit)? = null
    ) {
        if (rewardedAd != null || isRewardedLoading) {
            onLoaded?.invoke()
            return
        }
        isRewardedLoading = true

        val adUnitId = if (isFallback) TEST_REWARDED_ID else PROD_REWARDED_ID
        lastRewardedStatus = "Loading ($adUnitId)..."
        Log.d(TAG, "Loading Rewarded Ad using ID: $adUnitId")

        try {
            val request = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                adUnitId,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        isRewardedLoading = false
                        rewardedAd = ad
                        lastRewardedStatus = "Ready (${if (isFallback) "Test" else "Live"})"
                        Log.d(TAG, "Rewarded Ad loaded successfully ($adUnitId)")
                        onLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isRewardedLoading = false
                        rewardedAd = null
                        lastRewardedStatus = "Failed: ${error.code} (${error.message})"
                        Log.w(TAG, "Rewarded Ad failed ($adUnitId): code=${error.code}, message=${error.message}")

                        if (!isFallback && fallbackToTestOnNoFill && (error.code == AdRequest.ERROR_CODE_NO_FILL || error.code == 0 || error.code == 3)) {
                            Log.i(TAG, "Falling back to Google Test Rewarded Ad unit...")
                            loadRewardedAd(context, isFallback = true, onLoaded = onLoaded)
                        } else {
                            onLoaded?.invoke()
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            isRewardedLoading = false
            Log.w(TAG, "Rewarded Ad load exception", t)
            onLoaded?.invoke()
        }
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (RewardItem?) -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        if (isShowingAd) {
            Toast.makeText(activity, "An ad is already displaying", Toast.LENGTH_SHORT).show()
            onDismiss()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            displayRewardedAd(activity, ad, onRewardEarned, onDismiss)
            return
        }

        // If ad is currently loading, show feedback and display once ready
        Toast.makeText(activity, "Loading reward ad, please wait...", Toast.LENGTH_SHORT).show()
        loadRewardedAd(activity.applicationContext) {
            val freshAd = rewardedAd
            if (freshAd != null) {
                displayRewardedAd(activity, freshAd, onRewardEarned, onDismiss)
            } else {
                Toast.makeText(
                    activity,
                    "Ad is currently preparing. Please tap again in a moment! ($lastRewardedStatus)",
                    Toast.LENGTH_LONG
                ).show()
                onDismiss()
            }
        }
    }

    private fun displayRewardedAd(
        activity: Activity,
        ad: RewardedAd,
        onRewardEarned: (RewardItem?) -> Unit,
        onDismiss: () -> Unit
    ) {
        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad dismissed")
                isShowingAd = false
                rewardedAd = null
                loadRewardedAd(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                isShowingAd = false
                rewardedAd = null
                loadRewardedAd(activity.applicationContext)
                Toast.makeText(activity, "Ad playback failed: ${adError.message}", Toast.LENGTH_SHORT).show()
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad showing")
                isShowingAd = true
            }
        }

        ad.show(activity) { rewardItem ->
            rewardGranted = true
            Log.d(TAG, "User rewarded: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned(rewardItem)
        }
    }
}
