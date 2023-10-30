/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai

import com.quickblox.android_ai_answer_assistant.QBAIAnswerAssistant
import com.quickblox.android_ai_answer_assistant.settings.AnswerAssistantSettingsImpl
import com.quickblox.android_ai_editing_assistant.QBAIRephrase
import com.quickblox.android_ai_editing_assistant.exception.QBAIRephraseException
import com.quickblox.android_ai_editing_assistant.model.Tone
import com.quickblox.android_ai_editing_assistant.model.ToneImpl
import com.quickblox.android_ai_editing_assistant.settings.RephraseSettingsImpl
import com.quickblox.android_ai_translate.QBAITranslate
import com.quickblox.android_ai_translate.exception.QBAITranslateException
import com.quickblox.android_ai_translate.settings.TranslateSettingsImpl
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.dto.ai.*
import com.quickblox.android_ui_kit.data.source.ai.mapper.AIAnswerAssistantDTOMapper
import com.quickblox.android_ui_kit.data.source.ai.mapper.AIRephraseDTOMapper
import com.quickblox.android_ui_kit.data.source.ai.mapper.AITranslateDTOMapper
import com.quickblox.android_ui_kit.data.source.exception.AIDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

class AIDataSourceImpl : AIDataSource {
    val tones: MutableList<Tone> = crateDefaultTones()

