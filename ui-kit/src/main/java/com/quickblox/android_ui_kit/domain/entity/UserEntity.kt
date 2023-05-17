/*
 * Created by Injoit on 20.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity

import java.util.*

interface UserEntity {
    fun getUserId(): Int?
    fun setUserId(userId: Int?)

    fun getName(): String?
    fun setName(name: String?)

    fun getEmail(): String?
    fun setEmail(email: String?)

    fun getLogin(): String?
    fun setLogin(login: String?)

    fun getPhone(): String?
    fun setPhone(phone: String?)

    fun getWebsite(): String?
    fun setWebsite(website: String?)

    fun getLastRequestAt(): Date?
    fun setLastRequestAt(lastRequestAt: Date?)

    fun getExternalId(): String?
    fun setExternalId(externalId: String?)

    fun getFacebookId(): String?
    fun setFacebookId(facebookId: String?)

    fun getAvatarUrl(): String?
    fun setAvatarUrl(url: String?)

    fun getTags(): ArrayList<String>?
    fun setTags(tags: ArrayList<String>?)

    fun getCustomData(): String?
    fun setCustomData(customData: String?)

    override fun equals(other: Any?): Boolean
}