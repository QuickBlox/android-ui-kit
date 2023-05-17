/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.UserEntityImpl
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.random.Random

class UserEntityTest {

    @Test
    fun buildUserEntityStub_CompareEntities_EqualsTrue() {
        val entity = buildUserEntityStub()
        val otherEntity = buildUserEntityStub()

        Assert.assertTrue(entity == otherEntity)
        Assert.assertTrue(entity.hashCode() == otherEntity.hashCode())
    }

    @Test
    fun buildRandomUserEntityStub_CompareEntities_EqualsFalse() {
        val entity = buildRandomUserEntityStub()
        val otherEntity = buildRandomUserEntityStub()

        Assert.assertFalse(entity == otherEntity)
        Assert.assertFalse(entity.hashCode() == otherEntity.hashCode())
    }

    private fun buildUserEntityStub(): UserEntity {
        val entity = UserEntityImpl()

        entity.setUserId(123456)
        entity.setName("test_name")
        entity.setEmail("test_email")
        entity.setLogin("test_login")
        entity.setPhone("test_phone")
        entity.setWebsite("test_website")
        entity.setExternalId("test_external_id")
        entity.setFacebookId("test_facebook_id")
        entity.setAvatarUrl("")
        entity.setTags(arrayListOf("test"))
        entity.setCustomData("test_custom_data")

        return entity
    }

    private fun buildRandomUserEntityStub(): UserEntityImpl {
        val entity = UserEntityImpl()

        entity.setUserId(Random.nextInt())
        entity.setName("test_name ${Random.nextInt(0, 1000)}")
        entity.setEmail("test_email ${Random.nextInt(0, 1000)}")
        entity.setLogin("test_login ${Random.nextInt(0, 1000)}")
        entity.setPhone("test_phone ${Random.nextInt(0, 1000)}")
        entity.setWebsite("test_website ${Random.nextInt(0, 1000)}")
        entity.setLastRequestAt(Date())
        entity.setExternalId("test_external_id ${Random.nextInt(0, 1000)}")
        entity.setFacebookId("test_facebook_id ${Random.nextInt(0, 1000)}")
        entity.setAvatarUrl("")
        entity.setTags(arrayListOf("test ${Random.nextInt(0, 1000)}"))
        entity.setCustomData("test_custom_data ${Random.nextInt(0, 1000)}")

        return entity
    }
}