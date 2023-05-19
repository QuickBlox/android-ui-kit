/*
 * Created by Injoit on 23.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.create.name

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
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.screens.SimpleTextWatcher
import com.quickblox.android_ui_kit.presentation.screens.create.users.UsersActivity
import com.quickblox.android_ui_kit.presentation.screens.isValidDialogName
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUri
import kotlinx.coroutines.launch


private const val CAMERA_PERMISSION = "android.permission.CAMERA"

open class DialogNameFragment : BaseFragment() {
    companion object {
        val TAG: String = DialogNameFragment::class.java.simpleName

        fun newInstance(
            dialogEntity: DialogEntity? = null, screenSettings: DialogNameScreenSettings? = null,
        ): DialogNameFragment {
            val dialogNameFragment = DialogNameFragment()
            dialogNameFragment.screenSettings = screenSettings
            dialogNameFragment.dialogEntity = dialogEntity
            return dialogNameFragment
        }
    }

    private val viewModel by viewModels<DialogNameViewModel>()
    private var dialogEntity: DialogEntity? = null
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: DialogNameScreenSettings? = null

    private val requestPermissionLauncher = registerPermissionLauncher()
    private var cameraLauncher: ActivityResultLauncher<Uri>? = registerCameraLauncher()
    private var galleryLauncher: ActivityResultLauncher<String>? = registerGalleryLauncher()

    override fun collectViewsTemplateMethod(context: Context): List<View?> {
        val views = mutableListOf<View?>()
        views.add(screenSettings?.getHeaderComponent()?.getView())
        views.add(screenSettings?.getNameComponent()?.getView())
        return views
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = DialogNameScreenSettings.Builder(requireContext()).build()
        }

        viewModel.setDialogEntity(dialogEntity)
        initHeaderComponentListeners()
        initNameComponentListeners()
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

    private fun initNameComponentListeners() {
        val nameComponent = screenSettings?.getNameComponent()

        val avatarClickListener = nameComponent?.getAvatarClickListener()
        if (avatarClickListener == null) {
            nameComponent?.setAvatarClickListener { avatarPressed() }
        }

        nameComponent?.setTextWatcherToEditText(object : SimpleTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val dialogName = charSequence.toString()

                val isValidName = dialogName.isValidDialogName()

                if (isValidName) {
                    screenSettings?.getHeaderComponent()?.setRightButtonClickableState()
                } else {
                    screenSettings?.getHeaderComponent()?.setRightButtonNotClickableState()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)
        screenSettings?.getTheme()?.getMainBackgroundColor()?.let {
            binding?.root?.setBackgroundColor(it)
        }

        screenSettings?.getHeaderComponent()?.setTextRightButton(getString(R.string.next))

        if (viewModel.isGroupDialog()) {
            applyComponentsBehaviorForGroupType()
        }

        subscribeToAvatarLoading()
        subscribeToError()

        val views = collectViewsTemplateMethod(requireContext())
        for (view in views) {
            view?.let {
                binding?.llParent?.addView(view)
            }
        }
        return binding?.root
    }

    private fun applyComponentsBehaviorForGroupType() {
        screenSettings?.getHeaderComponent()?.setTitle(getString(R.string.new_group_dialog))
        screenSettings?.getHeaderComponent()?.setTextRightButton(getString(R.string.next))
        screenSettings?.getHeaderComponent()?.setRightButtonNotClickableState()
    }

    private fun subscribeToAvatarLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                screenSettings?.getNameComponent()?.showAvatarProgress()
            } else {
                screenSettings?.getNameComponent()?.hideAvatarProgress()
            }
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
            screenSettings?.getNameComponent()?.hideAvatarProgress()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    protected open fun backPressed() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    protected open fun nextPressed() {
        if (viewModel.isGroupDialog()) {
            startUsersActivity()
        }
    }

    private fun startUsersActivity() {
        val isLoading = viewModel.loading.value
        if (isLoading == true) {
            showToast("Please wait for the avatar to load.")
        } else {
            val dialogEntity = viewModel.getDialogEntity()

            val name = screenSettings?.getNameComponent()?.getNameText()
            dialogEntity?.setName(name)
            UsersActivity.show(requireActivity(), dialogEntity)
        }
    }

    protected open fun avatarPressed() {
        AvatarDialog.show(
            requireContext(), getString(R.string.change_photo), screenSettings?.getTheme(), AvatarDialogListenerImpl()
        )
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
                val uri = viewModel.getUri()
                val view = screenSettings?.getNameComponent()?.getAvatarView()
                view?.loadCircleImageFromUri(uri, R.drawable.dialog_preview)

                uploadFileBy(uri)
            }
        }
    }

    private fun registerGalleryLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val view = screenSettings?.getNameComponent()?.getAvatarView()
            view?.loadCircleImageFromUri(uri, R.drawable.dialog_preview)

            uploadFileBy(uri)
        }
    }

    private fun uploadFileBy(uri: Uri?) {
        lifecycleScope.launch {
            val file = uri?.let {
                viewModel.getFileBy(it)
            }
            viewModel.uploadFile(file)
        }
    }

    private fun checkPermissionRequest(): Boolean {
        val checkedCameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return checkedCameraPermission == PackageManager.PERMISSION_GRANTED
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
            PositiveNegativeDialog.show(requireContext(),
                contentText,
                positiveText,
                negativeText,
                screenSettings?.getTheme(),
                positiveListener = {
                    requestCameraPermission()
                },
                negativeListener = {
                    showToast(getString(R.string.permission_denied))
                })
        } else {
            requestCameraPermission()
        }
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
            screenSettings?.getNameComponent()?.setDefaultPlaceHolderAvatar()
        }
    }
}