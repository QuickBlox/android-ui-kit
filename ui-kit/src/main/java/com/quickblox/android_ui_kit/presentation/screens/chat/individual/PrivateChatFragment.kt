/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.chat.individual

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*
import com.quickblox.android_ui_kit.presentation.components.send.Recorder
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponentListenerImpl
import com.quickblox.android_ui_kit.presentation.dialogs.AIMenuDialog
import com.quickblox.android_ui_kit.presentation.dialogs.OkDialog
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.screens.chat.CameraResultContract
import com.quickblox.android_ui_kit.presentation.screens.chat.EXTRA_DATA
import com.quickblox.android_ui_kit.presentation.screens.chat.PermissionsContract
import com.quickblox.android_ui_kit.presentation.screens.chat.full_image_screen.FullImageScreenActivity
import com.quickblox.android_ui_kit.presentation.screens.chat.individual.PrivateChatViewModel.TypingEvents
import com.quickblox.android_ui_kit.presentation.screens.info.individual.PrivateChatInfoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val CAMERA_PERMISSION = "android.permission.CAMERA"
private const val RECORD_AUDIO_PERMISSION = "android.permission.RECORD_AUDIO"

open class PrivateChatFragment : BaseFragment() {
    companion object {
        val TAG: String = PrivateChatFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null, screenSettings: PrivateChatScreenSettings? = null,
        ): PrivateChatFragment {
            val privateChatFragment = PrivateChatFragment()
            privateChatFragment.dialogId = dialogId
            privateChatFragment.screenSettings = screenSettings
            return privateChatFragment
        }
    }

    private val viewModel by viewModels<PrivateChatViewModel>()
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: PrivateChatScreenSettings? = null
    private var dialogId: String? = null
    private var scrollListenerImpl = ScrollListenerImpl()

    private val requestCameraPermissionLauncher = registerCameraPermissionLauncher()
    private val requestAudioRecordPermissionLauncher = registerAudioRecordPermissionLauncher()

    private var photoCameraLauncher = registerPhotoCameraLauncher()
    private var videoCameraLauncher = registerVideoCameraLauncher()
    private var photoAndVideoGalleryLauncher = registerPhotoAndVideoGalleryLauncher()
    private var fileLauncher = registerFileLauncher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = PrivateChatScreenSettings.Builder(requireContext()).build()
        }

        initHeaderComponentListeners()
        initMessagesComponentListeners()
        initSendMessagesComponentListeners()
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
        setReadMessageListener()
        setAIListener()
    }

    private fun setImageOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val imageOutgoingListener = messageComponent?.getImageOutgoingListener()
        if (imageOutgoingListener == null) {
            messageComponent?.setImageOutgoingListener(object : ImageOutgoingViewHolder.ImageOutgoingListener {
                override fun onImageClick(message: OutgoingChatMessageEntity?) {
                    FullImageScreenActivity.show(requireContext(), message?.getMediaContent()?.getUrl())
                }

                override fun onImageLongClick(message: OutgoingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setImageIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val imageIncomingListener = messageComponent?.getImageIncomingListener()
        if (imageIncomingListener == null) {
            messageComponent?.setImageIncomingListener(object : ImageIncomingViewHolder.ImageIncomingListener {
                override fun onImageClick(message: IncomingChatMessageEntity?) {
                    FullImageScreenActivity.show(requireContext(), message?.getMediaContent()?.getUrl())
                }

                override fun onImageLongClick(message: IncomingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setVideoOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val videoOutgoingListener = messageComponent?.getVideoOutgoingListener()
        if (videoOutgoingListener == null) {
            messageComponent?.setVideoOutgoingListener(object : VideoOutgoingViewHolder.VideoOutgoingListener {
                override fun onVideoClick(message: OutgoingChatMessageEntity?) {
                    openChooserToShowFileFrom(message)
                }

                override fun onVideoLongClick(message: OutgoingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setVideoIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val videoIncomingListener = messageComponent?.getVideoIncomingListener()
        if (videoIncomingListener == null) {
            messageComponent?.setVideoIncomingListener(object : VideoIncomingViewHolder.VideoIncomingListener {
                override fun onVideoClick(message: IncomingChatMessageEntity?) {
                    openChooserToShowFileFrom(message)
                }

                override fun onVideoLongClick(message: IncomingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setFileOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val fileOutgoingListener = messageComponent?.getFileOutgoingListener()
        if (fileOutgoingListener == null) {
            messageComponent?.setFileOutgoingListener(object : FileOutgoingViewHolder.FileOutgoingListener {
                override fun onFileClick(message: OutgoingChatMessageEntity?) {
                    openChooserToShowFileFrom(message)
                }

                override fun onFileLongClick(message: OutgoingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setFileIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val fileIngoingListener = messageComponent?.getFileIncomingListener()
        if (fileIngoingListener == null) {
            messageComponent?.setFileIncomingListener(object : FileIncomingViewHolder.FileIncomingListener {
                override fun onFileClick(message: IncomingChatMessageEntity?) {
                    openChooserToShowFileFrom(message)
                }

                override fun onFileLongClick(message: IncomingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setAudioOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val audioOutgoingListener = messageComponent?.getAudioOutgoingListener()
        if (audioOutgoingListener == null) {
            messageComponent?.setAudioOutgoingListener(object : AudioOutgoingViewHolder.AudioOutgoingListener {
                override fun onAudioClick(message: OutgoingChatMessageEntity?) {
                    openChooserToShowFileFrom(message)
                }

                override fun onAudioLongClick(message: OutgoingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun setAudioIncomingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val audioIncomingListener = messageComponent?.getAudioIncomingListener()
        if (audioIncomingListener == null) {
            messageComponent?.setAudioIncomingListener(object : AudioIncomingViewHolder.AudioIncomingListener {
                override fun onAudioClick(message: IncomingChatMessageEntity?) {
                    openChooserToShowFileFrom(message)
                }

                override fun onAudioLongClick(message: IncomingChatMessageEntity?) {
                    // empty
                }
            })
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
            messageComponent?.setAIListener(object : TextIncomingViewHolder.AIListener {
                override fun onIconClick(message: IncomingChatMessageEntity?) {
                    if (message == null) {
                        return
                    }

                    if (isConfiguredAIAnswerAssistant()) {
                        AIMenuDialog.show(
                            requireContext(),
                            message,
                            screenSettings?.getTheme(),
                            object : AIMenuDialog.IncomingMessageMenuListener {
                                override fun onAiAnswerAssistantClicked(message: IncomingChatMessageEntity?) {
                                    if (dialogId != null && message != null) {
                                        viewModel.executeAIAnswerAssistant(dialogId!!, message)
                                    }
                                }
                            })
                    } else {
                        OkDialog.show(requireContext(), getString(R.string.error_init_ai), screenSettings?.getTheme())
                    }
                }
            })
        }
    }

    private fun isConfiguredAIAnswerAssistant(): Boolean {
        val enabledByOpenAIToken = QuickBloxUiKit.isAIAnswerAssistantEnabledByOpenAIToken()
        val enabledByQuickBloxToken = QuickBloxUiKit.isAIAnswerAssistantEnabledByQuickBloxToken()

        val enabledByOpenAITokenOrQuickBloxToken = enabledByOpenAIToken || enabledByQuickBloxToken

        return QuickBloxUiKit.isEnabledAIAnswerAssistant() && enabledByOpenAITokenOrQuickBloxToken
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
                    viewModel.createAndSendMessage(ChatMessageEntity.ContentTypes.TEXT, textMessage)
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

        enableAI()

        return binding?.root
    }

    private fun subscribeToLoadedDialog() {
        viewModel.loadedDialogEntity.observe(viewLifecycleOwner) { dialogEntity ->
            val avatarUrl = dialogEntity?.getPhoto()
            screenSettings?.getHeaderComponent()?.loadAvatar(avatarUrl.toString(), R.drawable.private_holder)

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
        viewModel.loadedMessage.observe(viewLifecycleOwner) {
            if (viewModel.messages.isNotEmpty()) {
                screenSettings?.getMessagesComponent()?.getAdapter()?.notifyItemInserted(0)
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

    private fun enableAI() {
        val enabledAI = QuickBloxUiKit.isEnabledAIAnswerAssistant()
        screenSettings?.getMessagesComponent()?.getAdapter()?.enabledAI(enabledAI)
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
        startPrivateChatInfoActivity()
    }

    private fun startPrivateChatInfoActivity() {
        PrivateChatInfoActivity.show(requireContext(), dialogId)
    }

    private fun requestPhotoCameraPermissions() {
        val intent = Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS)
        intent.putExtra(
            ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS, arrayOf(CAMERA_PERMISSION)
        )
        intent.putExtra(EXTRA_DATA, "photo")
        requestCameraPermissionLauncher.launch(intent)
    }

    private fun requestVideoCameraPermissions() {
        val intent = Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS)
        intent.putExtra(
            ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS, arrayOf(CAMERA_PERMISSION)
        )
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
            viewModel.createAndSendMessage(ChatMessageEntity.ContentTypes.MEDIA, null, file)
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
        val APPLICATION_MIME = "*/*"
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