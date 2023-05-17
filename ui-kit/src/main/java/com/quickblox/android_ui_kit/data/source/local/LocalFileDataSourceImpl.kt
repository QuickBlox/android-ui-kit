/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.local

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.core.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection
import java.text.ParseException


private const val DIVIDER = "#_divider_#"

@ExcludeFromCoverage
class LocalFileDataSourceImpl(private val context: Context) : LocalFileDataSource {
    private val exceptionFactory = LocalFileDataSourceExceptionFactoryImpl()

    override fun getFile(dto: LocalFileDTO): LocalFileDTO {
        if (isEmptyIdOrName(dto)) {
            throw exceptionFactory.makeIncorrectData("Id or name is empty")
        }

        val file = File(context.cacheDir, generateNameFrom(dto))

        val isNotExistFile = !file.exists()
        if (isNotExistFile) {
            throw exceptionFactory.makeNotFound("File doesn't exist")
        }
        try {
            return createAndFillLocalFileDTO(file, dto.type)
        } catch (exception: IOException) {
            throw exceptionFactory.makeWriteAndRead(exception.message.toString())
        } catch (exception: OutOfMemoryError) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: ParseException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    @SuppressLint("Recycle")
    override fun getFileByUri(dto: LocalFileDTO): LocalFileDTO {
        val uri = dto.uri

        if (uri == null || uri.toString().isEmpty()) {
            throw exceptionFactory.makeIncorrectData("Uri can't be null or empty")
        }

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()

            val name = it.getString(nameIndex)
            val file = File(context.cacheDir, name)

            // TODO: We need to write bytes when have file from gallery, but need just return file from camera. Because the file
            //  already has bytes
            if (file.length() == 0L) {
                val input = context.contentResolver.openInputStream(uri)
                val output = FileOutputStream(file)

                IOUtils.copy(input, output)
                input?.close()
                output.close()
            }

            dto.file = file
            dto.name = file.name
            dto.mimeType = getMimeType(file)
            dto.url = file.toURI().toURL().toString()
        }

        return dto
    }

    private fun createAndFillLocalFileDTO(file: File, type: String?): LocalFileDTO {
        val dto = LocalFileDTO()
        dto.data = file.readBytes()
        dto.id = parseIdFrom(file.name)
        dto.name = parseNameFrom(file.name)
        dto.type = type

        return dto
    }

    private fun parseIdFrom(source: String): String {
        val splitSource = source.split(DIVIDER, limit = 2)
        if (splitSource.isEmpty() || splitSource[0].isEmpty()) {
            throw ParseException("id", 0)
        }
        return splitSource[0]
    }

    private fun parseNameFrom(source: String): String {
        val splitSource = source.split(DIVIDER, limit = 2)
        if (splitSource.isEmpty() || splitSource[1].isEmpty()) {
            throw ParseException("name", 1)
        }
        return splitSource[1]
    }

    override fun createFile(dto: LocalFileDTO) {
        if (isEmptyIdOrName(dto)) {
            throw exceptionFactory.makeIncorrectData("Id or name is empty")
        }
        val name = generateNameFrom(dto)
        val file = File(context.cacheDir, name)

        if (file.exists()) {
            throw exceptionFactory.makeAlreadyExist("File already exist")
        }

        try {
            // TODO: Need to add check for NULL
            dto.data?.let {
                file.writeBytes(it)
            }
        } catch (exception: FileNotFoundException) {
            throw exceptionFactory.makeWriteAndRead(exception.message.toString())
        } catch (exception: SecurityException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
    }

    override fun createFile(extension: String): LocalFileDTO {
        val name = generateName(extension)

        val file = File(context.cacheDir, name)
        val uri = getUriFrom(file)

        val dto = LocalFileDTO()
        dto.name = name
        dto.file = file
        dto.uri = uri
        dto.mimeType = getMimeType(file)
        dto.url = file.toURI().toURL().toString()

        return dto
    }

    private fun getUriFrom(file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    }

    private fun getMimeType(file: File): String {
        return URLConnection.guessContentTypeFromName(file.name)
    }

    override fun deleteFile(dto: LocalFileDTO) {
        if (isEmptyIdOrName(dto)) {
            throw exceptionFactory.makeIncorrectData("Id or name is empty")
        }

        val name = generateNameFrom(dto)
        val file = File(context.cacheDir, name)
        val isNotDeleted: Boolean
        try {
            isNotDeleted = !file.delete()
        } catch (exception: SecurityException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
        if (isNotDeleted) {
            throw exceptionFactory.makeUnexpected("File has not been deleted")
        }
    }

    override fun clearAll() {
        val cacheDirectory = File(context.cacheDir.path)
        val files = cacheDirectory.listFiles()
        files?.forEach { file ->
            val isNotDeleted: Boolean
            try {
                isNotDeleted = !file.delete()
            } catch (exception: SecurityException) {
                throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
            }
            if (isNotDeleted) {
                throw exceptionFactory.makeUnexpected("File has not been deleted")
            }
        }
    }

    private fun generateName(fileExtension: String): String {
        return "${System.currentTimeMillis()}_temp_file.$fileExtension"
    }

    private fun generateNameFrom(dto: LocalFileDTO): String {
        return "${dto.id}$DIVIDER${dto.name}"
    }

    private fun isEmptyIdOrName(dto: LocalFileDTO): Boolean {
        return dto.id.isNullOrEmpty() || dto.name.isNullOrEmpty()
    }
}