package com.nityam.nlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.view.View
import android.view.WindowManager
import com.nityam.nlock.ui.lock.LockScreenActivity

/**
 * Manages app locking via a two-layer approach:
 *
 * 1. **Blocker overlay** — A simple opaque dark [View] (TYPE_ACCESSIBILITY_OVERLAY)
 *    that appears instantly to cover the locked app's content before the activity
 *    can render. It's FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE so it never
 *    intercepts user input. Once the [LockScreenActivity] draws its first frame
 *    (same dark background → seamless), the blocker fades to transparent.
 *
 * 2. **[LockScreenActivity]** — A proper FragmentActivity hosting the full lock screen
 *    UI (biometric prompt + PIN keypad). Using an Activity gives us native lifecycle
 *    for BiometricPrompt and a smooth, lag-free UX.
 *
 * Audio focus is also managed here to mute media from the locked app.
 */
internal class LockOverlayManager(private val service: AccessibilityService) {

    private lateinit var windowManager: WindowManager
    private lateinit var blockerView: View

    internal var isAttached: Boolean = false
        private set
    internal var currentTargetPackage: String? = null
        private set

    private var isBlockerAttached = false
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private var audioFocusRequest: Any? = null

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        PixelFormat.TRANSLUCENT
    )

    fun preInflate() {
        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        blockerView = View(service).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#111318"))
        }
    }

    /**
     * Show the lock for [targetPackage].
     *
     * 1. Instantly attaches the dark blocker overlay (prevents flash of locked content).
     * 2. Launches [LockScreenActivity] with the target package.
     */
    fun show(targetPackage: String) {
        // If already showing for the same package and the activity is alive, skip
        if (isAttached && currentTargetPackage == targetPackage) {
            if (LockScreenActivity.isInForeground || LockScreenActivity.currentInstance != null) {
                return
            }
        }

        currentTargetPackage = targetPackage
        isAttached = true

        // 1. Show the opaque blocker instantly
        showBlocker()

        // 2. Request audio focus to mute locked app's media
        requestAudioFocus()

        // 3. Launch the full lock screen activity
        val intent = Intent(service, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, targetPackage)
        }
        service.startActivity(intent)
    }

    /**
     * Dismiss the lock screen entirely (blocker + activity).
     */
    fun dismiss() {
        if (!isAttached) return
        isAttached = false
        currentTargetPackage = null
        removeBlocker()
        abandonAudioFocus()
        LockScreenActivity.currentInstance?.finish()
    }

    fun destroy() {
        dismiss()
    }

    // ── Blocker Management ──

    private fun showBlocker() {
        if (!isBlockerAttached) {
            blockerView.alpha = 1f
            windowManager.addView(blockerView, layoutParams)
            isBlockerAttached = true
        } else {
            // Already attached — make it opaque again (may have been faded out)
            blockerView.animate().cancel()
            blockerView.alpha = 1f
        }
    }

    /**
     * Called by [LockScreenActivity] once its window is drawn, fading the blocker
     * to reveal the activity underneath. Since both use the same dark background,
     * the transition is visually seamless.
     */
    internal fun hideBlocker() {
        if (isBlockerAttached) {
            blockerView.animate()
                .alpha(0f)
                .setDuration(120)
                .start()
        }
    }

    private fun removeBlocker() {
        if (isBlockerAttached) {
            blockerView.animate().cancel()
            windowManager.removeView(blockerView)
            isBlockerAttached = false
        }
    }

    // ── Audio Focus ──

    private fun requestAudioFocus() {
        if (audioFocusChangeListener != null) return // Already holding focus

        val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val listener = AudioManager.OnAudioFocusChangeListener { }
        audioFocusChangeListener = listener

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(listener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                listener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (audioFocusRequest as? AudioFocusRequest)?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
            audioFocusRequest = null
        } else {
            audioFocusChangeListener?.let {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(it)
            }
        }
        audioFocusChangeListener = null
    }
}
