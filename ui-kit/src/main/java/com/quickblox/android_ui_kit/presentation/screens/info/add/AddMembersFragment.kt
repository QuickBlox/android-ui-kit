/*
 * Created by Injoit on 19.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.info.add

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponentImpl
import com.quickblox.android_ui_kit.presentation.components.users.add.AddMembersAdapter
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog

open class AddMembersFragment : BaseFragment() {
    private val viewModel by viewModels<AddMembersViewModel>()
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: AddMembersScreenSettings? = null
    private var dialogId: String? = null
    private var scrollListenerImpl = ScrollListenerImpl()

    companion object {
        val TAG: String = AddMembersFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null, screenSettings: AddMembersScreenSettings? = null
        ): AddMembersFragment {
            val membersFragment = AddMembersFragment()
            membersFragment.dialogId = dialogId
            membersFragment.screenSettings = screenSettings
            return membersFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = AddMembersScreenSettings.Builder(requireContext()).build()
        }

        if (dialogId.isNullOrEmpty()) {
            showToast("The dialogId shouldn't be empty")
        } else {
            viewModel.setDialogId(dialogId!!)
        }

        initHeaderComponentListeners()
        initUsersComponentListeners()
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

        screenSettings?.getHeaderComponent()?.setTitle(getString(R.string.add_members))
        screenSettings?.getHeaderComponent()?.setVisibleRightButton(false)

        val adapter = (screenSettings?.getUsersComponent()?.getAdapter() as AddMembersAdapter)
        adapter.setItems(viewModel.loadedUsers)
        adapter.setAddUserClickListener { userEntity ->
            showRemoveDialog(userEntity)
        }
        screenSettings?.getUsersComponent()?.setOnScrollListener(scrollListenerImpl)

        subscribeToLoadingDialog()
        subscribeToUpdateList()
        subscribeToLoading()
        subscribeToError()

        val views = collectViewsTemplateMethod(requireContext())
        views.forEach { view ->
            view?.let {
                binding?.llParent?.addView(view)
            }
        }
        return binding?.root
    }

    private fun showRemoveDialog(userEntity: UserEntity?) {
        val contentText = "Are you sure you want to add ${userEntity?.getName()} to this dialog?"
        val positiveText = getString(R.string.add)
        val negativeText = getString(R.string.cancel)
        PositiveNegativeDialog.show(
            requireContext(), contentText, positiveText, negativeText, screenSettings?.getTheme(),
            positiveListener = {
                userEntity?.let {
                    viewModel.addUser(it)
                }
            })
    }

    private fun subscribeToLoadingDialog() {
        viewModel.loadedDialogEntity.observe(viewLifecycleOwner) { dialogEntity ->
            viewModel.loadAllUsers()
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    private fun subscribeToUpdateList() {
        viewModel.updateList.observe(viewLifecycleOwner) {
            screenSettings?.getUsersComponent()?.getAdapter()?.notifyDataSetChanged()
        }
    }

    private fun subscribeToLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                screenSettings?.getUsersComponent()?.showProgress()
                scrollListenerImpl.isLoading = false
            } else {
                screenSettings?.getUsersComponent()?.hideProgress()
            }
        }
    }

    override fun collectViewsTemplateMethod(context: Context): List<View?> {
        val views = mutableListOf<View?>()
        views.add(screenSettings?.getHeaderComponent()?.getView())
        views.add(screenSettings?.getUsersComponent()?.getView())
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

    private inner class ScrollListenerImpl : RecyclerView.OnScrollListener() {
        var isLoading: Boolean = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val isScrolledToBottomScreen = !recyclerView.canScrollVertically(1)
            if (isScrolledToBottomScreen || isLoading) {
                isLoading = true
                viewModel.loadUsers()
            }
        }
    }
}