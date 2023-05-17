/*
 * Created by Injoit on 14.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote

import com.quickblox.android_ui_kit.BaseTest
import junit.framework.Assert.assertEquals
import org.junit.Test

class BuildRequestRuleToExcludeUserIdsTest : BaseTest() {
    @Test
    fun correctUserIds_makeRequest_receivedCorrectRule() {
        val remoteDataSource = RemoteDataSourceImpl()

        val rule = remoteDataSource.makeRequestRuleToExcludeUserId(777)

        val paramName = rule.paramName
        val paramValue = rule.value

        assertEquals("filter[]", paramName)
        assertEquals("number id ne 777", paramValue)
    }
}