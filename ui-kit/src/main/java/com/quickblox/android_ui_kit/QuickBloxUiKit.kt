/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit

import android.content.Context
import android.util.Log
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.lifecycle.AppLifecycleManager
import com.quickblox.android_ui_kit.presentation.factory.DefaultScreenFactory
import com.quickblox.android_ui_kit.presentation.factory.ScreenFactory
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

@ExcludeFromCoverage
object QuickBloxUiKit {
    val TAG = QuickBloxUiKit::class.java.simpleName

    private var screenFactory: ScreenFactory = DefaultScreenFactory()
    private var theme: UiKitTheme = LightUIKitTheme()

    @Volatile
    private var dependency: Dependency? = null

    fun setDependency(dependency: Dependency) {
        this.dependency = dependency
    }

    fun getDependency(): Dependency {
        if (dependency == null) {
            Log.e(TAG, "The dependency hasn't initiated. You need first call QuickBloxUiKit.init(context)")
            throw RuntimeException("The dependency hasn't initiated. You need first call QuickBloxUiKit.init(context)")
        }
        return dependency!!
    }

    fun init(context: Context) {
        if (dependency == null) {
            dependency = DependencyImpl(context)
        }
        AppLifecycleManager.init()
    }

    fun getScreenFactory(): ScreenFactory {
        return screenFactory
    }

    fun setScreenFactory(factory: ScreenFactory) {
        screenFactory = factory
    }

    fun getTheme(): UiKitTheme {
        return theme
    }

    fun setTheme(uiKitTheme: UiKitTheme) {
        theme = uiKitTheme
    }

    fun getVersion(): String {
        return "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
    }
}