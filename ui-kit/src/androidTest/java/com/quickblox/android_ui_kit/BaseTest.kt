/*
 * Created by Injoit on 7.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.repository.*
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

const val APP_ID = ""
const val AUTH_KEY = ""
const val AUTH_SECRET = ""
const val ACCOUNT_KEY = ""

const val USER_LOGIN = ""
const val USER_ID = 0
const val USER_PASSWORD = ""

const val OPPONENT_LOGIN = ""
const val OPPONENT_ID = 0
const val OPPONENT_PASSWORD = ""

const val USER_OPPONENT_ID_1 = 0
const val USER_OPPONENT_ID_2 = 0

open class BaseTest {
    private var loggedUser: QBUser? = null

    protected fun initQuickblox() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QBSettings.getInstance().init(context, APP_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY

        initQBChatService()
    }

    private fun initQBChatService() {
        QBChatService.setConfigurationBuilder(buildChatConfig())
        QBChatService.setDefaultPacketReplyTimeout(10000)
        QBChatService.setDebugEnabled(true)
    }

    private fun buildChatConfig(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()
        configurationBuilder.socketTimeout = 500
        configurationBuilder.preferredResumptionTime = 500
        configurationBuilder.isKeepAlive = true
        configurationBuilder.isReconnectionAllowed = true
        configurationBuilder.setAllowListenNetwork(true)
        return configurationBuilder
    }

    protected fun loginToRest() {
        loggedUser = QBUsers.signIn(buildUser()).perform()
    }

    private fun buildUser(): QBUser {
        return QBUser(USER_LOGIN, USER_PASSWORD)
    }

    protected fun loginToChat() {
        loggedUser?.password = USER_PASSWORD
        QBChatService.getInstance().login(loggedUser)
    }

    protected fun logoutFromRest() {
        QBUsers.signOut().perform()
    }

    protected fun logoutFromChat() {
        QBChatService.getInstance().destroy()
    }

    protected fun deleteDialog(dialog: QBChatDialog?) {
        dialog?.dialogId?.let { dialogId ->
            try {
                QBRestChatService.deleteDialog(dialogId, true).perform()
            } catch (exception: QBResponseException) {
                println(exception)
            }
        }
    }

    protected fun deleteDialog(dialog: DialogEntity?) {
        dialog?.getDialogId()?.let { dialogId ->
            try {
                QBRestChatService.deleteDialog(dialogId, true).perform()
            } catch (exception: QBResponseException) {
                println(exception)
            }
        }
    }

    protected fun deleteDialog(dialogId: String?) {
        dialogId?.let {
            try {
                QBRestChatService.deleteDialog(dialogId, true).perform()
            } catch (exception: QBResponseException) {
                println(exception)
            }
        }
    }

    open class DependencyStub : Dependency {
        override fun getConnectionRepository(): ConnectionRepository {
            throw RuntimeException("expected: override, actual: not override")
        }

        override fun getDialogsRepository(): DialogsRepository {
            throw RuntimeException("expected: override, actual: not override")
        }

        override fun getFilesRepository(): FilesRepository {
            throw RuntimeException("expected: override, actual: not override")
        }

        override fun getMessagesRepository(): MessagesRepository {
            throw RuntimeException("expected: override, actual: not override")
        }

        override fun getUsersRepository(): UsersRepository {
            throw RuntimeException("expected: override, actual: not override")
        }

        override fun getEventsRepository(): EventsRepository {
            throw RuntimeException("expected: override, actual: not override")
        }

        override fun getAIRepository(): AIRepository {
            throw RuntimeException("expected: override, actual: not override")
        }
    }
}