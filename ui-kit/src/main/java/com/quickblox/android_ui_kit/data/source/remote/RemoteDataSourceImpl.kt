/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote

import android.os.Bundle
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessagePaginationDTO
import com.quickblox.android_ui_kit.data.dto.remote.typing.RemoteTypingDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserFilterDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserPaginationDTO
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.remote.listener.ChatMessageListener
import com.quickblox.android_ui_kit.data.source.remote.listener.StatusMessageListener
import com.quickblox.android_ui_kit.data.source.remote.listener.SystemMessageListener
import com.quickblox.android_ui_kit.data.source.remote.listener.TypingListener
import com.quickblox.android_ui_kit.data.source.remote.mapper.*
import com.quickblox.android_ui_kit.data.source.remote.parser.EventMessageParser
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.Query
import com.quickblox.chat.*
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.chat.request.QBMessageUpdateBuilder
import com.quickblox.content.QBContent
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jivesoftware.smack.AbstractConnectionListener
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.SmackException.NotConnectedException
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.filter.MessageTypeFilter
import org.jivesoftware.smackx.muc.DiscussionHistory
import java.io.IOException
import java.lang.reflect.Field


@ExcludeFromCoverage
open class RemoteDataSourceImpl : RemoteDataSource {
    val TAG = RemoteDataSourceImpl::class.java.simpleName

    private val xmppConnectionListener = XMPPConnectionListener()

    private val exceptionFactory = RemoteDataSourceExceptionFactoryImpl()

    private val connectionFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val dialogsEventFlow: MutableSharedFlow<RemoteDialogDTO?> = MutableSharedFlow(0, extraBufferCapacity = 1)
    private val messagesEventFlow: MutableSharedFlow<RemoteMessageDTO?> = MutableSharedFlow(0, extraBufferCapacity = 1)
    private val typingEventFlow: MutableSharedFlow<RemoteTypingDTO?> = MutableSharedFlow(0, extraBufferCapacity = 1)

    private val statusMessageListener = StatusMessageListener(messagesEventFlow)
    private val chatMessageListener = ChatMessageListener(messagesEventFlow, dialogsEventFlow)
    private val systemMessageListener = SystemMessageListener(dialogsEventFlow)

