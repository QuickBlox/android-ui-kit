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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@ExcludeFromCoverage
object QuickBloxUiKit {
    val TAG = QuickBloxUiKit::class.java.simpleName

    private var screenFactory: ScreenFactory = DefaultScreenFactory()
    private var theme: UiKitTheme = LightUIKitTheme()
    private var enabledAIAnswerAssistant = true
    private var enabledAITranslate = true
    private var enabledAIRephrase = true
    private var enabledForward = true
    private var enabledReply = true

    private var answerAssistantSmartChatAssistantId: String = ""

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantOpenAIToken: String = ""

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantProxyServerURL: String = ""

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantMaxRequestTokens: Int = 3000

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantOpenAIModel: AnswerAssistantSettingsImpl.Model =
        AnswerAssistantSettingsImpl.Model.GPT_3_5_TURBO

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantOrganization: String? = null

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantMaxResponseTokens: Int? = null

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var answerAssistantTemperature: Float = 0.5f

    private var rephraseOpenAIToken: String = ""
    private var rephraseProxyServerURL: String = ""
    private var rephraseMaxRequestTokens: Int = 3000
    private var rephraseOpenAIModel: RephraseSettingsImpl.Model = RephraseSettingsImpl.Model.GPT_3_5_TURBO
    private var rephraseOrganization: String? = null
    private var rephraseMaxResponseTokens: Int? = null
    private var rephraseTemperature: Float = 0.5f

    private var translateSmartChatAssistantId: String = ""

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateOpenAIToken: String = ""

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateProxyServerURL: String = ""

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateMaxRequestTokens: Int = 3000

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateOpenAIModel: TranslateSettingsImpl.Model = GPT_3_5_TURBO

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateOrganization: String? = null

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateMaxResponseTokens: Int? = null

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateTemperature: Float = 0.5f

    @Deprecated("This method is deprecated and will be removed in future versions.")
    private var translateLanguage: Languages = Languages.ENGLISH

    private var regexUserName: String? = null

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

