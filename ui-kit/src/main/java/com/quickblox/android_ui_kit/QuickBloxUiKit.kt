/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit

import android.content.Context
import android.util.Log
import com.quickblox.android_ai_answer_assistant.settings.AnswerAssistantSettingsImpl
import com.quickblox.android_ai_editing_assistant.settings.RephraseSettingsImpl
import com.quickblox.android_ai_translate.Languages
import com.quickblox.android_ai_translate.QBAITranslate
import com.quickblox.android_ai_translate.exception.QBAITranslateException
import com.quickblox.android_ai_translate.settings.TranslateSettingsImpl
import com.quickblox.android_ai_translate.settings.TranslateSettingsImpl.Model.GPT_3_5_TURBO
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
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
    private var enabledForward = true

    private var answerAssistantOpenAIToken: String = ""
    private var answerAssistantProxyServerURL: String = ""
    private var answerAssistantMaxRequestTokens: Int = 3000
    private var answerAssistantOpenAIModel: AnswerAssistantSettingsImpl.Model =
        AnswerAssistantSettingsImpl.Model.GPT_3_5_TURBO
    private var answerAssistantOrganization: String? = null
    private var answerAssistantMaxResponseTokens: Int? = null
    private var answerAssistantTemperature: Float = 0.5f

    private var rephraseOpenAIToken: String = ""
    private var rephraseProxyServerURL: String = ""
    private var rephraseMaxRequestTokens: Int = 3000
    private var rephraseOpenAIModel: RephraseSettingsImpl.Model = RephraseSettingsImpl.Model.GPT_3_5_TURBO
    private var rephraseOrganization: String? = null
    private var rephraseMaxResponseTokens: Int? = null
    private var rephraseTemperature: Float = 0.5f

    private var translateOpenAIToken: String = ""
    private var translateProxyServerURL: String = ""
    private var translateMaxRequestTokens: Int = 3000
    private var translateOpenAIModel: TranslateSettingsImpl.Model = GPT_3_5_TURBO
    private var translateOrganization: String? = null
    private var translateMaxResponseTokens: Int? = null
    private var translateTemperature: Float = 0.5f
    private var translateLanguage: Languages = Languages.ENGLISH

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

        val defaultLanguage = getDefaultLanguage()
        setupLanguageToAITranslate(defaultLanguage)
    }

    private fun getDefaultLanguage(): Languages {
        val systemLanguageName = getSystemLanguageName()
        var defaultLanguage: Languages

        try {
            defaultLanguage = QBAITranslate.findLanguageByName(systemLanguageName)
        } catch (exception: QBAITranslateException) {
            defaultLanguage = Languages.ENGLISH
        }

        return defaultLanguage
    }

    fun setupLanguageToAITranslate(language: Languages) {
        translateLanguage = language
    }

    fun getAITranslateLanguage(): Languages {
        return translateLanguage
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
        this.answerAssistantOpenAIToken = openAIToken
        this.answerAssistantProxyServerURL = ""
        enabledAIAnswerAssistant = true
    }

    fun enableAIAnswerAssistantWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.answerAssistantOpenAIToken = ""
        this.answerAssistantProxyServerURL = proxyServerURL
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

        if (isExistAITokenAndProxyServer(answerAssistantOpenAIToken, answerAssistantProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return answerAssistantOpenAIToken.isNotBlank()
    }

    fun isAIAnswerAssistantEnabledWithProxyServer(): Boolean {
        val isNotEnabledAIAnswerAssistant = !isEnabledAIAnswerAssistant()
        if (isNotEnabledAIAnswerAssistant) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (isExistAITokenAndProxyServer(answerAssistantOpenAIToken, answerAssistantProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return answerAssistantProxyServerURL.isNotBlank()
    }

    fun enableAIRephraseWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.rephraseOpenAIToken = openAIToken
        this.rephraseProxyServerURL = ""
        enabledAIRephrase = true
    }

    fun enableAIRephraseWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.rephraseOpenAIToken = ""
        this.rephraseProxyServerURL = proxyServerURL
        enabledAIRephrase = true
    }

    fun setMaxRequestTokensForAIRephrase(maxTokens: Int) {
        rephraseMaxRequestTokens = maxTokens
    }

    fun getMaxRequestTokensForAIRephrase(): Int {
        return rephraseMaxRequestTokens
    }

    fun setOpenAIModelForAIRephrase(openAIModel: RephraseSettingsImpl.Model) {
        this.rephraseOpenAIModel = openAIModel
    }

    fun getOpenAIModelForAIRephrase(): RephraseSettingsImpl.Model {
        return rephraseOpenAIModel
    }

    fun setOrganizationForAIRephrase(organization: String) {
        this.rephraseOrganization = organization
    }

    fun getOrganizationForAIRephrase(): String? {
        return rephraseOrganization
    }

    fun setMaxResponseTokensForAIRephrase(maxTokens: Int) {
        rephraseMaxResponseTokens = maxTokens
    }

    fun getMaxResponseTokensForAIRephrase(): Int? {
        return rephraseMaxResponseTokens
    }

    fun setTemperatureForAIRephrase(temperature: Float) {
        rephraseTemperature = temperature
    }

    fun getTemperatureForAIRephrase(): Float {
        return rephraseTemperature
    }

    fun setRephraseTones(rephraseTones: List<AIRephraseToneEntity>) {
        if (dependency == null) {
            Log.e(TAG, "The dependency hasn't initiated. You need first call QuickBloxUiKit.init(context)")
            throw RuntimeException("The dependency hasn't initiated. You need first call QuickBloxUiKit.init(context)")
        }
        dependency?.getAIRepository()?.setAllRephraseTones(rephraseTones)
    }

    fun getRephraseTones(): List<AIRephraseToneEntity> {
        if (dependency == null) {
            Log.e(TAG, "The dependency hasn't initiated. You need first call QuickBloxUiKit.init(context)")
            throw RuntimeException("The dependency hasn't initiated. You need first call QuickBloxUiKit.init(context)")
        }
        return dependency?.getAIRepository()?.getAllRephraseTones()!!
    }

    fun disableAIRephrase() {
        enabledAIRephrase = false
    }

    fun isAIRephraseEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAIRephrase = !isEnabledAIRephrase()
        if (isNotEnabledAIRephrase) {
            throw RuntimeException("The AI Rephrase is disabled")
        }

        if (isExistAITokenAndProxyServer(rephraseOpenAIToken, rephraseProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Rephrase should be initialized with Open AI Token or Proxy server")
        }

        return rephraseOpenAIToken.isNotBlank()
    }

    fun isAIRephraseEnabledWithProxyServer(): Boolean {
        val isNotEnabledAIRephrase = !isEnabledAIRephrase()
        if (isNotEnabledAIRephrase) {
            throw RuntimeException("The AI Rephrase is disabled")
        }

        if (isExistAITokenAndProxyServer(rephraseOpenAIToken, rephraseProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return rephraseProxyServerURL.isNotBlank()
    }

    fun enableAITranslateWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.translateOpenAIToken = openAIToken
        this.translateProxyServerURL = ""
        enabledAITranslate = true
    }

    fun enableAITranslateWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.translateOpenAIToken = ""
        this.translateProxyServerURL = proxyServerURL
        enabledAITranslate = true
    }

    fun setMaxRequestTokensForAITranslate(maxTokens: Int) {
        translateMaxRequestTokens = maxTokens
    }

    fun getMaxRequestTokensForAITranslate(): Int {
        return translateMaxRequestTokens
    }

    fun setOpenAIModelForAITranslate(openAIModel: TranslateSettingsImpl.Model) {
        this.translateOpenAIModel = openAIModel
    }

    fun getOpenAIModelForAITranslate(): TranslateSettingsImpl.Model {
        return translateOpenAIModel
    }

    fun setOrganizationForAITranslate(organization: String) {
        this.translateOrganization = organization
    }

    fun getOrganizationForAITranslate(): String? {
        return translateOrganization
    }

    fun setMaxResponseTokensForAITranslate(maxTokens: Int) {
        translateMaxResponseTokens = maxTokens
    }

    fun getMaxResponseTokensForAITranslate(): Int? {
        return translateMaxResponseTokens
    }

    fun setTemperatureForAITranslate(temperature: Float) {
        translateTemperature = temperature
    }

    fun getTemperatureForAITranslate(): Float {
        return translateTemperature
    }

    fun disableAITranslate() {
        enabledAITranslate = false
    }

    fun isAITranslateEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }

        if (isExistAITokenAndProxyServer(translateOpenAIToken, translateProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return translateOpenAIToken.isNotBlank()
    }

    fun isAITranslateEnabledWithProxyServer(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }

        if (isExistAITokenAndProxyServer(translateOpenAIToken, translateProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return translateProxyServerURL.isNotBlank()
    }

    private fun isExistAITokenAndProxyServer(openAIToken: String, proxyServerURL: String): Boolean {
        return openAIToken.isNotBlank() && proxyServerURL.isNotBlank()
    }

    fun getAnswerAssistantProxyServerURL(): String {
        return answerAssistantProxyServerURL
    }

    fun getAnswerAssistantOpenAIToken(): String {
        return answerAssistantOpenAIToken
    }

    fun getRephraseProxyServerURL(): String {
        return rephraseProxyServerURL
    }

    fun getRephraseOpenAIToken(): String {
        return rephraseOpenAIToken
    }

    fun getTranslateProxyServerURL(): String {
        return translateProxyServerURL
    }

    fun getTranslateOpenAIToken(): String {
        return translateOpenAIToken
    }

    fun setMaxRequestTokensForAIAnswerAssistant(maxTokens: Int) {
        answerAssistantMaxRequestTokens = maxTokens
    }

    fun getMaxRequestTokensForAIAnswerAssistant(): Int {
        return answerAssistantMaxRequestTokens
    }

    fun setOpenAIModelForAIAnswerAssistant(openAIModel: AnswerAssistantSettingsImpl.Model) {
        this.answerAssistantOpenAIModel = openAIModel
    }

    fun getOpenAIModelForAIAnswerAssistant(): AnswerAssistantSettingsImpl.Model {
        return answerAssistantOpenAIModel
    }

    fun setOrganizationForAIAnswerAssistant(organization: String) {
        this.answerAssistantOrganization = organization
    }

    fun getOrganizationForAIAnswerAssistant(): String? {
        return answerAssistantOrganization
    }

    fun setMaxResponseTokensForAIAnswerAssistant(maxTokens: Int) {
        answerAssistantMaxResponseTokens = maxTokens
    }

    fun getMaxResponseTokensForAIAnswerAssistant(): Int? {
        return answerAssistantMaxResponseTokens
    }

    fun setTemperatureForAIAnswerAssistant(temperature: Float) {
        answerAssistantTemperature = temperature
    }

    fun getTemperatureForAIAnswerAssistant(): Float {
        return answerAssistantTemperature
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

    fun enableForward() {
        enabledForward = true
    }

    fun disableForward() {
        enabledForward = false
    }

    fun isEnabledForward(): Boolean {
        return enabledForward
    }
}