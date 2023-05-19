/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponentImpl
import com.quickblox.android_ui_kit.presentation.dialogs.CreateDialog
import com.quickblox.android_ui_kit.presentation.dialogs.MenuDialog
import com.quickblox.android_ui_kit.presentation.screens.chat.group.GroupChatActivity
import com.quickblox.android_ui_kit.presentation.screens.chat.individual.PrivateChatActivity
import com.quickblox.android_ui_kit.presentation.screens.create.name.DialogNameActivity
import com.quickblox.android_ui_kit.presentation.screens.create.users.UsersActivity

open class DialogsFragment : BaseFragment() {
    companion object {
        val TAG: String = DialogsFragment::class.java.simpleName

        fun newInstance(screenSettings: DialogsScreenSettings? = null): DialogsFragment {
            val dialogsFragment = DialogsFragment()
            dialogsFragment.screenSettings = screenSettings
            return dialogsFragment
        }
    }

    private val viewModel by viewModels<DialogsViewModel>()

    private var binding: ContainerFragmentBinding? = null

    private var screenSettings: DialogsScreenSettings? = null

    override fun onResume() {
        viewModel.getDialogs()
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = DialogsScreenSettings.Builder(requireContext()).build()
        }

        initHeaderComponentListeners()
        initDialogsComponentListeners()
    }

    override fun onStop() {
        screenSettings?.getDialogsComponent()?.getSearchComponent()?.setSearchText("")
        super.onStop()
    }

    private fun initDialogsComponentListeners() {
        val searchComponent = screenSettings?.getSearchComponent()
        val searchClickListener = searchComponent?.getSearchClickListener()
        if (searchClickListener == null) {
            searchComponent?.setSearchClickListener(object : SearchComponentImpl.SearchEventListener {
                override fun onSearchEvent(text: String) {
                    searchPressed(text)
                }

                override fun onDefaultEvent() {
                    viewModel.getDialogs()
                }
            })
        }
        val dialogsComponent = screenSettings?.getDialogsComponent()

        if (dialogsComponent?.getItemClickListener() == null) {
            dialogsComponent?.setItemClickListener { dialogEntity ->
                itemPressed(dialogEntity)
            }
        }
        if (dialogsComponent?.getItemLongClickListener() == null) {
            dialogsComponent?.setItemLongClickListener {
                itemLongPressed(it)
            }
        }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)

        val views = collectViewsTemplateMethod(requireContext())
        for (view in views){
            view?.let {
                binding?.llParent?.addView(view)
            }
        }

        screenSettings?.getDialogsComponent()?.getAdapter()?.setList(viewModel.dialogs)
        screenSettings?.getHeaderComponent()?.setImageRightButton(R.drawable.create_dialog)

        val tittle = getString(R.string.default_title_dialogs_header)
        screenSettings?.getHeaderComponent()?.setTitle(tittle)

        subscribeToSyncing()
        subscribeToAddedUser()
        subscribeToError()

        return binding?.root
    }

    private fun subscribeToAddedUser() {
        viewModel.updatedDialogs.observe(viewLifecycleOwner) {
            screenSettings?.getDialogsComponent()?.getAdapter()?.notifyDataSetChanged()
        }
    }

    private fun subscribeToSyncing() {
        viewModel.syncing.observe(viewLifecycleOwner) { isSyncing ->
            if (isSyncing) {
                screenSettings?.getDialogsComponent()?.showProgressSync()
            } else {
                screenSettings?.getDialogsComponent()?.hideProgressSync()
            }
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    fun setSettings(screenSettings: DialogsScreenSettings) {
        this.screenSettings = screenSettings
    }

    override fun collectViewsTemplateMethod(context: Context): List<View?> {
        val views = mutableListOf<View?>()
        views.add(screenSettings?.getHeaderComponent()?.getView())
        views.add(screenSettings?.getDialogsComponent()?.getView())
        return views
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    protected open fun backPressed() {
        showToast("default back pressed")
    }

    protected open fun nextPressed() {
        CreateDialog.show(requireContext(), screenSettings?.getTheme(), object : CreateDialog.CreateDialogListener {
            override fun onClickPrivate() {
                val dialogEntity = viewModel.createPrivateDialogEntity()
                UsersActivity.show(requireActivity(), dialogEntity)
            }

            override fun onClickGroup() {
                val dialogEntity = viewModel.createGroupDialogEntity()
                DialogNameActivity.show(requireActivity(), dialogEntity)
            }
        })
    }

    protected open fun searchPressed(text: String) {
        viewModel.searchByName(text)
    }

    protected open fun itemPressed(dialogEntity: DialogEntity) {
        when (dialogEntity.getType()) {
            DialogEntity.Types.GROUP -> {
                startGroupChatActivity(dialogEntity)
            }
            DialogEntity.Types.PRIVATE -> {
                startPrivateChatActivity(dialogEntity)
            }
            else -> {
                showToast(getString(R.string.wrong_dialog_type))
            }
        }
    }

    private fun startGroupChatActivity(dialogEntity: DialogEntity) {
        val dialogId = dialogEntity.getDialogId()
        GroupChatActivity.show(requireContext(), dialogId)
    }

    private fun startPrivateChatActivity(dialogEntity: DialogEntity) {
        val dialogId = dialogEntity.getDialogId()
        PrivateChatActivity.show(requireContext(), dialogId)
    }

    protected open fun itemLongPressed(dialog: DialogEntity) {
        MenuDialog.show(requireContext(), dialog, screenSettings?.getTheme(), object : MenuDialog.DialogMenuListener {
            override fun onClickLeave(dialog: DialogEntity) {
                viewModel.leaveDialog(dialog)
            }
        })
    }
}