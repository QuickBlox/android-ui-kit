/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.features.forwarding.messages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.databinding.ContainerActivityBinding
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseActivity
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

private const val DIALOG_ID_EXTRA = "dialog_id"

class MessagesSelectionActivity : BaseActivity() {
    private lateinit var binding: ContainerActivityBinding

    companion object {
        private var themeUiKit: UiKitTheme? = LightUIKitTheme()
        private var messages: List<MessageEntity>? = null
        private var forwardedMessage: MessageEntity? = null
        private var paginationEntity: PaginationEntity? = null
        private var position: Int? = null

        fun show(
            context: Context,
            dialogId: String? = null,
            messages: List<MessageEntity>? = null,
            forwardedMessage: MessageEntity? = null,
            paginationEntity: PaginationEntity? = null,
            position: Int? = null,
            theme: UiKitTheme? = null,
        ) {
            themeUiKit = theme
            this.messages = messages
            this.forwardedMessage = forwardedMessage
            this.paginationEntity = paginationEntity
            this.position = position

            val intent = Intent(context, MessagesSelectionActivity::class.java)
            intent.putExtra(DIALOG_ID_EXTRA, dialogId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = QuickBloxUiKit.getTheme().getStatusBarColor()

        binding = ContainerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        val dialogId = intent.getStringExtra(DIALOG_ID_EXTRA)

        val fragment: Fragment = QuickBloxUiKit.getScreenFactory().createMessagesSelection(
            dialogId, themeUiKit, messages,
            forwardedMessage, paginationEntity, position
        )

        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
            .addToBackStack(MessagesSelectionFragment.TAG).commit()
    }
}