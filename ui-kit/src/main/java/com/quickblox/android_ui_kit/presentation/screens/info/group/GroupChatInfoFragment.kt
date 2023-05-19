/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.group

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.dialogs.AvatarDialog
import com.quickblox.android_ui_kit.presentation.dialogs.ChangeNameDialog
import com.quickblox.android_ui_kit.presentation.dialogs.EditDialog
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.screens.info.members.MembersActivity
import kotlinx.coroutines.launch

private const val CAMERA_PERMISSION = "android.permission.CAMERA"

open class GroupChatInfoFragment : BaseFragment() {
    companion object {
        val TAG: String = GroupChatInfoFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null, screenSettings: GroupChatInfoScreenSettings? = null
        ): GroupChatInfoFragment {
            val privateChatFragment = GroupChatInfoFragment()
            privateChatFragment.dialogId = dialogId
            privateChatFragment.screenSettings = screenSettings
            return privateChatFragment
        }
    }

    private val viewModel by viewModels<GroupChatInfoViewModel>()
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: GroupChatInfoScreenSettings? = null
    private var dialogId: String? = null

    private val requestPermissionLauncher = registerPermissionLauncher()
    private var cameraLauncher: ActivityResultLauncher<Uri>? = registerCameraLauncher()
    private var galleryLauncher: ActivityResultLauncher<String>? = registerGalleryLauncher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = GroupChatInfoScreenSettings.Builder(requireContext()).build()
        }

        if (dialogId.isNullOrEmpty()) {
            showToast("The dialogId shouldn't be empty")
        } else {
            viewModel.setDialogId(dialogId!!)
        }

        initHeaderComponentListeners()
        initInfoComponentListeners()
    }

    override fun onResume() {
        viewModel.loadDialogEntity()
        super.onResume()
    }

    private fun initHeaderComponentListeners() {
        val headerComponent = screenSettings?.getHeaderComponent()

        val leftClickListener = headerComponent?.getLeftButtonClickListener()
        if (leftClickListener == null) {
            headerComponent?.setLeftButtonClickListener {
                backPressed()
            }
        }

        val rightClickListener = headerComponent?.getRightButtonClickListener()
        if (rightClickListener == null) {
            headerComponent?.setRightButtonClickListener {
                nextPressed()
            }
        }
    }

    private fun initInfoComponentListeners() {
        val infoComponent = screenSettings?.getInfoComponent()

        val membersClickListener = infoComponent?.getMembersItemClickListener()
        if (membersClickListener == null) {
            infoComponent?.setMembersItemClickListener {
                startMembersActivity()
            }
        }

        val leaveClickListener = infoComponent?.getLeaveItemClickListener()
        if (leaveClickListener == null) {
            infoComponent?.setLeaveItemClickListener {
                viewModel.leaveDialog()
            }
        }
    }

    private fun startMembersActivity() {
        MembersActivity.show(requireContext(), dialogId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)

        screenSettings?.getHeaderComponent()?.setTitle(getString(R.string.dialog_info))
        val backGround = screenSettings?.getTheme()?.getMainBackgroundColor()

        backGround?.let {
            binding?.root?.setBackgroundColor(it)
        }

        subscribeToLoadedAndUpdateDialog()
        subscribeToLeaveDialog()
        subscribeToUpdatingProgress()
        subscribeToError()

        val views = collectViewsTemplateMethod(requireContext())
        for (view in views){
            view?.let {
                binding?.llParent?.addView(view)
            }
        }

        return binding?.root
    }

    private fun subscribeToLoadedAndUpdateDialog() {
        viewModel.loadedAndUpdatedDialogEntity.observe(viewLifecycleOwner) { dialogEntity ->
            updateInfoComponent(dialogEntity)
        }
    }

    private fun updateInfoComponent(dialogEntity: DialogEntity?) {
        val avatarUrl = dialogEntity?.getPhoto()

        var placeHolder = screenSettings?.getInfoComponent()?.getAvatarView()?.drawable
        if (placeHolder == null) {
            placeHolder = ContextCompat.getDrawable(requireContext(), R.drawable.group_holder)
        }

        screenSettings?.getInfoComponent()?.loadAvatar(avatarUrl.toString(), placeHolder)

        dialogEntity?.getName()?.let {
            screenSettings?.getInfoComponent()?.setTextName(it)
        }

        val members = dialogEntity?.getParticipantIds()?.size
        members?.let {
            screenSettings?.getInfoComponent()?.setCountMembers(it)
        }

        if (dialogEntity?.isOwner() == true) {
            screenSettings?.getHeaderComponent()?.setTextRightButton(getString(R.string.edit))
            screenSettings?.getHeaderComponent()?.setVisibleRightButton(true)
        } else {
            screenSettings?.getHeaderComponent()?.setVisibleRightButton(false)
        }
    }

    private fun subscribeToLeaveDialog() {
        viewModel.leaveDialog.observe(viewLifecycleOwner) {
            requireActivity().finishAffinity()
        }
    }

    private fun subscribeToUpdatingProgress() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                screenSettings?.getInfoComponent()?.showProgress()
            } else {
                screenSettings?.getInfoComponent()?.hideProgress()
            }
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    override fun collectViewsTemplateMethod(context: Context): List<View?> {
        val views = mutableListOf<View?>()
        views.add(screenSettings?.getHeaderComponent()?.getView())
        views.add(screenSettings?.getInfoComponent()?.getView())
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
        showEditDialog()
    }

    private fun showEditDialog() {
        EditDialog.show(
            requireContext(), getString(R.string.edit), screenSettings?.getTheme(), EditDialogListenerImpl()
        )
    }

    private fun showAvatarDialog() {
        AvatarDialog.show(
            requireContext(), getString(R.string.change_photo), screenSettings?.getTheme(), AvatarDialogListenerImpl()
        )
    }

    private fun showNameDialog() {
        ChangeNameDialog.show(requireContext(), screenSettings?.getTheme()) { newName ->
            viewModel.setDialogName(newName)
            viewModel.updateDialog()
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(CAMERA_PERMISSION)
    }

    private fun registerPermissionLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                checkPermissionAndLaunchCamera()
            } else {
                showToast(getString(R.string.permission_denied))
            }
        }
    }

    private fun registerCameraLauncher(): ActivityResultLauncher<Uri> {
        return registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val uri = viewModel.getDialogAvatarUri()
                uploadFileBy(uri)
            }
        }
    }

    private fun registerGalleryLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uploadFileBy(uri)
        }
    }

    private fun uploadFileBy(uri: Uri?) {
        lifecycleScope.launch {
            val file = uri?.let {
                viewModel.getFileBy(it)
            }
            viewModel.uploadFileAndUpdateDialog(file)
        }
    }

    private fun checkPermissionAndLaunchCamera() {
        val isHasPermission = checkPermissionRequest()
        if (isHasPermission) {
            lifecycleScope.launch {
                val uri = viewModel.createFileAndGetUri()
                cameraLauncher?.launch(uri)
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            val contentText = getString(R.string.permission_alert_text)
            val positiveText = getString(R.string.yes)
            val negativeText = getString(R.string.no)
            PositiveNegativeDialog.show(
                requireContext(), contentText, positiveText, negativeText, screenSettings?.getTheme(),
                positiveListener = {
                    requestCameraPermission()
                }, negativeListener = {
                    showToast(getString(R.string.permission_denied))
                }
            )
        } else {
            requestCameraPermission()
        }
    }

    private fun checkPermissionRequest(): Boolean {
        val checkedCameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return checkedCameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private inner class AvatarDialogListenerImpl : AvatarDialog.AvatarDialogListener {
        override fun onClickCamera() {
            checkPermissionAndLaunchCamera()
        }

        override fun onClickGallery() {
            val IMAGE_MIME = "image/*"
            galleryLauncher?.launch(IMAGE_MIME)
        }

        override fun onClickRemove() {
            viewModel.removeAvatar()
            screenSettings?.getInfoComponent()?.setDefaultPlaceHolderAvatar()
        }
    }

    private inner class EditDialogListenerImpl : EditDialog.EditDialogListener {
        override fun onClickPhoto() {
            showAvatarDialog()
        }

        override fun onClickName() {
            showNameDialog()
        }
    }
}