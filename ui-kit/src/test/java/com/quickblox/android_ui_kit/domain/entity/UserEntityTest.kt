/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.UserEntityImpl
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import java.util.*
import kotlin.random.Random

class UserEntityTest {
    @Test
    fun buildUserEntityStub_CompareEntities_EqualsTrue() {
        val userA = buildUserEntityStub()
        val userB = buildUserEntityStub()

        assertTrue(userA == userB)
        assertTrue(userA.hashCode() == userB.hashCode())
    }

    @Test
    fun buildEmptyUserEntity_CompareEntities_EqualsFalse() {
        val userA = UserEntityImpl()
        val userB = UserEntityImpl()

        assertFalse(userA == userB)
        assertFalse(userA.hashCode() == userB.hashCode())
    }

    @Test
    fun build2UserEntityWithUserId_CompareHashCodes_EqualsTrue() {
        val userA = UserEntityImpl()
        userA.setUserId(888)

        val userB = UserEntityImpl()
        userB.setUserId(888)

        assertTrue(userA == userB)
        assertTrue(userA.hashCode() == userB.hashCode())
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
        entity.setAvatarUrl("https://avatart.com/avatar.png")
        entity.setTags(arrayListOf("test_tag"))
        entity.setCustomData("test_custom_data")

        return entity
    }

    @Test
    fun buildRandomUserEntityStub_CompareEntities_EqualsFalse() {
        val userA = buildRandomUserEntityStub()
        val userB = buildRandomUserEntityStub()

        assertFalse(userA == userB)
        assertFalse(userA.hashCode() == userB.hashCode())
    }

    private fun buildRandomUserEntityStub(): UserEntityImpl {
        val entity = UserEntityImpl()

        entity.setUserId(Random.nextInt(1000, 2000))
        entity.setName("test_name ${Random.nextInt(0, 100)}")
        entity.setEmail("test_email ${Random.nextInt(100, 200)}")
        entity.setLogin("test_login ${Random.nextInt(200, 300)}")
        entity.setPhone("test_phone ${Random.nextInt(300, 400)}")
        entity.setWebsite("test_website ${Random.nextInt(500, 600)}")
        entity.setLastRequestAt(Date())
        entity.setExternalId("test_external_id ${Random.nextInt(600, 700)}")
        entity.setFacebookId("test_facebook_id ${Random.nextInt(700, 800)}")
        entity.setAvatarUrl("")
        entity.setTags(arrayListOf("test ${Random.nextInt(800, 900)}"))
        entity.setCustomData("test_custom_data ${Random.nextInt(900, 1000)}")

        return entity
    }
}