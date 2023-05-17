/*
 * Created by Injoit on 18.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.entity

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import java.util.*

open class UserEntityStub : UserEntity {
    override fun getUserId(): Int {
        throw RuntimeException()
    }

    override fun setUserId(userId: Int?) {
        throw RuntimeException()
    }

    override fun getName(): String? {
        throw RuntimeException()
    }

    override fun setName(name: String?) {
        throw RuntimeException()
    }

    override fun getEmail(): String? {
        throw RuntimeException()
    }

    override fun setEmail(email: String?) {
        throw RuntimeException()
    }

    override fun getLogin(): String? {
        throw RuntimeException()
    }

    override fun setLogin(login: String?) {
        throw RuntimeException()
    }

    override fun getPhone(): String? {
        throw RuntimeException()
    }

    override fun setPhone(phone: String?) {
        throw RuntimeException()
    }

    override fun getWebsite(): String? {
        throw RuntimeException()
    }

    override fun setWebsite(website: String?) {
        throw RuntimeException()
    }

    override fun getLastRequestAt(): Date? {
        throw RuntimeException()
    }

    override fun setLastRequestAt(lastRequestAt: Date?) {
        throw RuntimeException()
    }

    override fun getExternalId(): String? {
        throw RuntimeException()
    }

    override fun setExternalId(externalId: String?) {
        throw RuntimeException()
    }

    override fun getFacebookId(): String? {
        throw RuntimeException()
    }

    override fun setFacebookId(facebookId: String?) {
        throw RuntimeException()
    }

    override fun getAvatarUrl(): String? {
        throw RuntimeException()
    }

    override fun setAvatarUrl(url: String?) {
        throw RuntimeException()
    }

    override fun getTags(): ArrayList<String>? {
        throw RuntimeException()
    }

    override fun setTags(tags: ArrayList<String>?) {
        throw RuntimeException()
    }

    override fun getCustomData(): String? {
        throw RuntimeException()
    }

    override fun setCustomData(customData: String?) {
        throw RuntimeException()
    }

    override fun equals(other: Any?): Boolean {
        throw RuntimeException()
    }
}