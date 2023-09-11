/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.components.messages

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.MessagesComonentBinding
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageIncomingViewHolder.ImageIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageOutgoingViewHolder.ImageOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponent
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class MessagesComponentImpl : ConstraintLayout, MessagesComponent {
    private var binding: MessagesComonentBinding? = null

    private var theme: UiKitTheme = LightUIKitTheme()
    private var adapter: MessageAdapter? = MessageAdapter()

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val rootView: View = inflate(context, R.layout.messages_comonent, this)
        binding = MessagesComonentBinding.bind(rootView)

        setDefaultState()
    }

    private fun setDefaultState() {
        binding?.rvMessages?.background = ColorDrawable(theme.getMainBackgroundColor())
        adapter?.setTheme(theme)
        binding?.sendMessageComponent?.setTheme(theme)
        binding?.rvMessages?.itemAnimator = null
        binding?.rvMessages?.adapter = adapter
        binding?.progressBar?.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())
    }

    override fun getSendMessageComponent(): SendMessageComponent? {
        return binding?.sendMessageComponent
    }

    override fun setAdapter(adapter: MessageAdapter?) {
        this.adapter = adapter
    }

    override fun getAdapter(): MessageAdapter? {
        return adapter
    }

    override fun setImageIncomingListener(listener: ImageIncomingListener?) {
        adapter?.setImageIncomingListener(listener)
    }

    override fun getImageIncomingListener(): ImageIncomingListener? {
        return adapter?.getImageIncomingListener()
    }

    override fun setImageOutgoingListener(listener: ImageOutgoingListener?) {
        adapter?.setImageOutgoingListener(listener)
    }

    override fun getImageOutgoingListener(): ImageOutgoingListener? {
        return adapter?.getImageOutgoingListener()
    }

    override fun setTextIncomingListener(listener: TextIncomingViewHolder.TextIncomingListener?) {
        adapter?.setTextIncomingListener(listener)
    }

    override fun getTextIncomingListener(): TextIncomingViewHolder.TextIncomingListener? {
        return adapter?.getTextIncomingListener()
    }

    override fun enableAI() {

    }

    override fun disableAI() {

    }

    override fun setAIListener(listener: TextIncomingViewHolder.AIListener) {
        adapter?.setAIListener(listener)
    }

    override fun getAIListener(): TextIncomingViewHolder.AIListener? {
        return adapter?.getAIListener()
    }

    override fun setTextOutgoingListener(listener: TextOutgoingViewHolder.TextOutgoingListener?) {
        adapter?.setTextOutgoingListener(listener)
    }

    override fun getTextOutgoingListener(): TextOutgoingViewHolder.TextOutgoingListener? {
        return adapter?.getTextOutgoingListener()
    }

    override fun setVideoOutgoingListener(listener: VideoOutgoingViewHolder.VideoOutgoingListener?) {
        adapter?.setVideoOutgoingListener(listener)
    }

    override fun getVideoOutgoingListener(): VideoOutgoingViewHolder.VideoOutgoingListener? {
        return adapter?.getVideoOutgoingListener()
    }

    override fun setVideoIncomingListener(listener: VideoIncomingViewHolder.VideoIncomingListener?) {
        adapter?.setVideoIncomingListener(listener)
    }

    override fun getVideoIncomingListener(): VideoIncomingViewHolder.VideoIncomingListener? {
        return adapter?.getVideoIncomingListener()
    }

    override fun setFileOutgoingListener(listener: FileOutgoingViewHolder.FileOutgoingListener?) {
        adapter?.setFileOutgoingListener(listener)
    }

    override fun getFileOutgoingListener(): FileOutgoingViewHolder.FileOutgoingListener? {
        return adapter?.getFileOutgoingListener()
    }

    override fun setFileIncomingListener(listener: FileIncomingViewHolder.FileIncomingListener?) {
        adapter?.setFileIngoingListener(listener)
    }

    override fun getFileIncomingListener(): FileIncomingViewHolder.FileIncomingListener? {
        return adapter?.getFileIngoingListener()
    }

    override fun setAudioOutgoingListener(listener: AudioOutgoingViewHolder.AudioOutgoingListener?) {
        adapter?.setAudioOutgoingListener(listener)
    }

    override fun getAudioOutgoingListener(): AudioOutgoingViewHolder.AudioOutgoingListener? {
        return adapter?.getAudioOutgoingListener()
    }

    override fun setAudioIncomingListener(listener: AudioIncomingViewHolder.AudioIncomingListener?) {
        adapter?.setAudioIncomingListener(listener)
    }

    override fun getAudioIncomingListener(): AudioIncomingViewHolder.AudioIncomingListener? {
        return adapter?.getAudioIncomingListener()
    }

    override fun setOnScrollListener(listener: RecyclerView.OnScrollListener) {
        binding?.rvMessages?.removeOnScrollListener(listener)
        binding?.rvMessages?.addOnScrollListener(listener)
    }

    override fun scrollToDown() {
        val size = adapter?.getItems()?.size
        if (size != null && size > 0) {
            binding?.rvMessages?.scrollToPosition(0)
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        setDefaultState()
    }

    override fun getView(): View {
        return this
    }

    override fun showProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding?.progressBar?.visibility = View.GONE
    }
}