    override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
        try {
            val qbDialog = RemoteDialogDTOMapper.toQBDialogFrom(dto)
            val qbCreatedDialog = QBRestChatService.createChatDialog(qbDialog).perform()

            val needJoin = !qbCreatedDialog.isPrivate && !qbCreatedDialog.isJoined
            if (needJoin) {
                qbCreatedDialog.join(DiscussionHistory())
            }

            val dialogDTO = RemoteDialogDTOMapper.toDTOFrom(qbCreatedDialog, getLoggedUserId())

            if (qbCreatedDialog.isPrivate) {
                val opponentAvatarUid = getOpponentAvatarUid(qbCreatedDialog)
                dialogDTO.photo = opponentAvatarUid
            }

            return dialogDTO
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: XMPPException) {
            throw exceptionFactory.makeConnectionFailed("XMPP connection failed ")
        } catch (exception: SmackException) {
            throw exceptionFactory.makeConnectionFailed("XMPP connection failed")
        }
    }

    override fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
        val requestBuilder = QBDialogRequestBuilder()
        try {
            val localDialog = RemoteDialogDTOMapper.toQBDialogFrom(dto)
            val remoteDialog = QBRestChatService.updateChatDialog(localDialog, requestBuilder).perform()

            return RemoteDialogDTOMapper.toDTOFrom(remoteDialog, getLoggedUserId())
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
        val dialogId = RemoteDialogDTOMapper.getDialogIdFrom(dto)
        val qbChatDialog: QBChatDialog

        try {
            qbChatDialog = QBRestChatService.getChatDialogById(dialogId).perform()
            val needJoin = !qbChatDialog.isPrivate && !qbChatDialog.isJoined
            if (needJoin) {
                qbChatDialog.join(DiscussionHistory())
            }

            val dialogDTO = RemoteDialogDTOMapper.toDTOFrom(qbChatDialog, getLoggedUserId())
            if (qbChatDialog.isPrivate) {
                val opponentAvatarUid = getOpponentAvatarUid(qbChatDialog)
                dialogDTO.photo = opponentAvatarUid
            }

            return dialogDTO
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: XMPPException) {
            throw exceptionFactory.makeConnectionFailed("dialogId: $dialogId")
        } catch (exception: SmackException) {
            throw exceptionFactory.makeConnectionFailed("dialogId: $dialogId")
        }
    }

    override fun getAllDialogs(): Flow<Result<RemoteDialogDTO>> {
        return channelFlow {
            val dialogs = mutableListOf<QBChatDialog>()
            try {
                val qbDialogs = loadAllQBDialogs()
                dialogs.addAll(qbDialogs)
            } catch (exception: RemoteDataSourceException) {
                val defaultErrorMessage = RemoteDataSourceException.Codes.CONNECTION_FAILED.toString()
                send(Result.failure(exceptionFactory.makeUnexpected(exception.message ?: defaultErrorMessage)))
            }

            for (qbChatDialog in dialogs) {
                val isNotActiveScope = !coroutineContext.isActive
                if (isNotActiveScope) {
                    return@channelFlow
                }

                try {
                    val dialogDTO = RemoteDialogDTOMapper.toDTOFrom(qbChatDialog, getLoggedUserId())

                    if (qbChatDialog.isPrivate) {
                        val opponentAvatarUrl = getOpponentAvatarUid(qbChatDialog)
                        dialogDTO.photo = opponentAvatarUrl
                    }

                    val needJoin = !qbChatDialog.isPrivate && !qbChatDialog.isJoined
                    if (needJoin) {
                        qbChatDialog.join(DiscussionHistory())
                    }

                    send(Result.success(dialogDTO))
                } catch (exception: RemoteDataSourceException) {
                    val defaultErrorMessage = RemoteDataSourceException.Codes.CONNECTION_FAILED.toString()
                    send(Result.failure(exceptionFactory.makeUnexpected(exception.message ?: defaultErrorMessage)))
                } catch (exception: MappingException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeIncorrectData(errorMessage)))
                } catch (exception: XMPPException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeConnectionFailed(errorMessage)))
                } catch (exception: SmackException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeConnectionFailed(errorMessage)))
                } catch (exception: IllegalStateException) {
                    val errorMessage = exception.message.toString() + " dialogId: ${qbChatDialog.dialogId}"
                    send(Result.failure(exceptionFactory.makeUnexpected(errorMessage)))
                }
            }
        }
    }

    private fun getOpponentAvatarUrl(dialog: QBChatDialog): String? {
        var opponentAvatarUrl: String? = null

        try {
            val opponentId = getOpponentIdFromPrivate(dialog)
            val opponent = loadUserById(opponentId)
            opponentAvatarUrl = loadUserAvatarUrl(opponent?.fileId)
        } catch (exception: RuntimeException) {
            Log.e(TAG, exception.message.toString())
        }
        return opponentAvatarUrl
    }

    private fun getOpponentAvatarUid(dialog: QBChatDialog): String? {
        var opponentAvatarUid: String? = null

        try {
            val opponentId = getOpponentIdFromPrivate(dialog)
            val opponent = loadUserById(opponentId)
            opponentAvatarUid = loadUserAvatarUid(opponent?.fileId)
        } catch (exception: RuntimeException) {
            Log.e(TAG, exception.message.toString())
        }
        return opponentAvatarUid
    }

    private fun getOpponentIdFromPrivate(dialog: QBChatDialog): Int? {
        val isNotPrivate = !dialog.isPrivate
        if (isNotPrivate || dialog.occupants.isEmpty()) {
            throw RuntimeException("Dialog is not private or not contains opponents")
        }

        return dialog.occupants.find { it != getLoggedUserId() }
    }

    private fun loadUserById(userId: Int?): QBUser? {
        if (userId == null || userId <= 0) {
            throw RuntimeException("user Id can't be null")
        }

        try {
            return QBUsers.getUser(userId).perform()
        } catch (exception: QBResponseException) {
            throw RuntimeException("Error loading user  userId - $userId")
        }
    }

    @VisibleForTesting
    open fun loadAllQBDialogs(requestBuilder: QBRequestGetBuilder = QBRequestGetBuilder()): List<QBChatDialog> {
        try {
            setSortDescDialogsByLastMessageDate(requestBuilder)
            setFilterDialogsWithoutPublic(requestBuilder)

            val performer = QBRestChatService.getChatDialogs(null, requestBuilder) as Query
            val dialogs = performer.perform()

            // TODO: Need to add  pagination
            val bundle = performer.bundle

            return dialogs
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
    }

    private fun setSortDescDialogsByLastMessageDate(requestBuilder: QBRequestGetBuilder) {
        requestBuilder.sortDesc("last_message_date_sent")
    }

    private fun setFilterDialogsWithoutPublic(requestBuilder: QBRequestGetBuilder) {
        requestBuilder.`in`("type", QBDialogType.PRIVATE.code, QBDialogType.GROUP.code)
    }

    override fun leaveDialog(dto: RemoteDialogDTO) {
        try {
            if (dto.type == QBDialogType.GROUP.code) {
                leaveFromXMPPAndUpdateGroupDialog(dto)
            } else if (dto.type == QBDialogType.PRIVATE.code) {
                leaveFromPrivateDialog(dto.id)
            }
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: XMPPException) {
            exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: NotConnectedException) {
            exceptionFactory.makeConnectionFailed(exception.message.toString())
        }
    }

    private fun leaveFromXMPPAndUpdateGroupDialog(dto: RemoteDialogDTO) {
        dto.id?.let { dialogId ->
            val roomJid = JIDHelper.INSTANCE.getRoomJidByDialogId(dialogId)
            val dialog = QBChatService.getInstance().groupChatManager.getGroupChat(roomJid)
            dialog.leave()
        }

        val requestBuilder = buildRequestLeaveFromGroupDialog()
        val dialog = RemoteDialogDTOMapper.toQBDialogFrom(dto)
        QBRestChatService.updateChatDialog(dialog, requestBuilder).perform()
    }

    private fun buildRequestLeaveFromGroupDialog(): QBDialogRequestBuilder {
        val requestBuilder = QBDialogRequestBuilder()
        requestBuilder.removeUsers(getLoggedUserId())

        return requestBuilder
    }

    private fun leaveFromPrivateDialog(dialogId: String?) {
        if (dialogId.isNullOrEmpty()) {
            throw exceptionFactory.makeIncorrectData("dialogId shouldn't be null or empty")
        }

        try {
            QBRestChatService.deleteDialog(dialogId, false).perform()
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        }
    }

    override fun getLoggedUserId(): Int {
        try {
            return getLoggedUserIdOrThrowException()
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeUnauthorised(exception.message.toString())
        }
    }

    override fun getUserSessionToken(): String {
        val token = QBSessionManager.getInstance().token
        if (token == null) {
            throw exceptionFactory.makeUnauthorised("The session token is null")
        }
        return token
    }

    private fun getLoggedUserIdOrThrowException(): Int {
        val loggedUserId = getLoggedUserIdFromSession()
        if (loggedUserId == null) {
            throw RuntimeException("The logged userId is null")
        }
        return loggedUserId
    }

    private fun getLoggedUserIdFromSession(): Int? {
        val userIdFromSession = QBSessionManager.getInstance().activeSession?.userId ?: 0
        if (userIdFromSession > 0) {
            return userIdFromSession
        }

        val userIdFromSessionParameters = QBSessionManager.getInstance().sessionParameters?.userId ?: 0
        if (userIdFromSessionParameters > 0) {
            return userIdFromSessionParameters
        }
        return null
    }

    override fun getUser(dto: RemoteUserDTO): RemoteUserDTO {
        try {
            val userId = RemoteUserDTOMapper.getUserIdFrom(dto)
            val user = userId?.let {
                QBUsers.getUser(it).perform()
            }
            return RemoteUserDTOMapper.toDTOFrom(user)
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getUsers(userIds: Collection<Int>): Collection<RemoteUserDTO> {
        try {

            val rule = makeRequestRuleToIncludeUserIds(userIds)

            val requestBuilder = QBPagedRequestBuilder()
            requestBuilder.rules = arrayListOf(rule)

            val pairResult = loadAllQBUsers(requestBuilder)

            val qbUsers = pairResult.first

            val dtoUsers = arrayListOf<RemoteUserDTO>()

            for (qbUser in qbUsers) {
                val userDTO = RemoteUserDTOMapper.toDTOFrom(qbUser)
                userDTO.avatarUrl = loadUserAvatarUrl(userDTO.blobId)

                dtoUsers.add(userDTO)
            }

            return dtoUsers
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    private fun makeRequestRuleToIncludeUserIds(userIds: Collection<Int>): GenericQueryRule {
        val paramFilter = "filter[]"

        val typeField = "number"
        val field = "id"
        val searchOperator = "in"

        var value = ""
        for (userId in userIds) {
            value += "$userId,"
        }

        val valueWithoutLastComma = value.substring(0, value.length - 1)

        return GenericQueryRule(paramFilter, "$typeField $field $searchOperator $valueWithoutLastComma")
    }

    override fun getAllUsers(dto: RemoteUserPaginationDTO): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>> {
        return channelFlow {
            val requestBuilder = RemotePaginationDTOMapper.pagedRequestBuilderFrom(dto)

            val rule = makeRequestRuleToExcludeUserId(getLoggedUserId())
            requestBuilder.rules = arrayListOf(rule)

            val pairResult = loadAllQBUsers(requestBuilder)
            val users = pairResult.first
            val pagination = pairResult.second

            for (user in users) {
                try {
                    val userDTO = RemoteUserDTOMapper.toDTOFrom(user)

                    userDTO.avatarUrl = loadUserAvatarUrl(userDTO.blobId)

                    send(Result.success(Pair(userDTO, pagination)))
                } catch (e: RemoteDataSourceException) {
                    send(Result.failure(e))
                } catch (exception: MappingException) {
                    send(Result.failure(exceptionFactory.makeIncorrectData(exception.message.toString() + " dialogId: ${user.id}")))
                }
            }
        }
    }

    override fun addUsersToDialog(dto: RemoteDialogDTO, userIds: Collection<Int>): RemoteDialogDTO {
        try {
            val qbDialog = RemoteDialogDTOMapper.toQBDialogFrom(dto)
            val requestBuilder = createAddUsersRequestBuilder(userIds)
            val updatedQbDialog = QBRestChatService.updateChatDialog(qbDialog, requestBuilder).perform()

            return RemoteDialogDTOMapper.toDTOFrom(updatedQbDialog, getLoggedUserId())
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: XMPPException) {
            throw exceptionFactory.makeConnectionFailed("dialogId: ${dto.id}")
        } catch (exception: SmackException) {
            throw exceptionFactory.makeConnectionFailed("dialogId: ${dto.id}")
        }
    }

    private fun createAddUsersRequestBuilder(userIds: Collection<Int>): QBDialogRequestBuilder {
        val requestBuilder = QBDialogRequestBuilder()
        for (userId in userIds) {
            requestBuilder.addRule("occupants_ids", "push_all", userId)
        }

        return requestBuilder
    }

    override fun removeUsersFromDialog(dto: RemoteDialogDTO, userIds: Collection<Int>): RemoteDialogDTO {
        try {
            val qbDialog = RemoteDialogDTOMapper.toQBDialogFrom(dto)
            val requestBuilder = createRemoveUsersRequestBuilder(userIds)
            val updatedQbDialog = QBRestChatService.updateChatDialog(qbDialog, requestBuilder).perform()

            return RemoteDialogDTOMapper.toDTOFrom(updatedQbDialog, getLoggedUserId())
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: XMPPException) {
            throw exceptionFactory.makeConnectionFailed("dialogId: ${dto.id}")
        } catch (exception: SmackException) {
            throw exceptionFactory.makeConnectionFailed("dialogId: ${dto.id}")
        }
    }

    private fun createRemoveUsersRequestBuilder(userIds: Collection<Int>): QBDialogRequestBuilder {
        val requestBuilder = QBDialogRequestBuilder()
        for (userId in userIds) {
            requestBuilder.addRule("occupants_ids", "pull_all", userId)
        }

        return requestBuilder
    }

    override fun getUsersByFilter(
        paginationDTO: RemoteUserPaginationDTO, filterDto: RemoteUserFilterDTO,
    ): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>> {
        return channelFlow {
            val requestBuilder = RemotePaginationDTOMapper.pagedRequestBuilderFrom(paginationDTO)

            val rule = makeRequestRuleToExcludeUserId(getLoggedUserId())
            requestBuilder.rules = arrayListOf(rule)

            val pairResult = loadQBUsersByName(requestBuilder, filterDto.name)
            val users = pairResult.first
            val pagination = pairResult.second

            run breakLoop@{
                for (user in users) {
                    if (user.id == getLoggedUserId()) {
                        return@breakLoop
                    }

                    try {
                        val userDTO = RemoteUserDTOMapper.toDTOFrom(user)

                        userDTO.avatarUrl = loadUserAvatarUrl(userDTO.blobId)

                        send(Result.success(Pair(userDTO, pagination)))
                    } catch (e: RemoteDataSourceException) {
                        send(Result.failure(e))
                    } catch (exception: MappingException) {
                        send(Result.failure(exceptionFactory.makeIncorrectData(exception.message.toString() + " dialogId: ${user.id}")))
                    }
                }
            }
        }
    }

    fun makeRequestRuleToExcludeUserId(userId: Int): GenericQueryRule {
        val paramFilter = "filter[]"

        val typeField = "number"
        val field = "id"
        val searchOperator = "ne"

        return GenericQueryRule(paramFilter, "$typeField $field $searchOperator $userId")
    }

    private fun loadAllQBUsers(pageRequestBuilder: QBPagedRequestBuilder): Pair<List<QBUser>, RemoteUserPaginationDTO> {
        try {
            val performer = QBUsers.getUsers(pageRequestBuilder) as Query
            val users = performer.perform()

            val bundle = performer.bundle
            val dto = RemotePaginationDTOMapper.remoteUserPaginationDtoFrom(bundle)

            return Pair(users, dto)
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
    }

    private fun loadQBUsersByName(
        pageRequestBuilder: QBPagedRequestBuilder, name: String?,
    ): Pair<List<QBUser>, RemoteUserPaginationDTO> {
        try {
            val performer = QBUsers.getUsersByFullName(name, pageRequestBuilder) as Query
            val users = performer.perform()

            val bundle = performer.bundle
            val paginationDTO = RemotePaginationDTOMapper.remoteUserPaginationDtoFrom(bundle)

            return Pair(users, paginationDTO)
        } catch (exception: QBResponseException) {
            val NOT_FOUND_USERS_CODE = 404
            if (exception.httpStatusCode == NOT_FOUND_USERS_CODE) {
                return Pair(emptyList(), RemoteUserPaginationDTO())
            }
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
    }

    private fun loadUserAvatarUrl(blobId: Int?): String? {
        blobId?.let { id ->
            try {
                val file = QBContent.getFile(id).perform()
                return file.publicUrl
            } catch (exception: QBResponseException) {
                Log.e(TAG, exception.message.toString())
            }
        }
        return null
    }

    private fun loadUserAvatarUid(blobId: Int?): String? {
        blobId?.let { id ->
            try {
                val file = QBContent.getFile(id).perform()
                return file.uid
            } catch (exception: QBResponseException) {
                Log.e(TAG, exception.message.toString())
            }
        }
        return null
    }

    override fun getAllMessages(
        messageDTO: RemoteMessageDTO,
        paginationDTO: RemoteMessagePaginationDTO,
    ): Flow<Result<Pair<RemoteMessageDTO?, RemoteMessagePaginationDTO>>> {
        return channelFlow {
            if (messageDTO.dialogId == null) {
                throw exceptionFactory.makeIncorrectData("The remoteMessageDTO contains null value for \"dialogId\" field")
            }

            val requestBuilder = RemotePaginationDTOMapper.getRequestBuilderFrom(paginationDTO)

            val pair = loadAllQBMessages(messageDTO.dialogId!!, requestBuilder)

            val messages = pair.first

            val messagePaginationDTO = RemotePaginationDTOMapper.remoteMessagePaginationDtoFrom(
                messages.size, paginationDTO.page, paginationDTO.perPage
            )

            if (messages.isEmpty()) {
                send((Result.success(Pair(null, messagePaginationDTO))))
            }
            for (message in messages) {
                try {
                    val loggedUserId = getLoggedUserId()
                    val resultMessageDTO = RemoteMessageDTOMapper.messageDTOFrom(message, loggedUserId)
                    send((Result.success(Pair(resultMessageDTO, messagePaginationDTO))))
                } catch (e: RemoteDataSourceException) {
                    send(Result.failure(e))
                } catch (exception: MappingException) {
                    send(Result.failure(exceptionFactory.makeIncorrectData(exception.message.toString() + " messageId: ${messageDTO.id}")))
                }
            }
        }.buffer(1)
    }

    private fun loadAllQBMessages(
        dialogId: String,
        requestBuilder: QBRequestGetBuilder,
    ): Pair<List<QBChatMessage>, Bundle> {
        val qbChatDialog = QBChatDialog()
        qbChatDialog.dialogId = dialogId

        try {
            val performer = QBRestChatService.getDialogMessages(qbChatDialog, requestBuilder) as Query
            val messages = performer.perform()

            val bundle = performer.bundle
            return Pair(messages, bundle)
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeRestrictedAccess(exception.message.toString())
        }
    }

    override fun startTyping(dialogDTO: RemoteDialogDTO) {
        try {
            val qbChat = getQBChatFromManager(dialogDTO)
            qbChat.sendIsTypingNotification()
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeConnectionFailed(exception.message.toString())
        } catch (exception: IllegalStateException) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun stopTyping(dialogDTO: RemoteDialogDTO) {
        try {
            val qbChat = getQBChatFromManager(dialogDTO)
            qbChat.sendStopTypingNotification()
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeConnectionFailed(exception.message.toString())
        } catch (exception: IllegalStateException) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun sendChatMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        try {
            val qbChatMessage = RemoteMessageDTOMapper.qbChatMessageFrom(messageDTO)
            val qbChat = getQBChatFromManager(dialogDTO)
            qbChat.sendMessage(qbChatMessage)

            // TODO: Added this logic for notifying UI when sending a message in private chat.
            //  The server does not return a message sent from itself.
            CoroutineScope(Dispatchers.IO).launch {
                val isPrivate = dialogDTO.type == 3
                if (isPrivate) {
                    dialogsEventFlow.emit(dialogDTO)
                }
            }
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeConnectionFailed(exception.message.toString())
        } catch (exception: IllegalStateException) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun sendEventMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        try {
            var qbChatMessage = RemoteMessageDTOMapper.qbSystemMessageFrom(messageDTO)

            when (messageDTO.type) {
                RemoteMessageDTO.MessageTypes.EVENT_CREATED_DIALOG -> {
                    qbChatMessage = EventMessageParser.addCreatedDialogPropertyTo(qbChatMessage)
                }
                RemoteMessageDTO.MessageTypes.EVENT_ADDED_USER -> {
                    qbChatMessage = EventMessageParser.addAddedUsersPropertyTo(qbChatMessage)
                }
                RemoteMessageDTO.MessageTypes.EVENT_LEFT_USER -> {
                    qbChatMessage = EventMessageParser.addLeftUsersPropertyTo(qbChatMessage)
                }
                RemoteMessageDTO.MessageTypes.EVENT_REMOVED_USER -> {
                    qbChatMessage = EventMessageParser.addRemovedUsersPropertyTo(qbChatMessage)
                }
                else -> {}
            }

            if (messageDTO.needSendChatMessage == true) {
                qbChatMessage.setSaveToHistory(true)

                val qbChat = getQBChatFromManager(dialogDTO)
                qbChat.sendMessage(qbChatMessage)
            }

            qbChatMessage.removeQBChatUnMarkedMessageExtension()
            qbChatMessage.setSaveToHistory(false)

            val participantIds = dialogDTO.participantIds ?: return
            for (recipientId in participantIds) {
                qbChatMessage.recipientId = recipientId
                QBChatService.getInstance().systemMessagesManager.sendSystemMessage(qbChatMessage)
            }
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeConnectionFailed(exception.message.toString())
        } catch (exception: IllegalStateException) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    private fun getQBChatFromManager(dialogDTO: RemoteDialogDTO): QBAbstractChat<*> {
        if (dialogDTO.type == null || dialogDTO.participantIds == null || dialogDTO.participantIds!!.isEmpty()) {
            throw RuntimeException("RemoteDialogDTO has wrong type of wrong participants")
        }

        val isPrivateDialog = dialogDTO.type == QBDialogType.PRIVATE.code
        if (isPrivateDialog) {
            val opponentId = getOpponentIdForPrivateDialog(dialogDTO)
            val chat = QBChatService.getInstance().privateChatManager.getChat(opponentId)
            return chat
        } else {
            val roomJid = JIDHelper.INSTANCE.getRoomJidByDialogId(dialogDTO.id)
            val chat = QBChatService.getInstance().groupChatManager.getGroupChat(roomJid)

            val isNeedJoin = !chat.isJoined
            if (isNeedJoin) {
                chat.join(DiscussionHistory())
            }

            return chat
        }
    }

    private fun getOpponentIdForPrivateDialog(dialogDTO: RemoteDialogDTO): Int {
        val participantSize = dialogDTO.participantIds?.size
        if (participantSize == null || participantSize > 2) {
            throw RuntimeException("The participants count for private dialog should be positive and no more then 2")
        }

        val participantIdsList = dialogDTO.participantIds?.toMutableList()

        val loggedUserId = getLoggedUserIdOrThrowException()
        participantIdsList?.remove(loggedUserId)

        if (participantIdsList?.size != 1) {
            throw RuntimeException("The participants count for private dialog has wrong value")
        }

        return participantIdsList.toList()[0]
    }

    override fun updateMessage(dto: RemoteMessageDTO) {
        try {
            val localMessage = RemoteMessageDTOMapper.qbChatMessageFrom(dto)
            val dialogId = dto.dialogId
            QBRestChatService.updateMessage(localMessage.id, dialogId, QBMessageUpdateBuilder()).perform()
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createMessage(dto: RemoteMessageDTO): RemoteMessageDTO {
        try {
            val qbChatMessage = RemoteMessageDTOMapper.qbChatMessageFrom(dto)
            val loggedUserId = getLoggedUserIdOrThrowException()
            val createdMessageDTO = RemoteMessageDTOMapper.messageDTOFrom(qbChatMessage, loggedUserId)

            return createdMessageDTO
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun readMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        try {
            val qbChatMessage = RemoteMessageDTOMapper.qbChatMessageFrom(messageDTO)
            val qbChat = getQBChatFromManager(dialogDTO)
            qbChat.readMessage(qbChatMessage)
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeConnectionFailed(exception.message.toString())
        } catch (exception: IllegalStateException) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deliverMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        try {
            val qbChatMessage = RemoteMessageDTOMapper.qbChatMessageFrom(messageDTO)
            val qbChat = getQBChatFromManager(dialogDTO)
            qbChat.deliverMessage(qbChatMessage)
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeConnectionFailed(exception.message.toString())
        } catch (exception: IllegalStateException) {
            throw exceptionFactory.makeUnexpected(exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        } catch (exception: RuntimeException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deleteMessage(dto: RemoteMessageDTO) {
        val forceDelete = true
        try {
            val localMessage = RemoteMessageDTOMapper.qbChatMessageFrom(dto)
            QBRestChatService.deleteMessage(localMessage.id, forceDelete).perform()
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
        try {
            val qbFile = QBContent.uploadFileTask(dto.file, false, null, null).perform()
            return RemoteFileDTOMapper.toDTOFrom(qbFile)
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getFile(dto: RemoteFileDTO): RemoteFileDTO {
        try {
            val fileId = dto.id
            if (fileId == null || fileId <= 0) {
                throw MappingException("The fileId should have positive value")
            }

            val file = QBContent.getFile(fileId).perform()
            return RemoteFileDTOMapper.toDTOFrom(file)
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deleteFile(dto: RemoteFileDTO) {
        try {
            val fileId = dto.id
            if (fileId == null || fileId <= 0) {
                throw MappingException("The fileId should have positive value")
            }

            QBContent.deleteFile(fileId).perform()
        } catch (exception: QBResponseException) {
            throw exceptionFactory.makeBy(exception.httpStatusCode, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            connectionFlow.emit(false)
        }

        removeMessageListeners()

        val notLoggedInChat = !QBChatService.getInstance().isLoggedIn
        if (notLoggedInChat) {
            throw exceptionFactory.makeUnexpected("You have already logout from chat")
        }

        try {
            QBChatService.getInstance().logout()
        } catch (exception: NotConnectedException) {
            throw exceptionFactory.makeUnauthorised(exception.message ?: buildUnauthorizedDefaultMessage())
        }
    }

    override fun connect() {
        initXMPPConnectionListener()

        val alreadyLogged = QBChatService.getInstance().isLoggedIn
        if (alreadyLogged) {
            CoroutineScope(Dispatchers.IO).launch {
                connectionFlow.emit(true)
            }
            addMessageListeners()
            throw exceptionFactory.makeUnexpected("You have already logged to chat")
        }

        val login = QBSessionManager.getInstance().sessionParameters?.userLogin
        val password = QBSessionManager.getInstance().sessionParameters?.userPassword
        val userId = getLoggedUserId()

        val isNotValidLogin = login.isNullOrEmpty()
        val isNotValidPassword = password.isNullOrEmpty()
        val isNotValidUserId = userId < 0

        if (isNotValidLogin || isNotValidPassword || isNotValidUserId) {
            throw exceptionFactory.makeIncorrectData("You should be logged to Quickblox before start UIKit")
        }

        val user = QBUser()
        user.id = userId
        user.login = login
        user.password = password

        try {
            QBChatService.getInstance().login(user)
            addMessageListeners()
        } catch (exception: XMPPException) {
            throw exceptionFactory.makeUnauthorised(exception.message ?: buildUnauthorizedDefaultMessage())
        } catch (exception: IOException) {
            throw exceptionFactory.makeUnauthorised(exception.message ?: buildUnauthorizedDefaultMessage())
        } catch (exception: SmackException) {
            throw exceptionFactory.makeUnauthorised(exception.message ?: buildUnauthorizedDefaultMessage())
        }
    }

    private fun removeMessageListeners() {
        QBChatService.getInstance().messageStatusesManager?.removeMessageStatusListener(statusMessageListener)
        QBChatService.getInstance().incomingMessagesManager?.removeDialogMessageListrener(chatMessageListener)
        QBChatService.getInstance().systemMessagesManager?.removeSystemMessageListener(systemMessageListener)
    }

    private fun addMessageListeners() {
        QBChatService.getInstance().messageStatusesManager?.addMessageStatusListener(statusMessageListener)
        QBChatService.getInstance().incomingMessagesManager?.addDialogMessageListener(chatMessageListener)
        QBChatService.getInstance().systemMessagesManager?.addSystemMessageListener(systemMessageListener)
    }

    private fun initXMPPConnectionListener() {
        QBChatService.getInstance().removeConnectionListener(xmppConnectionListener)
        QBChatService.getInstance().addConnectionListener(xmppConnectionListener)
    }

    private fun joinAllGroupDialogs() {
        if (QBChatService.getInstance().groupChatManager != null) {
            val groupChatManager = QBChatService.getInstance().groupChatManager
            val fields = groupChatManager.javaClass.declaredFields

            val dialogsField: Field = fields[1]
            dialogsField.isAccessible = true

            val dialogsFieldValue = dialogsField.get(groupChatManager)

            if (dialogsFieldValue is Map<*, *>) {
                val dialogs = dialogsFieldValue as Map<String, QBGroupChat>
                for ((jid, groupChat) in dialogs) {
                    joinGroupDialog(groupChat)
                }
            }
        }
    }

    private fun joinGroupDialog(groupChat: QBGroupChat) {
        // TODO: Need to add Exception handling
        try {
            groupChat.join(null)
        } catch (e: XMPPException) {
            // ignore
        } catch (e: SmackException) {
            // ignore
        } catch (e: IllegalStateException) {
            // ignore
        }
    }

    private inner class XMPPConnectionListener : AbstractConnectionListener() {
        override fun connected(connection: XMPPConnection?) {
            connection?.let {
                addTypingListeners(connection)
            }

            CoroutineScope(Dispatchers.IO).launch {
                connectionFlow.emit(true)
            }
        }

        private fun addTypingListeners(connection: XMPPConnection) {
            connection.addSyncStanzaListener(TypingListener(typingEventFlow, "PRIVATE"), MessageTypeFilter.CHAT)
            connection.addSyncStanzaListener(TypingListener(typingEventFlow, "GROUP"), MessageTypeFilter.GROUPCHAT)
        }

        override fun connectionClosed() {
            CoroutineScope(Dispatchers.IO).launch {
                connectionFlow.emit(false)
            }
        }

        override fun reconnectionSuccessful() {
            joinAllGroupDialogs()
        }

        override fun reconnectionFailed(p0: Exception?) {
            CoroutineScope(Dispatchers.IO).launch {
                connectionFlow.emit(false)
            }
        }
    }

    private fun buildUnauthorizedDefaultMessage(): String {
        return RemoteDataSourceException.Codes.UNAUTHORISED.toString()
    }

    override fun subscribeConnection(): Flow<Boolean> {
        return connectionFlow
    }

    override fun subscribeDialogsEvent(): Flow<RemoteDialogDTO?> {
        return dialogsEventFlow
    }

    override fun subscribeMessagesEvent(): Flow<RemoteMessageDTO?> {
        return messagesEventFlow
    }

    override fun subscribeTypingEvent(): Flow<RemoteTypingDTO?> {
        return typingEventFlow
    }
}