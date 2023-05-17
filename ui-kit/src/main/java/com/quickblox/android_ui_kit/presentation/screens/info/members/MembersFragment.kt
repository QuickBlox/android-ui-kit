/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.members

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ContainerFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseFragment
import com.quickblox.android_ui_kit.presentation.components.users.members.MembersAdapter
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.screens.info.add.AddMembersActivity

open class MembersFragment : BaseFragment() {
    private val viewModel by viewModels<MembersViewModel>()
    private var binding: ContainerFragmentBinding? = null
    private var screenSettings: MembersScreenSettings? = null
    private var dialogId: String? = null

    companion object {
        val TAG: String = MembersFragment::class.java.simpleName

        fun newInstance(dialogId: String? = null, screenSettings: MembersScreenSettings? = null): MembersFragment {
            val membersFragment = MembersFragment()
            membersFragment.dialogId = dialogId
            membersFragment.screenSettings = screenSettings
            return membersFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (screenSettings == null) {
            screenSettings = MembersScreenSettings.Builder(requireContext()).build()
        }

        dialogId?.let {
            viewModel.setDialogIdAndSubscribeToConnection(it)
        }

        initHeaderComponentListeners()
        screenSettings?.getUsersComponent()?.setVisibleSearch(false)
    }

    override fun onResume() {
        viewModel.loadDialogAndMembers()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ContainerFragmentBinding.inflate(inflater, container, false)

        screenSettings?.getHeaderComponent()?.setTitle(getString(R.string.members))
        screenSettings?.getHeaderComponent()?.setImageRightButton(R.drawable.add)
        val adapter = (screenSettings?.getUsersComponent()?.getAdapter() as MembersAdapter)

        adapter.setRemoveClickListener { userEntity ->
            showRemoveDialog(userEntity)
        }

        subscribeToMembers()
        subscribeToAvatarLoading()
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
        val contentText = "Are you sure you want to remove ${userEntity?.getName()}?"
        val positiveText = getString(R.string.remove)
        val negativeText = getString(R.string.cancel)
        PositiveNegativeDialog.show(
            requireContext(), contentText, positiveText, negativeText, screenSettings?.getTheme(),
            positiveListener = {
                userEntity?.getUserId()?.let {
                    viewModel.removeUserAndLoadMembersAndDialog(it)
                }
            })
    }

    private fun subscribeToMembers() {
        val adapter = (screenSettings?.getUsersComponent()?.getAdapter() as MembersAdapter)

        viewModel.loadedMembers.observe(viewLifecycleOwner) {
            adapter.setLoggedUserId(viewModel.getLoggedUserId())
            adapter.setOwnerId(viewModel.getOwnerId())
            adapter.setItems(viewModel.loadedUsers)

            adapter.notifyDataSetChanged()
        }
    }

    private fun subscribeToAvatarLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                screenSettings?.getUsersComponent()?.showProgress()
            } else {
                screenSettings?.getUsersComponent()?.hideProgress()
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
        AddMembersActivity.show(requireContext(), dialogId)
    }
}