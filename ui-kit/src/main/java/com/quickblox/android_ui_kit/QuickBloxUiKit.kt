/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit

import android.content.Context
import android.util.Log
import com.quickblox.android_ai_editing_assistant.QBAIRephrase
import com.quickblox.android_ai_translate.Languages
import com.quickblox.android_ai_translate.QBAITranslate
import com.quickblox.android_ai_translate.exception.QBAITranslateException
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.lifecycle.AppLifecycleManager
import com.quickblox.android_ui_kit.presentation.factory.DefaultScreenFactory
import com.quickblox.android_ui_kit.presentation.factory.ScreenFactory
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import java.util.*

@ExcludeFromCoverage
object QuickBloxUiKit {
    val TAG = QuickBloxUiKit::class.java.simpleName

    private var screenFactory: ScreenFactory = DefaultScreenFactory()
    private var theme: UiKitTheme = LightUIKitTheme()
    private var enabledAIAnswerAssistant = true
    private var enabledAITranslate = true
    private var enabledAIRephrase = true
    private var openAIToken: String = ""
    private var proxyServerURL: String = ""

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
        QBAIRephrase.init(context)
        AppLifecycleManager.init()

        val defaultLanguage = getDefaultLanguage()
        setupLanguageToAITranslate(defaultLanguage)
    }

    private fun getDefaultLanguage(): Languages {
        val systemLanguageName = getSystemLanguageName()
        var defaultLanguage: Languages

        try {
            defaultLanguage = QBAITranslate.findLanguageBy(systemLanguageName)
        } catch (exception: QBAITranslateException) {
            defaultLanguage = Languages.ENGLISH
        }

        return defaultLanguage
    }

    fun setupLanguageToAITranslate(language: Languages) {
        QBAITranslate.setLanguage(language)
    }

    private fun getSystemLanguageName(): String {
        val defaultLocale: Locale = Locale.getDefault()
        val englishLocale = Locale("en")

        return defaultLocale.getDisplayLanguage(englishLocale)
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

    fun enableAIAnswerAssistantWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.openAIToken = openAIToken
        this.proxyServerURL = ""
        enabledAIAnswerAssistant = true
    }

    fun enableAIAnswerAssistantWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.openAIToken = ""
        this.proxyServerURL = proxyServerURL
        enabledAIAnswerAssistant = true
    }

    fun disableAIAnswerAssistant() {
        enabledAIAnswerAssistant = false
    }

    fun isAIAnswerAssistantEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAIAnswerAssistant = !isEnabledAIAnswerAssistant()
        if (isNotEnabledAIAnswerAssistant) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (isExistAITokenAndProxyServer()) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return openAIToken.isNotBlank()
    }

    fun isAIAnswerAssistantEnabledWithProxyServer(): Boolean {
        val isNotEnabledAIAnswerAssistant = !isEnabledAIAnswerAssistant()
        if (isNotEnabledAIAnswerAssistant) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (isExistAITokenAndProxyServer()) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return proxyServerURL.isNotBlank()
    }

    fun enableAIRephraseWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.openAIToken = openAIToken
        this.proxyServerURL = ""
        enabledAIRephrase = true
    }

    fun enableAIRephraseWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.openAIToken = ""
        this.proxyServerURL = proxyServerURL
        enabledAIRephrase = true
    }

    fun disableAIRephrase() {
        enabledAIRephrase = false
    }

    fun isAIRephraseEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAIRephrase = !isEnabledAIRephrase()
        if (isNotEnabledAIRephrase) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (isExistAITokenAndProxyServer()) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Rephrase should be initialized with Open AI Token or Proxy server")
        }

        return openAIToken.isNotBlank()
    }

    fun isAIRephraseEnabledWithProxyServer(): Boolean {
        val isNotEnabledAIRephrase = !isEnabledAIRephrase()
        if (isNotEnabledAIRephrase) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (isExistAITokenAndProxyServer()) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return proxyServerURL.isNotBlank()
    }

    fun enableAITranslateWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.openAIToken = openAIToken
        this.proxyServerURL = ""
        enabledAITranslate = true
    }

    fun enableAITranslateWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.openAIToken = ""
        this.proxyServerURL = proxyServerURL
        enabledAITranslate = true
    }

    fun disableAITranslate() {
        enabledAITranslate = false
    }

    fun isAITranslateEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }

        if (isExistAITokenAndProxyServer()) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return openAIToken.isNotBlank()
    }

    fun isAITranslateEnabledWithProxyServer(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }

        if (isExistAITokenAndProxyServer()) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return proxyServerURL.isNotBlank()
    }

    private fun isExistAITokenAndProxyServer(): Boolean {
        return openAIToken.isNotBlank() && proxyServerURL.isNotBlank()
    }

    fun getProxyServerURL(): String {
        return proxyServerURL
    }

    fun getOpenAIToken(): String {
        return openAIToken
    }

    fun isEnabledAIAnswerAssistant(): Boolean {
        return enabledAIAnswerAssistant
    }

    fun isEnabledAITranslate(): Boolean {
        return enabledAITranslate
    }

    fun isEnabledAIRephrase(): Boolean {
        return enabledAIRephrase
    }
}