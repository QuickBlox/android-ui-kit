/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class LocalDataSourceSortTest : BaseTest() {
    @Before
    @ExperimentalCoroutinesApi
    fun init() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    @ExperimentalCoroutinesApi
    fun release() {
        Dispatchers.resetMain()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun saveListOfObjects_getListOfObjects_objectsAreSorted() = runTest {
        val localDataSource = LocalDataSourceImpl()

        localDataSource.saveDialog(buildLocalDialogDTO(3L))
        localDataSource.saveDialog(buildLocalDialogDTO(4L))
        localDataSource.saveDialog(buildLocalDialogDTO(5L))
        localDataSource.saveDialog(buildLocalDialogDTO(7L))
        localDataSource.saveDialog(buildLocalDialogDTO(1L))

        val DTO = localDataSource.getDialogs()
        val dialogs = DTO.dialogs
        assertEquals(7L, dialogs?.get(0)?.lastMessageDateSent)
        assertEquals(5L, dialogs?.get(1)?.lastMessageDateSent)
        assertEquals(4L, dialogs?.get(2)?.lastMessageDateSent)
        assertEquals(3L, dialogs?.get(3)?.lastMessageDateSent)
        assertEquals(1L, dialogs?.get(4)?.lastMessageDateSent)
    }

    private fun buildLocalDialogDTO(dateSent: Long): LocalDialogDTO {
        val localDialogDTO = LocalDialogDTO()
        localDialogDTO.lastMessageDateSent = dateSent
        localDialogDTO.id = Random.nextInt(10, 1000000).toString()

        return localDialogDTO
    }
}