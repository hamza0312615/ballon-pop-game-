package com.example.balloonpop.ads

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
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
 *
 * Pass 5 (audit §2.1): Google test/sample units may ONLY ever load in
 * debuggable (debug) builds — the runtime equivalent of BuildConfig.DEBUG
 * (this project does not enable the BuildConfig build feature). A release
 * build always loads the production units, and a production no-fill simply
 * means "no ad this time": the show functions degrade gracefully to their
 * onDismiss callback. This guarantees a Play release can never serve
 * zero-revenue test creatives to players.
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

    /**
     * True only for debuggable (debug) builds. Gates every use of the Google
     * test ad units: in a release build the test-unit fallback below can
     * never trigger, even if a caller passes isFallback = true — the loaders
     * clamp the choice back to the production unit.
     */
    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // Cached Ad instances
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenWaitCallback: (() -> Unit)? = null

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val _isShowingAdFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isShowingAdFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = _isShowingAdFlow

    var isAdShowing: Boolean = false
        private set

    const val INTERSTITIAL_COOLDOWN_MS = 180_000L // At most 1 interstitial per ~3 minutes
    private var lastInterstitialShownTimeMillis: Long = 0L
    private var lastAnyAdClosedTimeMillis: Long = 0L

    var isShowingAd: Boolean = false
        private set

    private fun setAdShowingState(showing: Boolean) {
        isShowingAd = showing
        isAdShowing = showing
        _isShowingAdFlow.value = showing
    }

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

        // Test units are reachable ONLY in debuggable builds (see class doc).
        val useTestUnits = isFallback && isDebuggable(context)
        val adUnitId = if (useTestUnits) TEST_APP_OPEN_ID else PROD_APP_OPEN_ID
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
                        lastAppOpenStatus = "Ready (${if (useTestUnits) "Test" else "Live"})"
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

                        if (!isFallback && isDebuggable(context) && (error.code == AdRequest.ERROR_CODE_NO_FILL || error.code == 0 || error.code == 3)) {
                            Log.i(TAG, "Debug build: falling back to Google Test App Open ad unit...")
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
                setAdShowingState(false)
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                setAdShowingState(false)
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad showing")
                setAdShowingState(true)
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

        // Test units are reachable ONLY in debuggable builds (see class doc).
        val useTestUnits = isFallback && isDebuggable(context)
        val adUnitId = if (useTestUnits) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
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
                        lastInterstitialStatus = "Ready (${if (useTestUnits) "Test" else "Live"})"
                        Log.d(TAG, "Interstitial Ad loaded ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isInterstitialLoading = false
                        interstitialAd = null
                        lastInterstitialStatus = "Failed: ${error.code} (${error.message})"
                        Log.w(TAG, "Interstitial Ad failed ($adUnitId): code=${error.code}, message=${error.message}")

                        if (!isFallback && isDebuggable(context) && (error.code == AdRequest.ERROR_CODE_NO_FILL || error.code == 0 || error.code == 3)) {
                            Log.i(TAG, "Debug build: falling back to Google Test Interstitial unit...")
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

    fun showInterstitialAd(activity: Activity, force: Boolean = false, onDismiss: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (isShowingAd) {
            onDismiss()
            return
        }

        // At most 1 interstitial per ~3 minutes, and never right after another ad
        if (!force && (now - lastInterstitialShownTimeMillis < INTERSTITIAL_COOLDOWN_MS || now - lastAnyAdClosedTimeMillis < 60_000L)) {
            Log.d(TAG, "Skipping interstitial due to 3-min cooldown (${(now - lastInterstitialShownTimeMillis) / 1000}s elapsed)")
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    setAdShowingState(false)
                    lastAnyAdClosedTimeMillis = System.currentTimeMillis()
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    setAdShowingState(false)
                    lastAnyAdClosedTimeMillis = System.currentTimeMillis()
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showing")
                    lastInterstitialShownTimeMillis = System.currentTimeMillis()
                    setAdShowingState(true)
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

        // Test units are reachable ONLY in debuggable builds (see class doc).
        val useTestUnits = isFallback && isDebuggable(context)
        val adUnitId = if (useTestUnits) TEST_REWARDED_ID else PROD_REWARDED_ID
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
                        lastRewardedStatus = "Ready (${if (useTestUnits) "Test" else "Live"})"
                        Log.d(TAG, "Rewarded Ad loaded successfully ($adUnitId)")
                        onLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isRewardedLoading = false
                        rewardedAd = null
                        lastRewardedStatus = "Failed: ${error.code} (${error.message})"
                        Log.w(TAG, "Rewarded Ad failed ($adUnitId): code=${error.code}, message=${error.message}")

                        if (!isFallback && isDebuggable(context) && (error.code == AdRequest.ERROR_CODE_NO_FILL || error.code == 0 || error.code == 3)) {
                            Log.i(TAG, "Debug build: falling back to Google Test Rewarded Ad unit...")
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
                    "Ad is currently preparing. Please tap again in a moment!",
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
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad dismissed")
                setAdShowingState(false)
                rewardedAd = null
                loadRewardedAd(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                setAdShowingState(false)
                rewardedAd = null
                loadRewardedAd(activity.applicationContext)
                Toast.makeText(activity, "Ad playback failed: ${adError.message}", Toast.LENGTH_SHORT).show()
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad showing")
                setAdShowingState(true)
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User rewarded: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned(rewardItem)
        }
    }
}
