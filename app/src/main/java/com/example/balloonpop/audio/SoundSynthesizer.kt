package com.example.balloonpop.audio

import android.content.Context

/**
 * Backward compatibility alias for SoundManager.
 * Delegates all audio requests to the unified SoundManager implementation.
 */
class SoundSynthesizer(context: Context? = null) {
    private val delegate = SoundManager.getInstance(context)

    val isMuted: Boolean get() = delegate.muted
    val masterVolume: Float get() = delegate.masterVolume

    fun toggleMute(): Boolean = delegate.toggleMute()
    fun setMuted(value: Boolean) = delegate.setMuted(value)
    fun setVolume(volume: Float) = delegate.setVolume(volume)

    fun playPop(radius: Float = 35f) = delegate.playPop(radius)
    fun playHammerSmash(tier: Int = 1) = delegate.playHammerSmash(tier)
    fun playCoin() = delegate.playCoin()
    fun playSlash() = delegate.playSlash()
    fun playBomb() = delegate.playBomb()
    fun playFreeze() = delegate.playFreeze()
    fun playClick() = delegate.playClick()
    fun playCombo(comboLevel: Int) = delegate.playCombo(comboLevel)
    fun playStageClear() = delegate.playStageClear()
    fun playWheelTick() = delegate.playWheelTick()
    fun playRocketSwoosh() = delegate.playRocketSwoosh()
    fun playZap() = delegate.playZap()
    fun playShieldHit() = delegate.playShieldHit()
    fun playShieldCrack() = delegate.playShieldCrack()
    fun playMystery() = delegate.playMystery()
    fun playGhostChime() = delegate.playGhostChime()
    fun playPiggyJackpot() = delegate.playPiggyJackpot()
    fun playThunder() = delegate.playThunder()
    fun playRainbowBlast() = delegate.playRainbowBlast()
    fun playPraise() = delegate.playPraise()
    fun playStarChime(starIndex: Int = 1) = delegate.playStarChime(starIndex)
    fun playMiss() = delegate.playMiss()
    fun playMissWarning() = delegate.playMissWarning()
    fun playVaultOpen() = delegate.playVaultOpen()
    fun playShopChime() = delegate.playShopChime()
    fun playQuestReward() = delegate.playQuestReward()
    fun playHighScoreFanfare() = delegate.playHighScoreFanfare()
    fun playLevelWarp() = delegate.playLevelWarp()

    fun pauseAll() = delegate.pauseAll()
    fun resumeAll() = delegate.resumeAll()
    fun release() = delegate.release()
}
