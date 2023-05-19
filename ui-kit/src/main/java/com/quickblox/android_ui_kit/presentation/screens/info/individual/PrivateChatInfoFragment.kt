/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.individual

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.presentation.base.BaseFragment

open class PrivateChatInfoFragment : BaseFragment() {
    companion object {
        val TAG: String = PrivateChatInfoFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null, screenSettings: PrivateChatInfoScreenSettings? = null
        ): PrivateChatInfoFragment {
            val privateChatFragment = PrivateChatInfoFragment()
            privateChatFragment.dialogId = dialogId
            privateChatFragment.screenSettings = screenSettings
            return privateChatFragment
        }
    }

    private val viewModel by viewModels<PrivateChatInfoViewModel>()

    private var binding: ContainerFragmentBinding? = null

    private var screenSettings: PrivateChatInfoScreenSettings? = null

    private var dialogId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = PrivateChatInfoScreenSettings.Builder(requireContext()).build()
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

        val leaveClickListener = infoComponent?.getLeaveItemClickListener()
        if (leaveClickListener == null) {
            infoComponent?.setLeaveItemClickListener {
                viewModel.leaveDialog()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)

        val backGround = screenSettings?.getTheme()?.getMainBackgroundColor()

        backGround?.let {
            binding?.root?.setBackgroundColor(it)
        }

        screenSettings?.getHeaderComponent()?.setTitle(getString(R.string.dialog_info))
        screenSettings?.getHeaderComponent()?.setVisibleRightButton(false)
        screenSettings?.getInfoComponent()?.setVisibleMembersItem(false)

        val avatarHolder = ContextCompat.getDrawable(requireContext(), R.drawable.private_holder)
        screenSettings?.getInfoComponent()?.getAvatarView()?.setImageDrawable(avatarHolder)

        subscribeToLoadingDialog()
        subscribeToLeaveDialog()
        subscribeToError()

        val views = collectViewsTemplateMethod(requireContext())
        for (view in views){
            view?.let {
                binding?.llParent?.addView(view)
            }
        }

        return binding?.root
    }

    private fun subscribeToLoadingDialog() {
        viewModel.loadedDialogEntity.observe(viewLifecycleOwner) { dialogEntity ->
            val avatarUrl = dialogEntity?.getPhoto()
            screenSettings?.getInfoComponent()?.loadAvatar(
                avatarUrl.toString(),
                ContextCompat.getDrawable(requireContext(), R.drawable.private_holder)
            )

            dialogEntity?.getName()?.let {
                screenSettings?.getInfoComponent()?.setTextName(it)
            }
        }
    }

    private fun subscribeToLeaveDialog() {
        viewModel.leaveDialog.observe(viewLifecycleOwner) {
            requireActivity().finishAffinity()
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
        showToast("default next pressed")
    }
}