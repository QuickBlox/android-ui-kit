/*
 * Created by Injoit on 23.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.screens.create.users

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.databinding.ContainerActivityBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.base.BaseActivity
import com.quickblox.android_ui_kit.presentation.screens.serializable

private const val DIALOG_EXTRA = "dialog_extra"

class UsersActivity : BaseActivity() {
    private lateinit var binding: ContainerActivityBinding

    companion object {
        private var screenSettings: UsersScreenSettings? = null

        fun show(context: Context, dialogEntity: DialogEntity?, usersScreenSettings: UsersScreenSettings? = null) {
            usersScreenSettings?.let {
                this.screenSettings = it
            }
            val intent = Intent(context, UsersActivity::class.java)
            intent.putExtra(DIALOG_EXTRA, dialogEntity)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = QuickBloxUiKit.getTheme().getStatusBarColor()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        binding = ContainerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dialogEntity = intent.serializable<DialogEntity>(DIALOG_EXTRA)

        val fragment: Fragment = QuickBloxUiKit.getScreenFactory().createUsers(dialogEntity, screenSettings)
        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
            .addToBackStack(UsersFragment.TAG).commit()
    }
}