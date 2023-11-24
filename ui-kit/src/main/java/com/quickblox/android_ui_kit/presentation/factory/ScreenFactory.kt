/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.factory

import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.screens.chat.group.GroupChatScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.chat.individual.PrivateChatScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.create.name.DialogNameScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.create.users.UsersScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.add.AddMembersScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.group.GroupChatInfoScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.individual.PrivateChatInfoScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.members.MembersScreenSettings
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

interface ScreenFactory {
    fun createDialogs(screenSettings: DialogsScreenSettings? = null): Fragment
    fun createDialogName(dialogEntity: DialogEntity? = null, screenSettings: DialogNameScreenSettings? = null): Fragment
    fun createUsers(dialogEntity: DialogEntity? = null, screenSettings: UsersScreenSettings? = null): Fragment
    fun createPrivateChat(dialogId: String? = null, screenSettings: PrivateChatScreenSettings? = null): Fragment
    fun createGroupChat(dialogId: String? = null, screenSettings: GroupChatScreenSettings? = null): Fragment
    fun createGroupChatInfo(dialogId: String? = null, screenSettings: GroupChatInfoScreenSettings? = null): Fragment
    fun createPrivateChatInfo(dialogId: String? = null, screenSettings: PrivateChatInfoScreenSettings? = null): Fragment
    fun createMembers(dialogId: String? = null, screenSettings: MembersScreenSettings? = null): Fragment
    fun createAddMembers(dialogId: String? = null, screenSettings: AddMembersScreenSettings? = null): Fragment
    fun createMessagesSelection(
        dialogId: String? = null,
        theme: UiKitTheme? = null,
        messages: List<MessageEntity>? = null,
        forwardedMessage: MessageEntity? = null,
        paginationEntity: PaginationEntity? = null,
        position: Int? = null,
    ): Fragment
}