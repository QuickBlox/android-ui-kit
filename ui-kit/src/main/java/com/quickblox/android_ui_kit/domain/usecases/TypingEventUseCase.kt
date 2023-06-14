/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.TypingEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.TypingEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*

class TypingEventUseCase(private val dialogEntity: DialogEntity) : FlowUseCase<TypingEntity?>() {
    var timerLength = 10000L

    private var eventsRepository = QuickBloxUiKit.getDependency().getEventsRepository()
    private var usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    private var scope = CoroutineScope(Dispatchers.IO)
    private val typingEventFlow = MutableSharedFlow<TypingEntity?>(0)

    private val _typingTimers = hashMapOf<UserEntity, Timer>()
    val typingTimers: HashMap<UserEntity, Timer>
        get() = _typingTimers

    override suspend fun execute(): MutableSharedFlow<TypingEntity?> {
        if (isScopeNotActive(scope)) {
            scope = CoroutineScope(Dispatchers.IO)
        }

        if (dialogEntity.getParticipantIds()?.isEmpty() == true) {
            throw DomainException("The participants shouldn't be null or empty")
        }

        scope.launch {
            val allUsersFromDialog = loadUsers(dialogEntity.getParticipantIds()!!)

            val typingEntity = TypingEntityImpl()

            eventsRepository.subscribeTypingEvents().collect { result ->
                val senderId = result.first
                val typingType = result.second

                val isUserNotExistInDialogUsers = getUserBySenderId(senderId, allUsersFromDialog) == null
                if (isUserNotExistInDialogUsers || typingType == null) {
                    return@collect
                }

                val user = getUserBySenderId(senderId!!, allUsersFromDialog)!!

                when (typingType) {
                    TypingEntity.TypingTypes.STARTED -> {
                        startTypingTimer(user, _typingTimers, buildTimerFinishCallback(typingEntity))
                        typingEntity.setText(makeTypingTextFromAll(typingTimers))
                    }
                    TypingEntity.TypingTypes.STOPPED -> {
                        stopTypingTimer(user, _typingTimers)
                        typingEntity.setText(makeTypingTextFromAll(typingTimers))
                    }
                }

                typingEventFlow.emit(typingEntity)
            }
        }
        return typingEventFlow
    }

    private fun buildTimerFinishCallback(typingEntity: TypingEntity): () -> Unit {
        return {
            scope.launch {
                typingEntity.setText(makeTypingTextFromAll(typingTimers))
                typingEventFlow.emit(typingEntity)
            }
        }
    }

    private fun loadUsers(userIds: Collection<Int>): Collection<UserEntity> {
        return usersRepository.getUsersFromRemote(userIds)
    }

    @VisibleForTesting
    fun getUserBySenderId(userId: Int?, users: Collection<UserEntity>): UserEntity? {
        val foundUser = users.find { userEntity ->
            userEntity.getUserId() == userId
        }
        return foundUser
    }

    @VisibleForTesting
    fun startTypingTimer(user: UserEntity, typingTimers: HashMap<UserEntity, Timer>, finishCallback: () -> Unit = {}) {
        if (typingTimers.containsKey(user)) {
            typingTimers[user]?.cancel()
            typingTimers.remove(user)
        }

        buildAndStartTypingTimer(user, typingTimers, finishCallback)
    }

    private fun buildAndStartTypingTimer(
        user: UserEntity,
        typingTimers: HashMap<UserEntity, Timer>,
        finishCallback: () -> Unit,
        time: Long = timerLength
    ) {
        val timer = Timer()

        val timerTask = object : TimerTask() {
            override fun run() {
                typingTimers.remove(user)
                timer.cancel()

                finishCallback.invoke()
            }
        }

        timer.schedule(timerTask, time)

        typingTimers[user] = timer
    }

    @VisibleForTesting
    fun stopTypingTimer(user: UserEntity, typingTimers: HashMap<UserEntity, Timer>) {
        if (typingTimers.containsKey(user)) {
            val timer = typingTimers[user]
            timer?.cancel()

            typingTimers.remove(user)
        }
    }

    private fun makeTypingTextFromAll(typingTimers: HashMap<UserEntity, Timer>): String {
        val allTypingNames = getAllTypingNamesFrom(typingTimers)
        val typingText = makeTypingTextFrom(allTypingNames)
        return typingText
    }

    @VisibleForTesting
    fun getAllTypingNamesFrom(typingTimers: HashMap<UserEntity, Timer>): String {
        var typingNames = ""

        typingTimers.forEach { (user) ->
            val userName = getUserNameFrom(user)
            typingNames += "$userName|"
        }

        return typingNames
    }

    @VisibleForTesting
    fun makeTypingTextFrom(typingNames: String): String {
        val names = parseTypingNamesFrom(typingNames)

        if (names.isEmpty()) {
            return ""
        }

        val firstName = (names as MutableList)[0]

        if (names.size == 1) {
            return "$firstName typing..."
        }

        val countOfTypingUsers = names.size - 1

        val modifiedNames = "$firstName and $countOfTypingUsers others are typing..."
        return modifiedNames
    }

    @VisibleForTesting
    fun parseTypingNamesFrom(typingNames: String): Collection<String> {
        val parsedTypingNames = mutableListOf<String>()

        val names = typingNames.split("|")
        names.forEach { name ->
            if (name.trim().isNotEmpty()) {
                parsedTypingNames.add(name)
            }
        }

        return parsedTypingNames
    }

    @VisibleForTesting
    fun getUserNameFrom(user: UserEntity): String {
        val nameExist = !user.getName().isNullOrBlank()
        if (nameExist) {
            return user.getName()!!
        }

        val loginExist = !user.getLogin().isNullOrBlank()
        if (loginExist) {
            return user.getLogin()!!
        }

        return "User without name"
    }

    @ExcludeFromCoverage
    override suspend fun release() {
        scope.cancel()
    }
}