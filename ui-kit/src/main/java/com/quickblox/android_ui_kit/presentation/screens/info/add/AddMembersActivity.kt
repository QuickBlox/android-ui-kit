/*
 * Created by Injoit on 19.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.info.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.databinding.ContainerActivityBinding
import com.quickblox.android_ui_kit.presentation.base.BaseActivity

private const val DIALOG_ID_EXTRA = "dialog_id"

class AddMembersActivity : BaseActivity() {
    private lateinit var binding: ContainerActivityBinding

    companion object {
        private var screenSettings: AddMembersScreenSettings? = null

        fun show(
            context: Context, dialogId: String? = null, addMembersScreenSettings: AddMembersScreenSettings? = null
        ) {
            addMembersScreenSettings?.let {
                this.screenSettings = it
            }

            val intent = Intent(context, AddMembersActivity::class.java)
            intent.putExtra(DIALOG_ID_EXTRA, dialogId)
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

        val dialogId = intent.getStringExtra(DIALOG_ID_EXTRA)

        val fragment: Fragment = QuickBloxUiKit.getScreenFactory().createAddMembers(dialogId, screenSettings)
        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
            .addToBackStack(AddMembersFragment.TAG).commit()
    }
}