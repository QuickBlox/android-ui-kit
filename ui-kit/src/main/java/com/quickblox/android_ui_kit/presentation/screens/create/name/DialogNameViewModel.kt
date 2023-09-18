/*
 * Created by Injoit on 23.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.create.name

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.CreateLocalFileUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetLocalFileByUriUseCase
import com.quickblox.android_ui_kit.domain.usecases.UploadFileUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel

class DialogNameViewModel : BaseViewModel() {
    private var dialogEntity: DialogEntity? = null
    private var uri: Uri? = null

    fun setDialogEntity(dialogEntity: DialogEntity?) {
        this.dialogEntity = dialogEntity
    }

    fun isGroupDialog(): Boolean {
        return dialogEntity?.getType() == DialogEntity.Types.GROUP
    }

    fun getDialogEntity(): DialogEntity? {
        return dialogEntity
    }

    fun getUri(): Uri? {
        return uri
    }

    suspend fun createFileAndGetUri(): Uri? {
        try {
            val fileEntity = CreateLocalFileUseCase("jpg").execute()
            uri = fileEntity?.getUri()
            return uri
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun getFileBy(uri: Uri): FileEntity? {
        try {
            val file = GetLocalFileByUriUseCase(uri).execute()
            return file
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun uploadFile(fileEntity: FileEntity?) {
        if (fileEntity == null) {
            showError("The file doesn't exist")
            return
        }

        showLoading()
        try {
            val remoteEntity = UploadFileUseCase(fileEntity).execute()
            dialogEntity?.setPhoto(remoteEntity?.getUid())
            hideLoading()
        } catch (exception: DomainException) {
            hideLoading()
            showError(exception.message)
        }
    }
}