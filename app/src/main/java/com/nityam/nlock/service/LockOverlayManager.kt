package com.nityam.nlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nityam.nlock.NLockApplication
import com.nityam.nlock.security.BiometricAuthManager
import com.nityam.nlock.ui.lock.BiometricProxyActivity
import com.nityam.nlock.ui.lock.LockScreenContent
import com.nityam.nlock.ui.lock.LockScreenState
import com.nityam.nlock.ui.lock.LockScreenViewModel
import com.nityam.nlock.ui.theme.NLockTheme

/**
 * Custom LifecycleOwner, ViewModelStoreOwner, and SavedStateRegistryOwner for
 * the ComposeView in the AccessibilityService.
 */
internal class ServiceLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

/**
 * Manages a pre-inflated [ComposeView] overlay attached via [WindowManager].
 *
 * The overlay uses [TYPE_ACCESSIBILITY_OVERLAY], which does NOT require
 * [SYSTEM_ALERT_WINDOW] permission — the AccessibilityService has inherent
 * overlay permission.
 *
 * The view is inflated once at service start and reused for every lock event.
 * [show] attaches it; [dismiss] detaches it. No new Activity is launched.
 *
 * The [LockScreenViewModel] is created once at service start and [prepared][LockScreenViewModel.prepare]
 * each time [show] is called with a new target package. Compose observes the ViewModel state
 * reactively; when [LockScreenState.Unlocked] is emitted, the overlay auto-dismisses.
 */
internal class LockOverlayManager(private val service: AccessibilityService) {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: ComposeView
    private lateinit var serviceLifecycleOwner: ServiceLifecycleOwner
    private lateinit var viewModel: LockScreenViewModel
    private var isAttached: Boolean = false
    private var currentTargetPackage: String? = null

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        PixelFormat.TRANSLUCENT
    )

    fun preInflate() {
        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create ViewModel with real dependencies
        val app = service.application as NLockApplication
        viewModel = LockScreenViewModel(
            repository = app.repository,
            pinHashManager = app.pinHashManager,
        )

        serviceLifecycleOwner = ServiceLifecycleOwner()
        serviceLifecycleOwner.onCreate()

        overlayView = ComposeView(service).apply {
            setViewTreeLifecycleOwner(serviceLifecycleOwner)
            setViewTreeViewModelStoreOwner(serviceLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(serviceLifecycleOwner)
            setContent {
                NLockTheme {
                    val state by viewModel.state.collectAsState()

                    // Auto-dismiss when unlocked
                    LaunchedEffect(state) {
                        if (state is LockScreenState.Unlocked) {
                            val pkg = (state as LockScreenState.Unlocked).packageName
                            val svc = AppLockAccessibilityService.instance
                            svc?.recordUnlock(pkg)
                            dismiss()
                            viewModel.reset()
                        }
                    }

                    LockScreenContent(
                        state = state,
                        onDigit = { viewModel.onDigit(it) },
                        onBackspace = { viewModel.onBackspace() },
                        onConfirm = { viewModel.onConfirm() },
                        onBiometric = { launchBiometricProxy() },
                    )
                }
            }
        }
    }

    fun show(targetPackage: String) {
        // If already showing the same package, don't re-prepare
        if (isAttached && currentTargetPackage == targetPackage) return

        currentTargetPackage = targetPackage

        // Load app icon from PackageManager
        val icon = try {
            service.packageManager.getApplicationIcon(targetPackage)
        } catch (_: Exception) {
            null
        }

        // Check biometric availability from preferences (synchronous check)
        val biometricAvailable = BiometricAuthManager.canAuthenticate(service)

        // Prepare the ViewModel for this app
        viewModel.prepare(
            packageName = targetPackage,
            icon = icon,
            biometricAvailable = biometricAvailable,
        )

        if (!isAttached) {
            serviceLifecycleOwner.onResume()
            windowManager.addView(overlayView, layoutParams)
            isAttached = true
        }
    }

    fun dismiss() {
        if (isAttached) {
            serviceLifecycleOwner.onPause()
            windowManager.removeView(overlayView)
            isAttached = false
            currentTargetPackage = null
        }
    }

    fun destroy() {
        dismiss()
        serviceLifecycleOwner.onDestroy()
    }

    /** Called by [BiometricProxyActivity] on successful biometric auth. */
    internal fun onBiometricSuccess() {
        viewModel.onBiometricSuccess()
    }

    private fun launchBiometricProxy() {
        val pkg = currentTargetPackage ?: return
        val intent = Intent(service, BiometricProxyActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BiometricProxyActivity.EXTRA_TARGET_PACKAGE, pkg)
        }
        service.startActivity(intent)
    }
}
