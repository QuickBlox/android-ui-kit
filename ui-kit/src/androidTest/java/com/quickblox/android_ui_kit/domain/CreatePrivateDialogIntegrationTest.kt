/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.USER_OPPONENT_ID_1
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class CreatePrivateDialogIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null

    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    @After
    fun release() {
        deleteDialog(createdDialog)
        createdDialog = null

        logoutFromRest()
    }

    @Test
    fun correctOpponentId_execute_receiveDialogEntity() = runBlocking {
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(USER_OPPONENT_ID_1).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }

        assertTrue(createdDialog?.getDialogId()!!.isNotEmpty())
        assertTrue(createdDialog?.getParticipantIds()!!.contains(USER_OPPONENT_ID_1))
        assertTrue(createdDialog?.getUpdatedAt()!!.isNotEmpty())
        assertTrue(createdDialog?.getName()!!.isNotEmpty())
        assertEquals(createdDialog?.getType()!!, DialogEntity.Types.PRIVATE)
    }

    @Test
    fun negativeOpponentId_execute_error() = runBlocking {
        val errorLatch = CountDownLatch(1)
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(-1000).execute()
            }.onSuccess { result ->
                fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        errorLatch.await(20, TimeUnit.SECONDS)
        assertEquals(0, errorLatch.count)
    }

    @Test
    fun wrongOpponentId_execute_error() = runBlocking {
        val errorLatch = CountDownLatch(1)
        withContext(Dispatchers.Main) {
            runCatching {
                val userId = Random.nextInt(100000000, 1000000000)
                CreatePrivateDialogUseCase(userId).execute()
            }.onSuccess { result ->
                fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        errorLatch.await(20, TimeUnit.SECONDS)
        assertEquals(0, errorLatch.count)
    }
}