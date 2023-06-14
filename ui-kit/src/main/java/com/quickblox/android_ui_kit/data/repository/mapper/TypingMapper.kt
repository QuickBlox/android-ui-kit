/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.remote.typing.RemoteTypingDTO
import com.quickblox.android_ui_kit.domain.entity.TypingEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

object TypingMapper {
    fun parseTypeFrom(type: RemoteTypingDTO.Types?): TypingEntity.TypingTypes {
        when (type) {
            RemoteTypingDTO.Types.STARTED -> {
                return TypingEntity.TypingTypes.STARTED
            }
            RemoteTypingDTO.Types.STOPPED -> {
                return TypingEntity.TypingTypes.STOPPED
            }
            else -> {
                throw MappingException("The type in RemoteTypingDTO.Types shouldn't be empty")
            }
        }
    }

    fun getSenderIdFrom(senderId: Int?): Int {
        if (senderId == null || senderId < 0) {
            throw MappingException("The senderId RemoteTypingDTO should have positive value")
        }
        return senderId
    }
}