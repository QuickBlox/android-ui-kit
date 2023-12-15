/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.factory

import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.screens.chat.group.GroupChatFragment
import com.quickblox.android_ui_kit.presentation.screens.chat.group.GroupChatScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.chat.individual.PrivateChatFragment
import com.quickblox.android_ui_kit.presentation.screens.chat.individual.PrivateChatScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.create.name.DialogNameFragment
import com.quickblox.android_ui_kit.presentation.screens.create.name.DialogNameScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.create.users.UsersFragment
import com.quickblox.android_ui_kit.presentation.screens.create.users.UsersScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsFragment
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.features.forwarding.messages.MessagesSelectionFragment
import com.quickblox.android_ui_kit.presentation.screens.info.add.AddMembersFragment
import com.quickblox.android_ui_kit.presentation.screens.info.add.AddMembersScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.group.GroupChatInfoFragment
import com.quickblox.android_ui_kit.presentation.screens.info.group.GroupChatInfoScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.individual.PrivateChatInfoFragment
import com.quickblox.android_ui_kit.presentation.screens.info.individual.PrivateChatInfoScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.info.members.MembersFragment
import com.quickblox.android_ui_kit.presentation.screens.info.members.MembersScreenSettings
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

open class DefaultScreenFactory : ScreenFactory {
    override fun createDialogs(screenSettings: DialogsScreenSettings?): Fragment {
        return DialogsFragment.newInstance(screenSettings)
    }

    override fun createDialogName(dialogEntity: DialogEntity?, screenSettings: DialogNameScreenSettings?): Fragment {
        return DialogNameFragment.newInstance(dialogEntity, screenSettings)
    }

    override fun createUsers(dialogEntity: DialogEntity?, screenSettings: UsersScreenSettings?): Fragment {
        return UsersFragment.newInstance(dialogEntity, screenSettings)
    }

    override fun createPrivateChat(dialogId: String?, screenSettings: PrivateChatScreenSettings?): Fragment {
        return PrivateChatFragment.newInstance(dialogId, screenSettings)
    }

    override fun createGroupChat(dialogId: String?, screenSettings: GroupChatScreenSettings?): Fragment {
        return GroupChatFragment.newInstance(dialogId, screenSettings)
    }

    override fun createGroupChatInfo(dialogId: String?, screenSettings: GroupChatInfoScreenSettings?): Fragment {
        return GroupChatInfoFragment.newInstance(dialogId, screenSettings)
    }

    override fun createPrivateChatInfo(dialogId: String?, screenSettings: PrivateChatInfoScreenSettings?): Fragment {
        return PrivateChatInfoFragment.newInstance(dialogId, screenSettings)
    }

    override fun createMembers(dialogId: String?, screenSettings: MembersScreenSettings?): Fragment {
        return MembersFragment.newInstance(dialogId, screenSettings)
    }

    override fun createAddMembers(dialogId: String?, screenSettings: AddMembersScreenSettings?): Fragment {
        return AddMembersFragment.newInstance(dialogId, screenSettings)
    }

    override fun createMessagesSelection(
        dialogId: String?,
        theme: UiKitTheme?,
        messages: List<MessageEntity>?,
        forwardedMessage: MessageEntity?,
        paginationEntity: PaginationEntity?,
        position: Int?,
    ): Fragment {
        return MessagesSelectionFragment.newInstance(
            dialogId,
            theme,
            messages,
            forwardedMessage,
            paginationEntity,
            position
        )
    }
}