    fun release(errorCallback: (t: String) -> Unit = {}) {
        CoroutineScope(Dispatchers.Main).runCatching {
            launch {
                dependency?.getConnectionRepository()?.disconnect()
                dependency?.getDialogsRepository()?.clearAllDialogsInLocal()
            }
        }.onFailure {
            errorCallback.invoke(it.message ?: "Error release QuickBloxUiKit")
        }
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
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

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setupLanguageToAITranslate(language: Languages) {
        translateLanguage = language
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getAITranslateLanguage(): Languages {
        return translateLanguage
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
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

    fun enableAIAnswerAssistantWithSmartChatAssistantId(smartChatAssistantId: String) {
        if (smartChatAssistantId.isBlank()) {
            throw RuntimeException("The smartChatAssistantId shouldn't be empty")
        }
        this.answerAssistantSmartChatAssistantId = smartChatAssistantId
        this.answerAssistantOpenAIToken = ""
        this.answerAssistantProxyServerURL = ""
        enabledAIAnswerAssistant = true
    }

    @Deprecated(
        message = "Use enableAIAnswerAssistantWithSmartChatAssistantId(smartChatAssistantId) instead",
        replaceWith = ReplaceWith("enableAIAnswerAssistantWithSmartChatAssistantId(smartChatAssistantId)")
    ) fun enableAIAnswerAssistantWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.answerAssistantOpenAIToken = openAIToken
        this.answerAssistantProxyServerURL = ""
        this.answerAssistantSmartChatAssistantId = ""
        enabledAIAnswerAssistant = true
    }

    @Deprecated(
        message = "Use enableAIAnswerAssistantWithSmartChatAssistantId(smartChatAssistantId) instead",
        replaceWith = ReplaceWith("enableAIAnswerAssistantWithSmartChatAssistantId(smartChatAssistantId)")
    )  fun enableAIAnswerAssistantWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.answerAssistantOpenAIToken = ""
        this.answerAssistantProxyServerURL = proxyServerURL
        this.answerAssistantSmartChatAssistantId = ""
        enabledAIAnswerAssistant = true
    }

    fun disableAIAnswerAssistant() {
        enabledAIAnswerAssistant = false
    }

    fun isAIAnswerAssistantEnabledWithSmartChatAssistantId(): Boolean {
        val isNotEnabledAIAnswerAssistant = !isEnabledAIAnswerAssistant()
        if (isNotEnabledAIAnswerAssistant) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (answerAssistantSmartChatAssistantId.isNotBlank() && isExistAITokenAndProxyServer(answerAssistantOpenAIToken, answerAssistantProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Smart Chat Assistant Id, Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Smart Chat Assistant Id, Open AI Token or Proxy server")
        }

        return answerAssistantSmartChatAssistantId.isNotBlank()
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun isAIAnswerAssistantEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAIAnswerAssistant = !isEnabledAIAnswerAssistant()
        if (isNotEnabledAIAnswerAssistant) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (answerAssistantSmartChatAssistantId.isNotBlank() && isExistAITokenAndProxyServer(answerAssistantOpenAIToken, answerAssistantProxyServerURL)) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Answer Assistant should be initialized with Open AI Token or Proxy server")
        }

        return answerAssistantOpenAIToken.isNotBlank()
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun isAIAnswerAssistantEnabledWithProxyServer(): Boolean {
        val isNotEnabledAIAnswerAssistant = !isEnabledAIAnswerAssistant()
        if (isNotEnabledAIAnswerAssistant) {
            throw RuntimeException("The AI Answer assistant is disabled")
        }

        if (answerAssistantSmartChatAssistantId.isNotBlank() && isExistAITokenAndProxyServer(answerAssistantOpenAIToken, answerAssistantProxyServerURL)) {
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

    fun enableAITranslateWithSmartChatAssistantId(smartChatAssistantId: String) {
        if (smartChatAssistantId.isBlank()) {
            throw RuntimeException("The smartChatAssistantId shouldn't be empty")
        }
        this.translateSmartChatAssistantId = smartChatAssistantId
        this.translateOpenAIToken = ""
        this.translateProxyServerURL = ""
        enabledAITranslate = true
    }

    @Deprecated(
        message = "Use enableAITranslateWithSmartChatAssistantId(smartChatAssistantId) instead",
        replaceWith = ReplaceWith("enableAITranslateWithSmartChatAssistantId(smartChatAssistantId)")
    )
    fun enableAITranslateWithOpenAIToken(openAIToken: String) {
        if (openAIToken.isBlank()) {
            throw RuntimeException("The openAIToken shouldn't be empty")
        }
        this.translateOpenAIToken = openAIToken
        this.translateProxyServerURL = ""
        this.translateSmartChatAssistantId = ""
        enabledAITranslate = true
    }

    @Deprecated(
        message = "Use enableAITranslateWithSmartChatAssistantId(smartChatAssistantId) instead",
        replaceWith = ReplaceWith("enableAITranslateWithSmartChatAssistantId(smartChatAssistantId)")
    )
    fun enableAITranslateWithProxyServer(proxyServerURL: String) {
        if (proxyServerURL.isBlank()) {
            throw RuntimeException("The proxyServerURL shouldn't be empty")
        }
        this.translateOpenAIToken = ""
        this.translateSmartChatAssistantId = ""
        this.translateProxyServerURL = proxyServerURL
        enabledAITranslate = true
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setMaxRequestTokensForAITranslate(maxTokens: Int) {
        translateMaxRequestTokens = maxTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getMaxRequestTokensForAITranslate(): Int {
        return translateMaxRequestTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setOpenAIModelForAITranslate(openAIModel: TranslateSettingsImpl.Model) {
        this.translateOpenAIModel = openAIModel
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")

    fun getOpenAIModelForAITranslate(): TranslateSettingsImpl.Model {
        return translateOpenAIModel
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setOrganizationForAITranslate(organization: String) {
        this.translateOrganization = organization
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getOrganizationForAITranslate(): String? {
        return translateOrganization
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setMaxResponseTokensForAITranslate(maxTokens: Int) {
        translateMaxResponseTokens = maxTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getMaxResponseTokensForAITranslate(): Int? {
        return translateMaxResponseTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setTemperatureForAITranslate(temperature: Float) {
        translateTemperature = temperature
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getTemperatureForAITranslate(): Float {
        return translateTemperature
    }

    fun disableAITranslate() {
        enabledAITranslate = false
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun isAITranslateEnabledWithOpenAIToken(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }

        if (translateSmartChatAssistantId.isNotBlank() && isExistAITokenAndProxyServer(
                translateOpenAIToken,
                translateProxyServerURL,
            )
        ) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return translateOpenAIToken.isNotBlank()
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun isAITranslateEnabledWithProxyServer(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }

        if (translateSmartChatAssistantId.isNotBlank() && isExistAITokenAndProxyServer(
                translateOpenAIToken,
                translateProxyServerURL
            )
        ) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return translateProxyServerURL.isNotBlank()
    }

    fun isAITranslateEnabledWithSmartChatAssistantId(): Boolean {
        val isNotEnabledAITranslate = !isEnabledAITranslate()
        if (isNotEnabledAITranslate) {
            throw RuntimeException("The AI Translate is disabled")
        }
        translateSmartChatAssistantId.isNotBlank()
        if (translateSmartChatAssistantId.isNotBlank() && isExistAITokenAndProxyServer(
                translateOpenAIToken,
                translateProxyServerURL
            )
        ) {
            throw RuntimeException("Error initialization. There are Open AI Token and Proxy Server Url. The AI Translate should be initialized with Open AI Token or Proxy server")
        }

        return translateSmartChatAssistantId.isNotBlank()
    }

    private fun isExistAITokenAndProxyServer(
        openAIToken: String,
        proxyServerURL: String,
    ): Boolean {
        return openAIToken.isNotBlank() && proxyServerURL.isNotBlank()
    }

    fun getAnswerAssistantSmartChatAssistantId(): String {
        return answerAssistantSmartChatAssistantId
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getAnswerAssistantProxyServerURL(): String {
        return answerAssistantProxyServerURL
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getAnswerAssistantOpenAIToken(): String {
        return answerAssistantOpenAIToken
    }

    fun getRephraseProxyServerURL(): String {
        return rephraseProxyServerURL
    }

    fun getRephraseOpenAIToken(): String {
        return rephraseOpenAIToken
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getTranslateProxyServerURL(): String {
        return translateProxyServerURL
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getTranslateOpenAIToken(): String {
        return translateOpenAIToken
    }

    fun getTranslateSmartChatAssistantId(): String {
        return translateSmartChatAssistantId
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setMaxRequestTokensForAIAnswerAssistant(maxTokens: Int) {
        answerAssistantMaxRequestTokens = maxTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getMaxRequestTokensForAIAnswerAssistant(): Int {
        return answerAssistantMaxRequestTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setOpenAIModelForAIAnswerAssistant(openAIModel: AnswerAssistantSettingsImpl.Model) {
        this.answerAssistantOpenAIModel = openAIModel
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getOpenAIModelForAIAnswerAssistant(): AnswerAssistantSettingsImpl.Model {
        return answerAssistantOpenAIModel
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setOrganizationForAIAnswerAssistant(organization: String) {
        this.answerAssistantOrganization = organization
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getOrganizationForAIAnswerAssistant(): String? {
        return answerAssistantOrganization
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setMaxResponseTokensForAIAnswerAssistant(maxTokens: Int) {
        answerAssistantMaxResponseTokens = maxTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun getMaxResponseTokensForAIAnswerAssistant(): Int? {
        return answerAssistantMaxResponseTokens
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun setTemperatureForAIAnswerAssistant(temperature: Float) {
        answerAssistantTemperature = temperature
    }

    @Deprecated("This method is deprecated and will be removed in future versions.")
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

    fun enableReply() {
        enabledReply = true
    }

    fun disableReply() {
        enabledReply = false
    }

    fun isEnabledReply(): Boolean {
        return enabledReply
    }

    fun setRegexUserName(regex: String) {
        regexUserName = regex
    }

    fun getRegexUserName(): String? {
        return regexUserName
    }
}