/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.USER_OPPONENT_ID_1
import com.quickblox.android_ui_kit.USER_OPPONENT_ID_2
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.usecases.CreateGroupDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetUsersFromDialogUseCase
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetUsersFromDialogUseCaseIntegrationTest : BaseTest() {
    var createdDialog: DialogEntity? = null

    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()
    }

    @After
    fun release() {
        deleteDialog(createdDialog)
        createdDialog = null

        logoutFromChat()
        logoutFromRest()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithOneUser_execute_receivedTwoUsers() = runBlocking {
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(USER_OPPONENT_ID_1).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        val loadedUsers = mutableListOf<UserEntity>()
        withContext(Dispatchers.Main) {
            runCatching {
                GetUsersFromDialogUseCase(createdDialog!!).execute()
            }.onSuccess { result ->
                loadedUsers.addAll(result)
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        assertEquals(2, loadedUsers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithTwoUsers_execute_receivedThreeUsers() = runBlocking {
        withContext(Dispatchers.Main) {
            runCatching {
                val dialogName = "name: ${System.currentTimeMillis()}"
                val participants = mutableListOf(USER_OPPONENT_ID_1, USER_OPPONENT_ID_2)
                CreateGroupDialogUseCase(dialogName, participants).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        val loadedUsers = mutableListOf<UserEntity>()
        withContext(Dispatchers.Main) {
            runCatching {
                GetUsersFromDialogUseCase(createdDialog!!).execute()
            }.onSuccess { result ->
                loadedUsers.addAll(result)
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        assertEquals(3, loadedUsers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createGroupDialogWithOnlyLoggedUser_execute_receivedOneUser() = runBlocking {
        withContext(Dispatchers.Main) {
            runCatching {
                val dialogName = "name: ${System.currentTimeMillis()}"
                CreateGroupDialogUseCase(dialogName).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details: $error")
            }
        }

        val loadedUsers = mutableListOf<UserEntity>()
        withContext(Dispatchers.Main) {
            runCatching {
                GetUsersFromDialogUseCase(createdDialog!!).execute()
            }.onSuccess { result ->
                loadedUsers.addAll(result)
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details: $error")
            }
        }

        assertEquals(1, loadedUsers.size)
    }
}