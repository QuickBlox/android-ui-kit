/*
 * Created by Injoit on 17.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity.implementation

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import java.util.*

class UserEntityImpl : UserEntity {
    private var userId: Int? = null
    private var name: String? = null
    private var email: String? = null
    private var login: String? = null
    private var phone: String? = null
    private var website: String? = null
    private var lastRequestAt: Date? = null
    private var externalId: String? = null
    private var facebookId: String? = null
    private var avatarUrl: String? = null
    private var tags: ArrayList<String>? = null
    private var customData: String? = null

    override fun getUserId(): Int? {
        return userId
    }

    override fun setUserId(userId: Int?) {
        this.userId = userId
    }

    override fun getName(): String? {
        return name
    }

    override fun setName(name: String?) {
        this.name = name
    }

    override fun getEmail(): String? {
        return email
    }

    override fun setEmail(email: String?) {
        this.email = email
    }

    override fun getLogin(): String? {
        return login
    }

    override fun setLogin(login: String?) {
        this.login = login
    }

    override fun getPhone(): String? {
        return phone
    }

    override fun setPhone(phone: String?) {
        this.phone = phone
    }

    override fun getWebsite(): String? {
        return website
    }

    override fun setWebsite(website: String?) {
        this.website = website
    }

    override fun getLastRequestAt(): Date? {
        return lastRequestAt
    }

    override fun setLastRequestAt(lastRequestAt: Date?) {
        this.lastRequestAt = lastRequestAt
    }

    override fun getExternalId(): String? {
        return externalId
    }

    override fun setExternalId(externalId: String?) {
        this.externalId = externalId
    }

    override fun getFacebookId(): String? {
        return facebookId
    }

    override fun setFacebookId(facebookId: String?) {
        this.facebookId = facebookId
    }

    override fun getAvatarUrl(): String? {
        return avatarUrl
    }

    override fun setAvatarUrl(url: String?) {
        this.avatarUrl = url
    }

    override fun getTags(): ArrayList<String>? {
        return tags
    }

    override fun setTags(tags: ArrayList<String>?) {
        this.tags = tags
    }

    override fun getCustomData(): String? {
        return customData
    }

    override fun setCustomData(customData: String?) {
        this.customData = customData
    }

    override fun equals(other: Any?): Boolean {
        return if (other is UserEntityImpl) {
            userId == other.getUserId() && name == other.getName() && email == other.getEmail()
                    && login == other.getLogin() && phone == other.getPhone()
                    && website == other.getWebsite() && lastRequestAt == other.getLastRequestAt()
                    && externalId == other.getExternalId() && facebookId == other.getFacebookId()
                    && avatarUrl == other.getAvatarUrl() && tags == other.getTags()
                    && customData == other.getCustomData()
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + userId.hashCode()
        return hash
    }
}