    override fun translateIncomingMessageWithApiKey(
        translateDTO: AITranslateDTO,
        messagesDTO: List<AITranslateMessageDTO>,
    ): AITranslateDTO {
        val openAIToken = QuickBloxUiKit.getTranslateOpenAIToken()

        val text = translateDTO.content

        if (text.isNullOrEmpty()) {
            throw AIDataSourceException("Content should not be null or empty")
        }
        val language = QuickBloxUiKit.getAITranslateLanguage()

        val translateSettings = TranslateSettingsImpl(openAIToken, language)

        val maxRequestTokens = QuickBloxUiKit.getMaxRequestTokensForAITranslate()
        val openAIModel = QuickBloxUiKit.getOpenAIModelForAITranslate()
        val maxResponseTokens = QuickBloxUiKit.getMaxResponseTokensForAITranslate()
        val organization = QuickBloxUiKit.getOrganizationForAITranslate()
        val temperature = QuickBloxUiKit.getTemperatureForAITranslate()

        translateSettings.setMaxRequestTokens(maxRequestTokens)
        translateSettings.setModel(openAIModel)
        translateSettings.setMaxResponseTokens(maxResponseTokens)
        translateSettings.setOrganization(organization)
        translateSettings.setTemperature(temperature)

        try {
            val messages = AITranslateDTOMapper.dtosToMessages(messagesDTO)

            val translation = QBAITranslate.translateSync(text, translateSettings, messages)
            translateDTO.translation = translation

            return translateDTO
        } catch (exception: QBAITranslateException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun translateIncomingMessageWithProxyServer(
        translateDTO: AITranslateDTO,
        token: String,
        messagesDTO: List<AITranslateMessageDTO>,
    ): AITranslateDTO {
        val proxyServerURL = QuickBloxUiKit.getTranslateProxyServerURL()

        val text = translateDTO.content

        if (text.isNullOrEmpty()) {
            throw AIDataSourceException("Content should not be null or empty")
        }
        val language = QuickBloxUiKit.getAITranslateLanguage()

        val translateSettings = TranslateSettingsImpl(token, proxyServerURL, language, true)

        val maxRequestTokens = QuickBloxUiKit.getMaxRequestTokensForAITranslate()
        val openAIModel = QuickBloxUiKit.getOpenAIModelForAITranslate()
        val maxResponseTokens = QuickBloxUiKit.getMaxResponseTokensForAITranslate()
        val organization = QuickBloxUiKit.getOrganizationForAITranslate()
        val temperature = QuickBloxUiKit.getTemperatureForAITranslate()

        translateSettings.setMaxRequestTokens(maxRequestTokens)
        translateSettings.setModel(openAIModel)
        translateSettings.setMaxResponseTokens(maxResponseTokens)
        translateSettings.setOrganization(organization)
        translateSettings.setTemperature(temperature)

        try {
            val messages = AITranslateDTOMapper.dtosToMessages(messagesDTO)
            val translation = QBAITranslate.translateSync(text, translateSettings, messages)
            translateDTO.translation = translation

            return translateDTO
        } catch (exception: QBAITranslateException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun createAnswerWithApiKey(messagesDTO: List<AIAnswerAssistantMessageDTO>): String {
        val openAIToken = QuickBloxUiKit.getAnswerAssistantOpenAIToken()

        val answerAssistantSettings = AnswerAssistantSettingsImpl(openAIToken)

        val maxRequestTokens = QuickBloxUiKit.getMaxRequestTokensForAIAnswerAssistant()
        val openAIModel = QuickBloxUiKit.getOpenAIModelForAIAnswerAssistant()
        val maxResponseTokens = QuickBloxUiKit.getMaxResponseTokensForAIAnswerAssistant()
        val organization = QuickBloxUiKit.getOrganizationForAIAnswerAssistant()
        val temperature = QuickBloxUiKit.getTemperatureForAIAnswerAssistant()

        answerAssistantSettings.setMaxRequestTokens(maxRequestTokens)
        answerAssistantSettings.setModel(openAIModel)
        answerAssistantSettings.setMaxResponseTokens(maxResponseTokens)
        answerAssistantSettings.setOrganization(organization)
        answerAssistantSettings.setTemperature(temperature)

        try {
            val messages = AIAnswerAssistantDTOMapper.dtosToMessages(messagesDTO)

            val answer = QBAIAnswerAssistant.createAnswerSync(messages, answerAssistantSettings)
            return answer
        } catch (exception: QBAITranslateException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun createAnswerWithProxyServer(messagesDTO: List<AIAnswerAssistantMessageDTO>, token: String): String {
        val proxyServerURL = QuickBloxUiKit.getAnswerAssistantProxyServerURL()

        val answerAssistantSettings = AnswerAssistantSettingsImpl(token, proxyServerURL, true)

        val maxRequestTokens = QuickBloxUiKit.getMaxRequestTokensForAIAnswerAssistant()
        val openAIModel = QuickBloxUiKit.getOpenAIModelForAIAnswerAssistant()
        val maxResponseTokens = QuickBloxUiKit.getMaxResponseTokensForAIAnswerAssistant()
        val organization = QuickBloxUiKit.getOrganizationForAIAnswerAssistant()
        val temperature = QuickBloxUiKit.getTemperatureForAIAnswerAssistant()

        answerAssistantSettings.setMaxRequestTokens(maxRequestTokens)
        answerAssistantSettings.setModel(openAIModel)
        answerAssistantSettings.setMaxResponseTokens(maxResponseTokens)
        answerAssistantSettings.setOrganization(organization)
        answerAssistantSettings.setTemperature(temperature)

        try {
            val messages = AIAnswerAssistantDTOMapper.dtosToMessages(messagesDTO)

            val answer = QBAIAnswerAssistant.createAnswerSync(messages, answerAssistantSettings)
            return answer
        } catch (exception: QBAITranslateException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun rephraseWithApiKey(
        rephraseDTO: AIRephraseDTO,
        messagesDTO: List<AIRephraseMessageDTO>,
    ): AIRephraseDTO {
        val openAIToken = QuickBloxUiKit.getRephraseOpenAIToken()
        val toneName = rephraseDTO.tone.toneName
        val toneDescription = rephraseDTO.tone.descriptionTone
        val toneIcon = rephraseDTO.tone.icon

        val tone = ToneImpl(toneName, toneDescription, toneIcon)
        val rephraseSettings = RephraseSettingsImpl(openAIToken, tone)

        val maxRequestTokens = QuickBloxUiKit.getMaxRequestTokensForAIRephrase()
        val openAIModel = QuickBloxUiKit.getOpenAIModelForAIRephrase()
        val maxResponseTokens = QuickBloxUiKit.getMaxResponseTokensForAIRephrase()
        val organization = QuickBloxUiKit.getOrganizationForAIRephrase()
        val temperature = QuickBloxUiKit.getTemperatureForAIRephrase()

        rephraseSettings.setMaxRequestTokens(maxRequestTokens)
        rephraseSettings.setModel(openAIModel)
        rephraseSettings.setMaxResponseTokens(maxResponseTokens)
        rephraseSettings.setOrganization(organization)
        rephraseSettings.setTemperature(temperature)

        val text = rephraseDTO.originalText

        try {
            val messages = AIRephraseDTOMapper.dtosToMessages(messagesDTO)
            val results = QBAIRephrase.rephraseSync(text, rephraseSettings, messages)

            val resultDTO = AIRephraseDTOMapper.dtoToDtoWithRephrasedText(rephraseDTO, results)

            return resultDTO
        } catch (exception: QBAIRephraseException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun rephraseWithProxyServer(
        rephraseDTO: AIRephraseDTO,
        token: String,
        messagesDTO: List<AIRephraseMessageDTO>,
    ): AIRephraseDTO {
        val serverPath = QuickBloxUiKit.getRephraseProxyServerURL()

        val toneName = rephraseDTO.tone.toneName
        val toneDescription = rephraseDTO.tone.descriptionTone
        val toneIcon = rephraseDTO.tone.icon

        val tone = ToneImpl(toneName, toneDescription, toneIcon)
        val rephraseSettings = RephraseSettingsImpl(token, serverPath, tone, true)

        val maxRequestTokens = QuickBloxUiKit.getMaxRequestTokensForAIRephrase()
        val openAIModel = QuickBloxUiKit.getOpenAIModelForAIRephrase()
        val maxResponseTokens = QuickBloxUiKit.getMaxResponseTokensForAIRephrase()
        val organization = QuickBloxUiKit.getOrganizationForAIRephrase()
        val temperature = QuickBloxUiKit.getTemperatureForAIRephrase()

        rephraseSettings.setMaxRequestTokens(maxRequestTokens)
        rephraseSettings.setModel(openAIModel)
        rephraseSettings.setMaxResponseTokens(maxResponseTokens)
        rephraseSettings.setOrganization(organization)
        rephraseSettings.setTemperature(temperature)

        val text = rephraseDTO.originalText

        try {
            val messages = AIRephraseDTOMapper.dtosToMessages(messagesDTO)
            val results = QBAIRephrase.rephraseSync(text, rephraseSettings, messages)

            val resultDTO = AIRephraseDTOMapper.dtoToDtoWithRephrasedText(rephraseDTO, results)

            return resultDTO
        } catch (exception: QBAIRephraseException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun getAllRephraseTones(): List<AIRephraseToneDTO> {
        try {
            return AIRephraseDTOMapper.tonesToDtos(tones)
        } catch (exception: QBAIRephraseException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun setAllRephraseTones(rephraseTones: List<AIRephraseToneDTO>) {
        try {
            val tones = AIRephraseDTOMapper.dtosToTones(rephraseTones)
            this.tones.clear()
            this.tones.addAll(tones)
        } catch (exception: QBAIRephraseException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    private fun crateDefaultTones(): MutableList<Tone> {
        val tones = mutableListOf<Tone>()
        tones.add(
            ToneImpl(
                "Professional",
                "This would edit messages to sound more formal, using technical vocabulary, clear sentence structures, and maintaining a respectful tone. It would avoid colloquial language and ensure appropriate salutations and sign-offs.",
                "\uD83D\uDC54"
            )
        )

        tones.add(
            ToneImpl(
                "Friendly",
                "This would adjust messages to reflect a casual, friendly tone. It would incorporate casual language, use emoticons, exclamation points, and other informalities to make the message seem more friendly and approachable.",
                "\uD83E\uDD1D"
            )
        )
        tones.add(
            ToneImpl(
                "Encouraging",
                "This tone would be useful for motivation and encouragement. It would include positive words, affirmations, and express support and belief in the recipient.",
                "\uD83D\uDCAA"
            )
        )
        tones.add(
            ToneImpl(
                "Empathetic",
                "This tone would be utilized to display understanding and empathy. It would involve softer language, acknowledging feelings, and demonstrating compassion and support.",
                "\uD83E\uDD32"
            )
        )
        tones.add(
            ToneImpl(
                "Neutral",
                "For times when you want to maintain an even, unbiased, and objective tone. It would avoid extreme language and emotive words, opting for clear, straightforward communication.",
                "\uD83D\uDE10"
            )
        )
        tones.add(
            ToneImpl(
                "Assertive",
                "This tone is beneficial for making clear points, standing ground, or in negotiations. It uses direct language, is confident, and does not mince words.",
                "\uD83D\uDD28"
            )
        )
        tones.add(
            ToneImpl(
                "Instructive",
                "This tone would be useful for tutorials, guides, or other teaching and training materials. It is clear, concise, and walks the reader through steps or processes in a logical manner.",
                "\uD83D\uDCD6"
            )
        )
        tones.add(
            ToneImpl(
                "Persuasive",
                "This tone can be used when trying to convince someone or argue a point. It uses persuasive language, powerful words, and logical reasoning.",
                "☝️"
            )
        )
        tones.add(
            ToneImpl(
                "Sarcastic/Ironic",
                "This tone can make the communication more humorous or show an ironic stance. It is harder to implement as it requires the AI to understand nuanced language and may not always be taken as intended by the reader.",
                "\uD83D\uDE0F"
            )
        )
        tones.add(
            ToneImpl(
                "Poetic",
                "This would add an artistic touch to messages, using figurative language, rhymes, and rhythm to create a more expressive text.",
                "\uD83C\uDFAD"
            )
        )
        return tones
    }
}