/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.features.forwarding.recipients

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerRecipientsActivityBinding
import com.quickblox.android_ui_kit.databinding.SendMessageAttachmentPreviewBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseActivity
import com.quickblox.android_ui_kit.presentation.components.dialogs.DialogsComponentImpl
import com.quickblox.android_ui_kit.presentation.components.dialogs.DialogsSelectionAdapter
import com.quickblox.android_ui_kit.presentation.components.dialogs.DialogsSelectionAdapter.SelectionStrategy
import com.quickblox.android_ui_kit.presentation.components.dialogs.DialogsSelectionAdapter.SingleSelectionCompleteListener
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponent.MessageComponentStates.FORWARDING_MESSAGE
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponentListenerImpl
import com.quickblox.android_ui_kit.presentation.hideKeyboard
import com.quickblox.android_ui_kit.presentation.listeners.ImageLoadListenerWithProgress
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.features.forwarding.messages.MessagesSelectionFragment
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

private const val DIALOG_ID_EXTRA = "dialog_id"

class RecipientSelectionActivity : BaseActivity() {
    private var selectedDialogs: List<DialogEntity>? = null

    private lateinit var binding: ContainerRecipientsActivityBinding
    private val viewModel by lazy {
        ViewModelProvider(this)[RecipientSelectionActivityViewModel::class.java]
    }

