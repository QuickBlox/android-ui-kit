/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens.dialogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.databinding.ContainerActivityBinding
import com.quickblox.android_ui_kit.presentation.base.BaseActivity

class DialogsActivity : BaseActivity() {
    private lateinit var binding: ContainerActivityBinding

    companion object {
        private var screenSettings: DialogsScreenSettings? = null

        fun show(context: Context, dialogsScreenSettings: DialogsScreenSettings? = null) {
            dialogsScreenSettings?.let {
                this.screenSettings = it
            }
            val intent = Intent(context, DialogsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = QuickBloxUiKit.getTheme().getStatusBarColor()

        binding = ContainerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        val fragment: Fragment = QuickBloxUiKit.getScreenFactory().createDialogs(screenSettings)

        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
            .addToBackStack(DialogsFragment.TAG).commit()
    }
}