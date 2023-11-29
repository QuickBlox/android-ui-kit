/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.chat.group

import android.Manifest
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.ACTION_REQUEST_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.databinding.PopupChatLayoutBinding
import com.quickblox.android_ui_kit.databinding.SendMessagePreviewBinding
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ContentTypes
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity.Types
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.components.send.Recorder
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponentListenerImpl
import com.quickblox.android_ui_kit.presentation.dialogs.AIMenuDialog
import com.quickblox.android_ui_kit.presentation.dialogs.OkDialog
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.listeners.ImageLoadListenerWithProgress
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.chat.CameraResultContract
import com.quickblox.android_ui_kit.presentation.screens.chat.EXTRA_DATA
import com.quickblox.android_ui_kit.presentation.screens.chat.PermissionsContract
import com.quickblox.android_ui_kit.presentation.screens.chat.full_image_screen.FullImageScreenActivity
import com.quickblox.android_ui_kit.presentation.screens.chat.group.GroupChatViewModel.TypingEvents
import com.quickblox.android_ui_kit.presentation.screens.features.forwarding.messages.MessagesSelectionActivity
import com.quickblox.android_ui_kit.presentation.screens.info.group.GroupChatInfoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val CAMERA_PERMISSION = "android.permission.CAMERA"
private const val RECORD_AUDIO_PERMISSION = "android.permission.RECORD_AUDIO"

