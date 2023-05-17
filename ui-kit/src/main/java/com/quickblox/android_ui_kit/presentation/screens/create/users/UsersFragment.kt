/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.screens.create.users

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponentImpl
import com.quickblox.android_ui_kit.presentation.components.users.selection.SelectionUsersAdapter
import com.quickblox.android_ui_kit.presentation.screens.chat.group.GroupChatActivity
import com.quickblox.android_ui_kit.presentation.screens.chat.individual.PrivateChatActivity

open class UsersFragment : BaseFragment() {
    companion object {
        val TAG: String = UsersFragment::class.java.simpleName

        fun newInstance(dialogEntity: DialogEntity?, screenSettings: UsersScreenSettings? = null): UsersFragment {
            val usersFragment = UsersFragment()
            usersFragment.screenSettings = screenSettings
            usersFragment.dialogEntity = dialogEntity
            return usersFragment
        }
    }

    private val viewModel by viewModels<UsersViewModel>()
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: UsersScreenSettings? = null
    private var dialogEntity: DialogEntity? = null
    private var scrollListenerImpl = ScrollListenerImpl()

    override fun collectViewsTemplateMethod(context: Context): List<View?> {
        val views = mutableListOf<View?>()
        views.add(screenSettings?.getHeaderComponent()?.getView())
        views.add(screenSettings?.getUsersComponent()?.getView())
        return views
    }

    fun setSettings(screenSettings: UsersScreenSettings) {
        this.screenSettings = screenSettings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = UsersScreenSettings.Builder(requireContext()).build()
        }

        viewModel.setDialogEntity(dialogEntity)

        initHeaderComponentListeners()
        initUsersComponentListeners()
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

    private fun initUsersComponentListeners() {
        val usersComponent = screenSettings?.getUsersComponent()

        val searchClickListener = usersComponent?.getSearchComponent()?.getSearchClickListener()
        if (searchClickListener == null) {
            usersComponent?.getSearchComponent()
                ?.setSearchClickListener(object : SearchComponentImpl.SearchEventListener {
                    override fun onSearchEvent(text: String) {
                        viewModel.cleanAndLoadUsersBy(text)
                    }

                    override fun onDefaultEvent() {
                        if (viewModel.isSearchingEvent) {
                            viewModel.loadAllCleanUsers()
                        }
                    }
                })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)

        screenSettings?.getHeaderComponent()?.setTextRightButton(getString(R.string.create))

        val adapter = screenSettings?.getUsersComponent()?.getAdapter() as SelectionUsersAdapter
        if (viewModel.isPrivateDialog()) {
            applyComponentsBehaviorForPrivateType(adapter)
        }

        applyCommonComponentsBehavior(adapter)

        subscribeToLoading()
        subscribeToAddedUser()
        subscribeToError()
        subscribeToCreateDialog()

        viewModel.loadAllUsers()

        val views = collectViewsTemplateMethod(requireContext())
        views.forEach { view ->
            view?.let {
                binding?.llParent?.addView(view)
            }
        }

        return binding?.root
    }

    private fun applyCommonComponentsBehavior(adapter: SelectionUsersAdapter) {
        screenSettings?.getTheme()?.getMainBackgroundColor()?.let {
            binding?.root?.setBackgroundColor(it)
        }
        adapter.setItems(viewModel.loadedUsers)
        adapter.setSelectedUsers(viewModel.selectedUsers)
        screenSettings?.getUsersComponent()?.setOnScrollListener(scrollListenerImpl)
    }

    private fun applyComponentsBehaviorForPrivateType(adapter: SelectionUsersAdapter) {
        screenSettings?.getHeaderComponent()?.setRightButtonNotClickableState()
        adapter.setSelectionStrategy(SelectionUsersAdapter.SelectionStrategy.SINGLE)
        adapter.setSingleSelectionCompleteListener(object : SelectionUsersAdapter.SingleSelectionCompleteListener {
                override fun onSingleSelectionCompleted(isCompleted: Boolean) {
                    if (isCompleted) {
                        screenSettings?.getHeaderComponent()?.setRightButtonClickableState()
                    } else {
                        screenSettings?.getHeaderComponent()?.setRightButtonNotClickableState()
                    }
                }
            })
    }

    private fun subscribeToLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                screenSettings?.getUsersComponent()?.showProgress()
                scrollListenerImpl.isLoad = false
            } else {
                screenSettings?.getUsersComponent()?.hideProgress()
            }
        }
    }

    private fun subscribeToAddedUser() {
        viewModel.addedUser.observe(viewLifecycleOwner) {
            screenSettings?.getUsersComponent()?.getAdapter()?.notifyDataSetChanged()
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    private fun subscribeToCreateDialog() {
        viewModel.createdDialog.observe(viewLifecycleOwner) { dialogEntity ->
            when (dialogEntity?.getType()) {
                DialogEntity.Types.GROUP -> {
                    startGroupChatActivity(dialogEntity.getDialogId())
                }
                DialogEntity.Types.PRIVATE -> {
                    startPrivateChatActivity(dialogEntity.getDialogId())
                }
                else -> {
                    showToast(getString(R.string.wrong_dialog_type))
                }
            }
        }
    }

    private fun startGroupChatActivity(dialogId: String?) {
        GroupChatActivity.show(requireContext(), dialogId)
        activity?.finishAffinity()
    }

    private fun startPrivateChatActivity(dialogId: String?) {
        PrivateChatActivity.show(requireContext(), dialogId)
        activity?.finish()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    protected open fun backPressed() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    protected open fun nextPressed() {
        when (viewModel.getDialogType()) {
            DialogEntity.Types.GROUP -> {
                viewModel.createGroupDialog()
            }
            DialogEntity.Types.PRIVATE -> {
                viewModel.createPrivateDialog()
            }
            else -> {
                showToast(getString(R.string.wrong_dialog_type))
            }
        }
    }

    private inner class ScrollListenerImpl : RecyclerView.OnScrollListener() {
        var isLoad: Boolean = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1) || isLoad) {
                isLoad = true
                viewModel.loadUsers()
            }
        }
    }
}