    companion object {
        private var themeUiKit: UiKitTheme? = LightUIKitTheme()
        private var forwardedMessage: MessageEntity? = null

        fun show(
            context: Context,
            dialogId: String? = null,
            forwardedMessage: MessageEntity? = null,
            theme: UiKitTheme? = null,
        ) {
            themeUiKit = theme
            this.forwardedMessage = forwardedMessage


            val intent = Intent(context, RecipientSelectionActivity::class.java)
            intent.putExtra(DIALOG_ID_EXTRA, dialogId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = QuickBloxUiKit.getTheme().getStatusBarColor()

        binding = ContainerRecipientsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        val dialogsComponent = DialogsComponentImpl(this)

        val dialogId = intent.getStringExtra(DIALOG_ID_EXTRA)

        val adapter = DialogsSelectionAdapter()

        adapter.setForwardedFromDialogId(dialogId)
        adapter.setSelectionStrategy(SelectionStrategy.SINGLE)
        adapter.setSingleSelectionCompleteListener(object : SingleSelectionCompleteListener {
            override fun onSingleSelectionCompleted(isCompleted: Boolean) {
                selectedDialogs = adapter.getSelectedDialogs()
            }
        })

        dialogsComponent.setAdapter(adapter)
        dialogsComponent.setTheme(themeUiKit!!)
        dialogsComponent.showSearch(true)
        dialogsComponent.setItemClickListener {}

        val settings: DialogsScreenSettings =
            DialogsScreenSettings.Builder(this).setTheme(themeUiKit!!).showHeader(false)
                .setDialogsComponent(dialogsComponent)
                .build()

        val fragment: Fragment = QuickBloxUiKit.getScreenFactory().createDialogs(settings)
        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
            .addToBackStack(MessagesSelectionFragment.TAG).commit()

        val message = forwardedMessage as ChatMessageEntity
        showAttachmentMessage(message)

        binding.root.background = ColorDrawable(themeUiKit!!.getMainBackgroundColor())
        binding.tvTitle.setTextColor(themeUiKit!!.getMainTextColor())
        binding.vDivider.setBackgroundColor(themeUiKit!!.getDividerColor())
        binding.btnRight.setTextColor(themeUiKit!!.getMainElementsColor())
        binding.progressBar.indeterminateTintList = themeUiKit?.getMainElementsColor()
            ?.let { ColorStateList.valueOf(it) }

        binding.sendMessage.isShowButtonAttachment(false)
        binding.sendMessage.setComponentMessageState(FORWARDING_MESSAGE)

        initSendMessagesComponentListeners()

        binding.btnRight.setOnClickListener {
            finishAffinity()
        }

        subscribeToReceivedMessage()
    }

    private fun buildAttachmentPreviewBinding(): SendMessageAttachmentPreviewBinding {
        val inflater = LayoutInflater.from(binding.root.context)

        return SendMessageAttachmentPreviewBinding.inflate(inflater)
    }

    private fun showAttachmentMessage(message: ChatMessageEntity) {
        val attachmentPreviewBinding = buildAttachmentPreviewBinding()

        attachmentPreviewBinding.ivIcon.setImageResource(R.drawable.froward)
        themeUiKit?.getSecondaryTextColor()?.let {
            attachmentPreviewBinding.ivIcon.setColorFilter(it)
            attachmentPreviewBinding.tvActionText.setTextColor(it)
        }

        attachmentPreviewBinding.tvActionText.text =
            getString(R.string.forwarded_from_with_name, message.getSender()?.getName())

        val mediaContent = message.getMediaContent()
        if (mediaContent == null) {
            attachmentPreviewBinding.ivMediaIcon.visibility = View.GONE
            showTextMessage(message, attachmentPreviewBinding)
        } else {
            showMediaMessage(mediaContent, attachmentPreviewBinding)
        }

        val container = binding.sendMessage.getAttachmentContainer()
        container?.visibility = View.VISIBLE

        themeUiKit?.let {
            binding.sendMessage.setTheme(it)
        }

        container?.addView(attachmentPreviewBinding.root)
    }

    private fun subscribeToReceivedMessage() {
        viewModel.sentMessage.observe(this) { dialogName ->
            binding.progressBar.visibility = View.GONE
            binding.sendMessage.hideKeyboard()
            Toast.makeText(this, getString(R.string.message_forwarded_to, dialogName), Toast.LENGTH_LONG).show()
            finishAffinity()
        }
    }

    private fun showTextMessage(
        message: ChatMessageEntity,
        attachmentPreviewBinding: SendMessageAttachmentPreviewBinding,
    ) {
        attachmentPreviewBinding.tvText.text = message.getContent()
        attachmentPreviewBinding.tvText.textSize = 16F

        themeUiKit?.getMainTextColor()?.let {
            attachmentPreviewBinding.tvText.setTextColor(it)
        }
    }

    private fun showMediaMessage(
        mediaContent: MediaContentEntity,
        attachmentPreviewBinding: SendMessageAttachmentPreviewBinding,
    ) {
        attachmentPreviewBinding.tvText.text = mediaContent.getName()

        val resourceId = getResourceIdByMediaContent(mediaContent.getType())

        val context = binding.root.context
        attachmentPreviewBinding.ivMediaIcon.background =
            ContextCompat.getDrawable(context, R.drawable.bg_media_message)
        attachmentPreviewBinding.ivMediaIcon.setImageResource(resourceId)

        if (mediaContent.isImage()) {
            val progressBar = attachmentPreviewBinding.progressBar

            val backgroundImageView = attachmentPreviewBinding.ivMediaIcon
            backgroundImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            loadImageByUrl(mediaContent.getUrl(), backgroundImageView, progressBar)
        }
        themeUiKit?.getCaptionColor()?.let {
            attachmentPreviewBinding.tvText.setTextColor(it)
        }
    }

    private fun getResourceIdByMediaContent(contentType: MediaContentEntity.Types?): Int {
        when (contentType) {
            MediaContentEntity.Types.IMAGE -> {
                return R.drawable.ic_image_placeholder
            }
            MediaContentEntity.Types.VIDEO -> {
                return R.drawable.ic_video_file
            }
            MediaContentEntity.Types.AUDIO -> {
                return R.drawable.ic_audio_file
            }
            MediaContentEntity.Types.FILE -> {
                return R.drawable.ic_application_file
            }
            else -> {
                throw IllegalArgumentException("$contentType - type does not exist for media content")
            }
        }
    }

    private fun initSendMessagesComponentListeners() {
        val sendMessageComponent = binding.sendMessage

        val listener = sendMessageComponent.getSendMessageComponentListener()
        if (listener == null) {
            sendMessageComponent.setSendMessageComponentListener(object : SendMessageComponentListenerImpl() {
                override fun onSendTextMessageClickListener(textMessage: String) {
                    if (selectedDialogs?.isNotEmpty() == true) {
                        binding.progressBar.visibility = View.VISIBLE
                        viewModel.createAndSendMessage(
                            listOf(forwardedMessage),
                            selectedDialogs?.get(0),
                            textMessage
                        )
                    } else {
                        Toast.makeText(this@RecipientSelectionActivity, "Please select dialog", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
        }
    }

    private fun loadImageByUrl(url: String?, imageView: AppCompatImageView, progressBar: ProgressBar) {
        val context = binding.root.context

        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder))
            .listener(ImageLoadListenerWithProgress(imageView, context, progressBar))
            .into(imageView)
    }
}