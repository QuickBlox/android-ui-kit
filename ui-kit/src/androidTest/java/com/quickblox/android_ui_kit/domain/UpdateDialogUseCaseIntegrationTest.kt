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
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.UpdateDialogUseCase
import com.quickblox.content.model.QBFile
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
class UpdateDialogUseCaseIntegrationTest : BaseTest() {
    var createdDialog: DialogEntity? = null

    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
    }

    @After
    fun release() {
        deleteDialog(createdDialog)
        createdDialog = null

        logoutFromRest()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialog_changeDialogPhotoAndExecute_dialogExistWithUpdatedPhoto() = runBlocking {
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(USER_OPPONENT_ID_1).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }

        val updatedPhoto = "updated photo: ${System.currentTimeMillis()}"
        createdDialog?.setPhoto(updatedPhoto)

        var loadedDialog: DialogEntity? = null
        withContext(Dispatchers.Main) {
            runCatching {
                UpdateDialogUseCase(createdDialog!!).execute()
            }.onSuccess { result ->
                loadedDialog = result
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }

        assertEquals(createdDialog?.getDialogId(), loadedDialog?.getDialogId())
        assertEquals(QBFile.getPrivateUrlForUID(updatedPhoto), loadedDialog?.getPhoto())
    }
}