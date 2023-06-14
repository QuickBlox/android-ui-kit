/*
 * Created by Injoit on 2.6.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.utils

import java.io.File
import java.io.RandomAccessFile

object FileUtils {
    fun buildFileWithMegaBytesLength(megabytesLength: Int): File {
        val file = File(generatePath())

        val randomAccessFile = RandomAccessFile(file, "rw")

        val fileLength = (1024 * 1024 * megabytesLength).toLong()
        randomAccessFile.setLength(fileLength)

        return file
    }

    fun generatePath(): String {
        return "${System.currentTimeMillis()}_test_path"
    }

    fun generateName(): String {
        return "${System.currentTimeMillis()}_temp_file.txt"
    }
}