open class GroupChatFragment : BaseFragment() {
    companion object {
        val TAG: String = GroupChatFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null, screenSettings: GroupChatScreenSettings? = null,
        ): GroupChatFragment {
            val privateChatFragment = GroupChatFragment()
            privateChatFragment.dialogId = dialogId
            privateChatFragment.screenSettings = screenSettings
            return privateChatFragment
        }
    }

    private var repliedMessage: ForwardedRepliedMessageEntity? = null
    private val viewModel by viewModels<GroupChatViewModel>()
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: GroupChatScreenSettings? = null
    private var dialogId: String? = null
    private var scrollListenerImpl = ScrollListenerImpl()

    private val requestCameraPermissionLauncher = registerCameraPermissionLauncher()
    private val requestAudioRecordPermissionLauncher = registerAudioRecordPermissionLauncher()

    private var photoCameraLauncher = registerPhotoCameraLauncher()
    private var videoCameraLauncher = registerVideoCameraLauncher()
    private var photoAndVideoGalleryLauncher = registerPhotoAndVideoGalleryLauncher()
    private var fileLauncher = registerFileLauncher()

    private var originalText: String = ""
    private var needToSetText = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = GroupChatScreenSettings.Builder(requireContext()).build()
        }

        initHeaderComponentListeners()
        initMessagesComponentListeners()
        initSendMessagesComponentListeners()

        viewModel.getAllTones()
    }

    override fun onResume() {
        if (TextUtils.isEmpty(dialogId)) {
            showToast("The dialogId shouldn't be empty")
        } else {
            viewModel.loadDialogAndMessages(dialogId.toString())
            screenSettings?.getMessagesComponent()?.getAdapter()?.notifyDataSetChanged()
        }
        super.onResume()
    }

    private fun initHeaderComponentListeners() {
        val headerComponent = screenSettings?.getHeaderComponent()

        val leftClickListener = headerComponent?.getLeftButtonClickListener()
        if (leftClickListener == null) {
            headerComponent?.setLeftButtonClickListener { backPressed() }
        }

        val rightClickListener = headerComponent?.getRightButtonClickListener()
        if (rightClickListener == null) {
            headerComponent?.setRightButtonClickListener { nextPressed() }
        }
    }

    private fun initMessagesComponentListeners() {
        setImageOutgoingListener()
        setImageIncomingListener()
        setVideoOutgoingListener()
        setVideoIncomingListener()
        setFileOutgoingListener()
        setFileIncomingListener()
        setAudioOutgoingListener()
        setAudioIncomingListener()
        setTextOutgoingListener()
        setTextIncomingListener()
        setReadMessageListener()
        setAIListener()
    }

    private fun setTextIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val textIncomingListener = messageComponent?.getTextIncomingListener()
        if (textIncomingListener == null) {
            messageComponent?.setTextIncomingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    if (message?.getContentType() == ContentTypes.TEXT) {
                        return
                    }
                    if (message?.getMediaContent()?.getType() == Types.IMAGE) {
                        FullImageScreenActivity.show(requireContext(), message.getMediaContent()?.getUrl())
                    } else {
                        openChooserToShowFileFrom(message)
                    }
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun createAndShowChatPopUp(
        message: ForwardedRepliedMessageEntity?,
        position: Int?,
        view: View,
        xRawTouch: Int,
        yRawTouch: Int,
    ) {
        if (!QuickBloxUiKit.isEnabledForward() && !QuickBloxUiKit.isEnabledReply()) {
            return
        }

        val layoutInflater = requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bindingPopUp = PopupChatLayoutBinding.inflate(layoutInflater)

        val scale = Resources.getSystem().displayMetrics.density
        val width150px = (150 * scale + 0.5f).toInt()
        val popupWindow = PopupWindow(bindingPopUp.root, width150px, ViewGroup.LayoutParams.WRAP_CONTENT)

        popupWindow.isOutsideTouchable = true

        screenSettings?.getTheme()?.getMainTextColor()?.let {
            bindingPopUp.tvForward.setTextColor(it)
            bindingPopUp.tvReply.setTextColor(it)
            bindingPopUp.root.setBackgroundTintList(ColorStateList.valueOf(it))
        }

        screenSettings?.getTheme()?.getMainBackgroundColor()?.let {
            bindingPopUp.clPopUp.setBackgroundTintList(ColorStateList.valueOf(it))
        }

        screenSettings?.getTheme()?.getMainElementsColor()?.let {
            bindingPopUp.tvForward.makeClickableBackground(it)
            bindingPopUp.tvReply.makeClickableBackground(it)
        }

        if (QuickBloxUiKit.isEnabledForward()) {
            bindingPopUp.tvForward.visibility = View.VISIBLE
            bindingPopUp.tvForward.setOnClickListener {
                MessagesSelectionActivity.show(
                    requireContext(),
                    dialogId,
                    viewModel.messages,
                    message,
                    viewModel.pagination,
                    position,
                    screenSettings?.getTheme()
                )
                popupWindow.dismiss()
            }
        } else {
            bindingPopUp.tvForward.visibility = View.GONE
        }

        if (QuickBloxUiKit.isEnabledReply()) {
            bindingPopUp.tvReply.visibility = View.VISIBLE
            bindingPopUp.tvReply.setOnClickListener {
                message?.let { it1 -> showReplyMessage(it1) }
                popupWindow.dismiss()
            }
        } else {
            bindingPopUp.tvReply.visibility = View.GONE
        }

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, xRawTouch, yRawTouch)
    }

    private fun showReplyMessage(message: ForwardedRepliedMessageEntity) {
        repliedMessage = message
        val replyPreviewBinding = buildReplyPreviewBinding()
        val themeUiKit = screenSettings?.getTheme()
        val container = screenSettings?.getMessagesComponent()?.getSendMessageComponent()?.getTopContainer()
        container?.visibility = View.VISIBLE
        container?.removeAllViews()

        replyPreviewBinding.ivIcon.setImageResource(R.drawable.ic_reply)
        themeUiKit?.getSecondaryTextColor()?.let {
            replyPreviewBinding.ivIcon.setColorFilter(it)
            replyPreviewBinding.ivCross.setColorFilter(it)
            replyPreviewBinding.tvActionText.setTextColor(it)
        }

        replyPreviewBinding.tvActionText.text =
            getString(R.string.replied_to_with_name, message.getSender()?.getName())

        val mediaContent = message.getMediaContent()
        if (mediaContent == null) {
            replyPreviewBinding.ivMediaIcon.visibility = View.GONE
            showTextMessage(message, replyPreviewBinding)
        } else {
            showMediaMessage(mediaContent, replyPreviewBinding)
        }

        replyPreviewBinding.ivCross.visibility = View.VISIBLE
        replyPreviewBinding.ivCross.makeClickableBackground(themeUiKit?.getMainElementsColor())
        replyPreviewBinding.ivCross.setOnClickListener {
            hideReplyMessage()
            container?.visibility = View.GONE
        }

        container?.addView(replyPreviewBinding.root)
    }

    private fun hideReplyMessage() {
        repliedMessage = null
        val container = screenSettings?.getMessagesComponent()?.getSendMessageComponent()?.getTopContainer()
        container?.removeAllViews()
        container?.visibility = View.GONE
    }

    private fun buildReplyPreviewBinding(): SendMessagePreviewBinding {
        val inflater = LayoutInflater.from(requireContext())

        return SendMessagePreviewBinding.inflate(inflater)
    }

    private fun showTextMessage(
        message: ChatMessageEntity,
        previewBinding: SendMessagePreviewBinding,
    ) {
        val themeUiKit = screenSettings?.getTheme()

        previewBinding.tvText.text = message.getContent()
        previewBinding.tvText.textSize = 16F

        themeUiKit?.getMainTextColor()?.let {
            previewBinding.tvText.setTextColor(it)
        }
    }

    private fun showMediaMessage(
        mediaContent: MediaContentEntity,
        attachmentPreviewBinding: SendMessagePreviewBinding,
    ) {
        val themeUiKit = screenSettings?.getTheme()

        attachmentPreviewBinding.tvText.text = mediaContent.getName()

        val resourceId = getResourceIdByMediaContent(mediaContent.getType())

        attachmentPreviewBinding.ivMediaIcon.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_media_message)
        attachmentPreviewBinding.ivMediaIcon.setImageResource(resourceId)

        if (mediaContent.getType() == Types.IMAGE || mediaContent.getType() == Types.VIDEO) {
            val progressBar = attachmentPreviewBinding.progressBar

            val backgroundImageView = attachmentPreviewBinding.ivMediaIcon
            backgroundImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            loadImageByUrl(mediaContent.getUrl(), backgroundImageView, progressBar)
        }
        themeUiKit?.getCaptionColor()?.let {
            attachmentPreviewBinding.tvText.setTextColor(it)
        }
    }

    private fun getResourceIdByMediaContent(contentType: Types?): Int {
        when (contentType) {
            Types.IMAGE -> {
                return R.drawable.ic_image_placeholder
            }
            Types.VIDEO -> {
                return R.drawable.ic_video_file
            }
            Types.AUDIO -> {
                return R.drawable.ic_audio_file
            }
            Types.FILE -> {
                return R.drawable.ic_application_file
            }
            else -> {
                throw IllegalArgumentException("$contentType - type does not exist for media content")
            }
        }
    }

    private fun loadImageByUrl(url: String?, imageView: AppCompatImageView, progressBar: ProgressBar) {
        Glide.with(requireContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.ic_image_placeholder))
            .listener(ImageLoadListenerWithProgress(imageView, requireContext(), progressBar)).into(imageView)
    }

    private fun setTextOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val textOutgoingListener = messageComponent?.getTextOutgoingListener()
        if (textOutgoingListener == null) {
            messageComponent?.setTextOutgoingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setImageOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val imageOutgoingListener = messageComponent?.getImageOutgoingListener()
        if (imageOutgoingListener == null) {
            messageComponent?.setImageOutgoingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setImageIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val imageIncomingListener = messageComponent?.getImageIncomingListener()
        if (imageIncomingListener == null) {
            messageComponent?.setImageIncomingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setVideoOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val videoOutgoingListener = messageComponent?.getVideoOutgoingListener()
        if (videoOutgoingListener == null) {
            messageComponent?.setVideoOutgoingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setVideoIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val videoIncomingListener = messageComponent?.getVideoIncomingListener()
        if (videoIncomingListener == null) {
            messageComponent?.setVideoIncomingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setFileOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val fileOutgoingListener = messageComponent?.getFileOutgoingListener()
        if (fileOutgoingListener == null) {
            messageComponent?.setFileOutgoingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setFileIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val fileIngoingListener = messageComponent?.getFileIncomingListener()
        if (fileIngoingListener == null) {
            messageComponent?.setFileIncomingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setAudioOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val audioOutgoingListener = messageComponent?.getAudioOutgoingListener()
        if (audioOutgoingListener == null) {
            messageComponent?.setAudioOutgoingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun setAudioIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val audioIncomingListener = messageComponent?.getAudioIncomingListener()
        if (audioIncomingListener == null) {
            messageComponent?.setAudioIncomingListener(object : BaseMessageViewHolder.MessageListener {
                override fun onClick(message: ChatMessageEntity?) {
                    handlingClickAttachment(message)
                }

                override fun onLongClick(
                    message: ForwardedRepliedMessageEntity?,
                    position: Int?,
                    view: View,
                    xRawTouch: Int,
                    yRawTouch: Int,
                ) {
                    createAndShowChatPopUp(message, position, view, xRawTouch, yRawTouch)
                }
            })
        }
    }

    private fun handlingClickAttachment(message: ChatMessageEntity?) {
        if (message?.getContentType() == ContentTypes.TEXT) {
            return
        }
        if (message?.getMediaContent()?.getType() == Types.IMAGE) {
            FullImageScreenActivity.show(requireContext(), message.getMediaContent()?.getUrl())
        } else {
            openChooserToShowFileFrom(message)
        }
    }

    private fun setReadMessageListener() {
        val messageAdapter = screenSettings?.getMessagesComponent()?.getAdapter()

        val readMessageListener = messageAdapter?.getReadMessageListener()
        if (readMessageListener == null) {
            messageAdapter?.setReadMessageListener(object : MessageAdapter.ReadMessageListener {
                override fun read(message: MessageEntity) {
                    viewModel.sendRead(message)
                }
            })
        }
    }

    private fun setAIListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val aiListener = messageComponent?.getAIListener()
        if (aiListener == null) {
            messageComponent?.setAIListener(object : BaseMessageViewHolder.AIListener {
                override fun onIconClick(message: ForwardedRepliedMessageEntity?) {
                    if (message == null) {
                        return
                    }

                    if (isConfiguredAIAnswerAssistant()) {
                        AIMenuDialog.show(requireContext(),
                            message,
                            screenSettings?.getTheme(),
                            object : AIMenuDialog.IncomingMessageMenuListener {
                                override fun onAiAnswerAssistantClicked(message: ForwardedRepliedMessageEntity?) {
                                    if (dialogId != null && message != null) {
                                        viewModel.executeAIAnswerAssistant(dialogId!!, message)
                                    }
                                }
                            })
                    } else {
                        val errorText = getString(R.string.error_init_ai_answer_assistant)
                        OkDialog.show(requireContext(), errorText, screenSettings?.getTheme())
                    }
                }

                override fun onTranslateClick(message: ForwardedRepliedMessageEntity?) {
                    if (message == null) {
                        return
                    }

                    if (isConfiguredAITranslate()) {
                        viewModel.executeAITranslation(message)
                    } else {
                        OkDialog.show(
                            requireContext(), getString(R.string.error_init_ai_translate), screenSettings?.getTheme()
                        )
                    }
                }
            })
        }
    }

    private fun isConfiguredAIAnswerAssistant(): Boolean {
        val enabledByOpenAIToken = QuickBloxUiKit.isAIAnswerAssistantEnabledWithOpenAIToken()
        val enabledByQuickBloxToken = QuickBloxUiKit.isAIAnswerAssistantEnabledWithProxyServer()

        val enabledByOpenAITokenOrQuickBloxToken = enabledByOpenAIToken || enabledByQuickBloxToken

        return QuickBloxUiKit.isEnabledAIAnswerAssistant() && enabledByOpenAITokenOrQuickBloxToken
    }

    private fun isConfiguredAIRephrase(): Boolean {
        val enabledByOpenAIToken = QuickBloxUiKit.isAIRephraseEnabledWithOpenAIToken()
        val enabledByQuickBloxToken = QuickBloxUiKit.isAIRephraseEnabledWithProxyServer()

        val enabledByOpenAITokenOrQuickBloxToken = enabledByOpenAIToken || enabledByQuickBloxToken

        return QuickBloxUiKit.isEnabledAIRephrase() && enabledByOpenAITokenOrQuickBloxToken
    }

    private fun isConfiguredAITranslate(): Boolean {
        val enabledByOpenAIToken = QuickBloxUiKit.isAITranslateEnabledWithOpenAIToken()
        val enabledByQuickBloxToken = QuickBloxUiKit.isAITranslateEnabledWithProxyServer()

        val enabledByOpenAITokenOrQuickBloxToken = enabledByOpenAIToken || enabledByQuickBloxToken

        return QuickBloxUiKit.isEnabledAITranslate() && enabledByOpenAITokenOrQuickBloxToken
    }

    private fun openChooserToShowFileFrom(message: ChatMessageEntity?) {
        val uri = Uri.parse(message?.getMediaContent()?.getUrl())
        val mimeType = message?.getMediaContent()?.getMimeType()

        try {
            openChooser(uri, mimeType)
        } catch (exception: Exception) {
            openChooserWithCommonMimeTypeAndHandleException(uri)
        }
    }

    private fun openChooserWithCommonMimeTypeAndHandleException(uri: Uri) {
        try {
            openChooser(uri, "*/*")
        } catch (exception: Exception) {
            showToast("There is a problem to open file")
        }
    }

    private fun openChooser(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        startActivity(intent)
    }

    private fun initSendMessagesComponentListeners() {
        val sendMessageComponent = screenSettings?.getMessagesComponent()?.getSendMessageComponent()

        val listener = sendMessageComponent?.getSendMessageComponentListener()
        if (listener == null) {
            sendMessageComponent?.setSendMessageComponentListener(object : SendMessageComponentListenerImpl() {
                override fun onSendTextMessageClickListener(textMessage: String) {
                    if (repliedMessage == null) {
                        viewModel.createAndSendMessage(ContentTypes.TEXT, textMessage)
                    } else {
                        viewModel.createAndSendReplyMessage(repliedMessage, ContentTypes.TEXT, textMessage)
                        hideReplyMessage()
                    }
                    originalText = ""
                }

                override fun onStartRecordVoiceClickListener() {
                    checkAudioRecordPermissionAndStartRecord()
                }

                override fun onStopRecordVoiceClickListener() {
                    stopAudiRecording()
                }

                override fun onClickPhotoCamera() {
                    checkCameraPermissionAndLaunchCamera(false)
                }

                override fun onClickVideoCamera() {
                    checkCameraPermissionAndLaunchCamera(true)
                }

                override fun onClickPhotoAndVideoGallery() {
                    launchPhotoAndVideoGallery()
                }

                override fun onClickFile() {
                    launchFileSelection()
                }

                override fun onStartedTyping() {
                    viewModel.sendStartedTyping()
                }

                override fun onStoppedTyping() {
                    viewModel.sendStoppedTyping()
                }

                override fun onClickedTone(rephraseEntity: AIRephraseEntity) {
                    val notConfiguredAIRephrase = !isConfiguredAIRephrase()
                    if (notConfiguredAIRephrase) {
                        val errorText = getString(R.string.error_init_ai_rephrase)
                        OkDialog.show(requireContext(), errorText, screenSettings?.getTheme())
                        return
                    }

                    val messageEditText =
                        screenSettings?.getMessagesComponent()?.getSendMessageComponent()?.getMessageEditText()

                    val needToSetOriginalText = originalText.isBlank()
                    if (needToSetOriginalText) {
                        originalText = messageEditText?.text.toString()
                    }

                    if (rephraseEntity.isOriginal()) {
                        messageEditText?.setText(originalText)
                        return
                    }

                    val text = messageEditText?.text.toString()
                    if (text.isNotBlank()) {
                        rephraseEntity.setOriginalText(text)
                        viewModel.executeAIRephrase(rephraseEntity)
                    }
                }

                override fun onClearRephraseOriginalText() {
                    originalText = ""
                }

                override fun onChangedRephraseText() {
                    if (needToSetText && originalText.isNotBlank()) {
                        val messageEditText =
                            screenSettings?.getMessagesComponent()?.getSendMessageComponent()?.getMessageEditText()
                        originalText = messageEditText?.text.toString()
                    }
                }
            })
        }
    }

    private fun stopAudiRecording() {
        lifecycleScope.launch(Dispatchers.Main) {
            Recorder.stopRecording()
            val uri = Recorder.uri
            uri?.let {
                createAndSendMessage(it)
            }
        }
    }

    private fun startAudiRecording() {
        lifecycleScope.launch(Dispatchers.Main) {
            val fileEntity = viewModel.createFileWith("aac")
            val file = fileEntity?.getFile()

            Recorder.startRecording(requireContext(), file)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)

        val views = collectViewsTemplateMethod(requireContext())
        for (view in views) {
            view?.let {
                binding?.llParent?.addView(view)
            }
        }

        val adapter = screenSettings?.getMessagesComponent()?.getAdapter()
        adapter?.setItems(viewModel.messages)
        screenSettings?.getHeaderComponent()?.setImageRightButton(R.drawable.info)
        screenSettings?.getMessagesComponent()?.setOnScrollListener(scrollListenerImpl)

        subscribeToLoadedDialog()
        subscribeToError()
        subscribeToLoadedMessages()
        subscribeToUpdatedMessage()
        subscribeToReceivedMessage()
        subscribeToMessagesLoading()
        subscribeToUpdateDialog()
        subscribeToAIAnswer()
        subscribeToAIRephrase()

        enableAIMenu()
        enableAITranslate()
        enableAIRephrase()

        return binding?.root
    }

    private fun subscribeToLoadedDialog() {
        viewModel.loadedDialogEntity.observe(viewLifecycleOwner) { dialogEntity ->
            val avatarUrl = dialogEntity?.getPhoto()
            screenSettings?.getHeaderComponent()?.loadAvatar(avatarUrl.toString(), R.drawable.group_holder)

            val dialogName = dialogEntity?.getName()
            screenSettings?.getHeaderComponent()?.setTitle(dialogName)
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    private fun subscribeToLoadedMessages() {
        viewModel.loadedMessage.observe(viewLifecycleOwner) { index ->
            if (viewModel.messages.isNotEmpty()) {
                screenSettings?.getMessagesComponent()?.getAdapter()?.notifyItemChanged(index)
            }
        }
    }

    private fun subscribeToUpdatedMessage() {
        viewModel.updatedMessage.observe(viewLifecycleOwner) { index ->
            screenSettings?.getMessagesComponent()?.getAdapter()?.notifyItemChanged(index)
        }
    }

    private fun subscribeToReceivedMessage() {
        viewModel.receivedMessage.observe(viewLifecycleOwner) {
            if (viewModel.messages.isNotEmpty()) {
                screenSettings?.getMessagesComponent()?.getAdapter()?.notifyItemInserted(0)
                screenSettings?.getMessagesComponent()?.scrollToDown()
            }
        }
    }

    private fun subscribeToMessagesLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                screenSettings?.getMessagesComponent()?.showProgress()
                scrollListenerImpl.isLoading = false
            } else {
                screenSettings?.getMessagesComponent()?.hideProgress()
            }
        }
    }

    private fun subscribeToUpdateDialog() {
        viewModel.typingEvents.observe(viewLifecycleOwner) { result ->
            val sendMessageComponent = screenSettings?.getMessagesComponent()?.getSendMessageComponent()

            val typingEvent = result.first
            val text = result.second

            when (typingEvent) {
                TypingEvents.STARTED -> {
                    sendMessageComponent?.showStartedTyping(text)
                }
                TypingEvents.STOPPED -> {
                    sendMessageComponent?.showStoppedTyping()
                }
            }
        }
    }

    private fun subscribeToAIAnswer() {
        viewModel.aiAnswer.observe(viewLifecycleOwner) { result ->
            val editText = screenSettings?.getMessagesComponent()?.getSendMessageComponent()?.getMessageEditText()
            editText?.setText(result)
        }
    }

    private fun subscribeToAIRephrase() {
        val sendMessageComponent = screenSettings?.getMessagesComponent()?.getSendMessageComponent()

        viewModel.rephrasedText.observe(viewLifecycleOwner) { entity ->
            val editText = sendMessageComponent?.getMessageEditText()
            needToSetText = false
            editText?.setText(entity.getRephrasedText())
            needToSetText = true
        }

        viewModel.allTones.observe(viewLifecycleOwner) { tones ->
            sendMessageComponent?.setRephraseTones(tones)
        }
    }

    private fun enableAIMenu() {
        val enabledAI = QuickBloxUiKit.isEnabledAIAnswerAssistant()
        screenSettings?.getMessagesComponent()?.getAdapter()?.enabledAI(enabledAI)
    }

    private fun enableAITranslate() {
        val enabled = QuickBloxUiKit.isEnabledAITranslate()
        screenSettings?.getMessagesComponent()?.getAdapter()?.enabledAITranslate(enabled)
    }

    private fun enableAIRephrase() {
        val enabled = QuickBloxUiKit.isEnabledAIRephrase()
        screenSettings?.getMessagesComponent()?.getSendMessageComponent()?.enableRephrase(enabled)
    }

    override fun collectViewsTemplateMethod(context: Context): List<View?> {
        val views = mutableListOf<View?>()
        views.add(screenSettings?.getHeaderComponent()?.getView())

        val messagesComponentView = screenSettings?.getMessagesComponent()?.getView()
        messagesComponentView?.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        views.add(messagesComponentView)

        return views
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    protected open fun backPressed() {
        // TODO: Need to add logic for update dialog,
        //  because when we send read message the unread message counter not updating in DialogsScreen
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    protected open fun nextPressed() {
        startGroupChatInfoActivity()
    }

    private fun startGroupChatInfoActivity() {
        GroupChatInfoActivity.show(requireContext(), dialogId)
    }

    private fun requestPhotoCameraPermissions() {
        val intent = Intent(ACTION_REQUEST_PERMISSIONS)
        intent.putExtra(EXTRA_PERMISSIONS, arrayOf(CAMERA_PERMISSION))
        intent.putExtra(EXTRA_DATA, "photo")
        requestCameraPermissionLauncher.launch(intent)
    }

    private fun requestVideoCameraPermissions() {
        val intent = Intent(ACTION_REQUEST_PERMISSIONS)
        intent.putExtra(EXTRA_PERMISSIONS, arrayOf(CAMERA_PERMISSION))
        intent.putExtra(EXTRA_DATA, "video")
        requestCameraPermissionLauncher.launch(intent)
    }

    private fun requestAudiRecordPermissions() {
        requestAudioRecordPermissionLauncher.launch(RECORD_AUDIO_PERMISSION)
    }

    private fun registerCameraPermissionLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(PermissionsContract()) {
            val isNotGranted = !it.first
            val extraData = it.second

            if (isNotGranted) {
                showToast(getString(R.string.permission_denied))
                return@registerForActivityResult
            }

            when (extraData) {
                "photo" -> {
                    checkCameraPermissionAndLaunchCamera(false)
                }

                "video" -> {
                    checkCameraPermissionAndLaunchCamera(true)
                }
            }
        }
    }

    private fun registerAudioRecordPermissionLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startAudiRecording()
            } else {
                showToast(getString(R.string.permission_denied))
            }
        }
    }

    private fun registerPhotoCameraLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(CameraResultContract()) {
            val isSuccess = it.first
            val uri = it.second

            if (isSuccess && uri != null) {
                createAndSendMessage(uri)
            }
        }
    }

    private fun registerVideoCameraLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(CameraResultContract()) {
            val isSuccess = it.first
            val uri = it.second

            if (isSuccess && uri != null) {
                createAndSendMessage(uri)
            }
        }
    }

    private fun registerPhotoAndVideoGalleryLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                createAndSendMessage(it)
            }
        }
    }

    private fun registerFileLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                createAndSendMessage(it)
            }
        }
    }

    private fun createAndSendMessage(uri: Uri) {
        lifecycleScope.launch {
            val file = viewModel.getFileBy(uri)
            if (repliedMessage == null) {
                viewModel.createAndSendMessage(ContentTypes.MEDIA, null, file)
            } else {
                viewModel.createAndSendReplyMessage(repliedMessage, ContentTypes.MEDIA, null, file)
                hideReplyMessage()
            }
        }
    }

    private fun checkCameraPermissionAndLaunchCamera(isVideo: Boolean) {
        val isHasPermission = checkCameraPermissionRequest()
        if (isHasPermission) {
            launchCamera(isVideo)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showPositiveNegativeDialogForCameraPermission(isVideo)
        } else {
            requestCameraPermissions(isVideo)
        }
    }

    private fun checkCameraPermissionRequest(): Boolean {
        val checkedCameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return checkedCameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAudioRecordPermissionAndStartRecord() {
        val isHasPermission = checkAudioRecordPermissionRequest()
        if (isHasPermission) {
            startAudiRecording()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showPositiveNegativeDialogForAudioRecordPermission()
        } else {
            requestAudiRecordPermissions()
        }
    }

    private fun checkAudioRecordPermissionRequest(): Boolean {
        val checkedCameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
        return checkedCameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun launchCamera(isVideo: Boolean) {
        if (isVideo) {
            launchVideoCamera()
        } else {
            launchPhotoCamera()
        }
    }

    private fun showPositiveNegativeDialogForCameraPermission(isVideo: Boolean) {
        val contentText = getString(R.string.permission_alert_text)
        val positiveText = getString(R.string.yes)
        val negativeText = getString(R.string.no)
        PositiveNegativeDialog.show(requireContext(),
            contentText,
            positiveText,
            negativeText,
            screenSettings?.getTheme(),
            positiveListener = {
                requestCameraPermissions(isVideo)
            },
            negativeListener = {
                showToast(getString(R.string.permission_denied))
            })
    }

    private fun showPositiveNegativeDialogForAudioRecordPermission() {
        val contentText = getString(R.string.permission_alert_text)
        val positiveText = getString(R.string.yes)
        val negativeText = getString(R.string.no)
        PositiveNegativeDialog.show(requireContext(),
            contentText,
            positiveText,
            negativeText,
            screenSettings?.getTheme(),
            positiveListener = {
                requestAudiRecordPermissions()
            },
            negativeListener = {
                showToast(getString(R.string.permission_denied))
            })
    }

    private fun requestCameraPermissions(isVideo: Boolean) {
        if (isVideo) {
            requestVideoCameraPermissions()
        } else {
            requestPhotoCameraPermissions()
        }
    }

    private fun launchVideoCamera() {
        lifecycleScope.launch {
            val fileEntity = viewModel.createFileWith("mp4")
            val uri = fileEntity?.getUri()

            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri)
            videoCameraLauncher.launch(intent)
        }
    }

    private fun launchPhotoCamera() {
        lifecycleScope.launch {
            val fileEntity = viewModel.createFileWith("jpg")
            val uri = fileEntity?.getUri()

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri)
            photoCameraLauncher.launch(intent)
        }
    }

    private fun launchPhotoAndVideoGallery() {
        val IMAGE_VIDEO_MIME = "image/*, video/*"
        lifecycleScope.launch(Dispatchers.IO) {
            photoAndVideoGalleryLauncher.launch(IMAGE_VIDEO_MIME)
        }
    }

    private fun launchFileSelection() {
        val APPLICATION_MIME = "application/*"
        fileLauncher.launch(APPLICATION_MIME)
    }

    private inner class ScrollListenerImpl : RecyclerView.OnScrollListener() {
        var isLoading: Boolean = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val isScrolledToTop = !recyclerView.canScrollVertically(-1)
            if (isScrolledToTop || isLoading) {
                isLoading = true
                viewModel.loadMessages()
            }
        }
    }
}