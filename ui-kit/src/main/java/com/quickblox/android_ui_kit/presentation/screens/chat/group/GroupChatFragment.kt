/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.chat.group

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
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.ACTION_REQUEST_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ContentTypes
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageIncomingViewHolder.ImageIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageOutgoingViewHolder.ImageOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.VideoOutgoingViewHolder.VideoOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.send.Recorder
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponentListenerImpl
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.screens.chat.CameraResultContract
import com.quickblox.android_ui_kit.presentation.screens.chat.EXTRA_DATA
import com.quickblox.android_ui_kit.presentation.screens.chat.PermissionsContract
import com.quickblox.android_ui_kit.presentation.screens.chat.full_image_screen.FullImageScreenActivity
import com.quickblox.android_ui_kit.presentation.screens.info.group.GroupChatInfoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val CAMERA_PERMISSION = "android.permission.CAMERA"
private const val RECORD_AUDIO_PERMISSION = "android.permission.RECORD_AUDIO"

open class GroupChatFragment : BaseFragment() {
    companion object {
        val TAG: String = GroupChatFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null, screenSettings: GroupChatScreenSettings? = null
        ): GroupChatFragment {
            val privateChatFragment = GroupChatFragment()
            privateChatFragment.dialogId = dialogId
            privateChatFragment.screenSettings = screenSettings
            return privateChatFragment
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = GroupChatScreenSettings.Builder(requireContext()).build()
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
    }

    private fun setImageOutgoingListener() {
        val messageComponent = screenSettings?.getMessagesComponent()

        val imageOutgoingListener = messageComponent?.getImageOutgoingListener()
        if (imageOutgoingListener == null) {
            messageComponent?.setImageOutgoingListener(object : ImageOutgoingListener {
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
            messageComponent?.setImageIncomingListener(object : ImageIncomingListener {
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
            messageComponent?.setVideoOutgoingListener(object : VideoOutgoingListener {
                override fun onVideoClick(message: OutgoingChatMessageEntity?) {
                    startActionView(message)
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
                    startActionView(message)
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
                    startActionView(message)
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
                    startActionView(message)
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
                    startActionView(message)
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
                    startActionView(message)
                }

                override fun onAudioLongClick(message: IncomingChatMessageEntity?) {
                    // empty
                }
            })
        }
    }

    private fun startActionView(message: ChatMessageEntity?) {
        val url = message?.getMediaContent()?.getUrl()
        val mimeType = message?.getMediaContent()?.getMimeType()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), mimeType)
        try {
            startActivity(intent)
            // TODO: Need to refactor catch block
        } catch (exception: Exception) {
            showToast(exception.message.toString())
        }
    }

    private fun initSendMessagesComponentListeners() {
        val sendMessageComponent = screenSettings?.getMessagesComponent()?.getSendMessageComponent()

        val listener = sendMessageComponent?.getSendMessageComponentListener()
        if (listener == null) {
            sendMessageComponent?.setSendMessageComponentListener(object : SendMessageComponentListenerImpl() {
                override fun onSendTextMessageClickListener(textMessage: String) {
                    viewModel.createAndSendMessage(ContentTypes.TEXT, textMessage)
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
        for (view in views){
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
            viewModel.createAndSendMessage(ContentTypes.MEDIA, null, file)
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