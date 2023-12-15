/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.features.forwarding.messages

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.MessagesSelectionFragmentBinding
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.UIKitScreen
import com.quickblox.android_ui_kit.presentation.screens.features.forwarding.recipients.RecipientSelectionActivity
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class MessagesSelectionFragment : Fragment(), UIKitScreen {
    companion object {
        val TAG: String = MessagesSelectionFragment::class.java.simpleName

        fun newInstance(
            dialogId: String? = null,
            theme: UiKitTheme? = null,
            messages: List<MessageEntity>?,
            forwardedMessage: MessageEntity?,
            paginationEntity: PaginationEntity?,
            position: Int?,
        ): MessagesSelectionFragment {
            val messagesSelectionFragment = MessagesSelectionFragment()
            messagesSelectionFragment.theme = theme
            messagesSelectionFragment.dialogId = dialogId
            messagesSelectionFragment.messages = messages
            messagesSelectionFragment.forwardedMessage = forwardedMessage
            messagesSelectionFragment.paginationEntity = paginationEntity
            messagesSelectionFragment.position = position

            return messagesSelectionFragment
        }
    }

    override fun onResume() {
        if (TextUtils.isEmpty(dialogId)) {
            Toast.makeText(requireContext(), "The dialogId shouldn't be empty", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.loadDialog(dialogId.toString())
            binding?.rvMessages?.getAdapter()?.notifyDataSetChanged()
        }
        super.onResume()
    }

    private val viewModel by viewModels<MessagesSelectionViewModel>()

    private var binding: MessagesSelectionFragmentBinding? = null

    private var theme: UiKitTheme? = LightUIKitTheme()
    private var dialogId: String? = null
    private var scrollListenerImpl = ScrollListenerImpl()

    private var messages: List<MessageEntity>? = null
    private var forwardedMessage: MessageEntity? = null
    private var paginationEntity: PaginationEntity? = null
    private var position: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MessagesSelectionFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        paginationEntity?.let {
            viewModel.setPaginationEntity(it)
        }
        messages?.let {
            viewModel.setLoadedMessages(it.toMutableList())
        }

        val adapter = binding?.rvMessages?.getAdapter()

        viewModel.messages?.let {
            adapter?.setItems(it)
        }

        enableForward(adapter)

        forwardedMessage?.let {
            adapter?.setSelectedMessages(it)
            binding?.tvTitle?.text =
                getString(R.string.messages_selected, adapter?.getSelectedMessages()?.size.toString())

            adapter?.setSelectionListener(object : MessageAdapter.SelectionListener {
                override fun onSelection(countMessages: Int) {
                    binding?.tvTitle?.text = getString(R.string.messages_selected, countMessages.toString())

                    if (countMessages < 1) {
                        binding?.clForward?.visibility = View.GONE
                        return
                    }
                    binding?.clForward?.visibility = View.VISIBLE
                }
            })
        }

        position?.let {
            binding?.rvMessages?.scrollToPosition(it)
        }
        binding?.rvMessages?.setOnScrollListener(scrollListenerImpl)

        binding?.btnRight?.setOnClickListener {
            backPressed()
        }
        binding?.tvForward?.setOnClickListener {
            val forwardedMessage = adapter?.getSelectedMessages()?.get(0)

            RecipientSelectionActivity.show(requireContext(), dialogId, forwardedMessage, theme)
        }

        subscribeToLoadedMessages()
        subscribeToMessagesLoading()
        enableAIMenu()
        enableAIRephrase()
        enableAITranslate()
    }

    private fun enableForward(adapter: MessageAdapter?) {
        if (QuickBloxUiKit.isEnabledForward()) {
            adapter?.setForwardState(true)
        } else {
            adapter?.setForwardState(false)
        }
    }

    private fun initView() {
        binding?.rvMessages?.getSendMessageComponent()?.getView()?.visibility = View.GONE

        theme?.let {
            binding?.rvMessages?.setTheme(it)
        }

        binding?.root?.background = theme?.getMainBackgroundColor()?.let {
            ColorDrawable(it)
        }

        theme?.getDividerColor()?.let {
            binding?.vDivider?.setBackgroundColor(it)
            binding?.vForwardDivider?.setBackgroundColor(it)
        }

        theme?.getMainElementsColor()?.let {
            binding?.btnRight?.setTextColor(it)
            binding?.tvForward?.setTextColor(it)
            binding?.btnRight?.makeClickableBackground(it)
            binding?.tvForward?.makeClickableBackground(it)
        }

        theme?.getMainTextColor()?.let {
            binding?.tvTitle?.setTextColor(it)
        }
    }

    private fun backPressed() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    private fun subscribeToMessagesLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding?.rvMessages?.showProgress()
                scrollListenerImpl.isLoading = false
            } else {
                binding?.rvMessages?.hideProgress()
            }
        }
    }

    private fun subscribeToLoadedMessages() {
        viewModel.loadedMessage.observe(viewLifecycleOwner) { index ->
            if (viewModel.messages?.isNotEmpty() == true) {
                binding?.rvMessages?.getAdapter()?.notifyItemChanged(index)
            }
        }
    }

    private fun enableAIMenu() {
        val enabledAI = QuickBloxUiKit.isEnabledAIAnswerAssistant()
        binding?.rvMessages?.getAdapter()?.enabledAI(enabledAI)
    }

    private fun enableAITranslate() {
        val enabled = QuickBloxUiKit.isEnabledAITranslate()
        binding?.rvMessages?.getAdapter()?.enabledAITranslate(enabled)
    }

    private fun enableAIRephrase() {
        val enabled = QuickBloxUiKit.isEnabledAIRephrase()
        binding?.rvMessages?.getSendMessageComponent()?.enableRephrase(enabled)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
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