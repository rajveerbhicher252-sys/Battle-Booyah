package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    val userDao = db.userDao()
    val tournamentDao = db.tournamentDao()
    val participantDao = db.participantDao()
    val transactionDao = db.transactionDao()
    val depositDao = db.depositDao()
    val withdrawalDao = db.withdrawalDao()
    val notificationDao = db.notificationDao()

    suspend fun joinTournament(userId: Long, tournamentId: Long, inGameName: String): Result<String> {
        val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val tournament = tournamentDao.getTournamentById(tournamentId) ?: return Result.failure(Exception("Tournament not found"))

        if (inGameName.isBlank()) {
            return Result.failure(Exception("Please enter your Free Fire In-Game Name (IGN)!"))
        }

        if (user.walletBalance < tournament.entryFee) {
            return Result.failure(Exception("Insufficient balance! Please add money to wallet."))
        }

        val existingParticipant = participantDao.getParticipant(userId, tournamentId)
        if (existingParticipant != null) {
            return Result.failure(Exception("You have already joined this tournament!"))
        }

        // Deduct entry fee
        val newBalance = user.walletBalance - tournament.entryFee
        userDao.updateUser(user.copy(walletBalance = newBalance))

        // Record transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = tournament.entryFee,
                type = "debit",
                description = "Entry fee for ${tournament.title}"
            )
        )

        // Insert participant with Free Fire Name
        participantDao.insertParticipant(
            ParticipantEntity(
                userId = userId,
                tournamentId = tournamentId,
                inGameName = inGameName.trim(),
                result = "Participated"
            )
        )

        return Result.success("Successfully joined tournament with IGN: $inGameName!")
    }

    suspend fun createDepositRequest(userId: Long, amount: Double, transactionId: String): Result<String> {
        if (amount <= 0 || transactionId.isBlank()) {
            return Result.failure(Exception("Please enter valid amount and transaction ID."))
        }
        depositDao.insertDeposit(
            DepositEntity(
                userId = userId,
                amount = amount,
                transactionId = transactionId,
                status = "Pending"
            )
        )
        return Result.success("Deposit request submitted successfully! Awaiting admin approval.")
    }

    suspend fun approveDeposit(depositId: Long): Result<String> {
        val deposit = depositDao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        if (deposit.status != "Pending") return Result.failure(Exception("Deposit already processed"))

        val user = userDao.getUserById(deposit.userId) ?: return Result.failure(Exception("User not found"))
        
        // Update deposit status
        depositDao.updateDeposit(deposit.copy(status = "Completed"))

        // Credit user wallet
        val newBalance = user.walletBalance + deposit.amount
        userDao.updateUser(user.copy(walletBalance = newBalance))

        // Record transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = user.id,
                amount = deposit.amount,
                type = "credit",
                description = "Deposit approved (UPI TXN: ${deposit.transactionId})"
            )
        )

        return Result.success("Deposit approved and credited!")
    }

    suspend fun rejectDeposit(depositId: Long): Result<String> {
        val deposit = depositDao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        depositDao.updateDeposit(deposit.copy(status = "Rejected"))
        return Result.success("Deposit request rejected.")
    }

    suspend fun createWithdrawalRequest(userId: Long, amount: Double): Result<String> {
        val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        if (user.upiId.isNullOrBlank()) {
            return Result.failure(Exception("Please set your UPI ID in your profile first!"))
        }
        val settings = userDao.getAdminSettings()
        val minLimit = settings?.minWithdrawalLimit ?: 100.0
        if (amount < minLimit) {
            return Result.failure(Exception("Minimum withdrawal amount is ₹$minLimit"))
        }
        if (user.walletBalance < amount) {
            return Result.failure(Exception("Insufficient wallet balance!"))
        }

        // Deduct from wallet immediately upon request or escrow
        val newBalance = user.walletBalance - amount
        userDao.updateUser(user.copy(walletBalance = newBalance))

        withdrawalDao.insertWithdrawal(
            WithdrawalEntity(
                userId = userId,
                amount = amount,
                status = "Pending"
            )
        )

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = amount,
                type = "debit",
                description = "Withdrawal request submitted (Pending)"
            )
        )

        return Result.success("Withdrawal request submitted successfully!")
    }

    suspend fun completeWithdrawal(withdrawalId: Long): Result<String> {
        val w = withdrawalDao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("Withdrawal not found"))
        if (w.status != "Pending") return Result.failure(Exception("Withdrawal already processed"))

        withdrawalDao.updateWithdrawal(w.copy(status = "Completed"))
        return Result.success("Withdrawal marked as completed.")
    }

    suspend fun declareWinner(tournamentId: Long, winnerUserId: Long): Result<String> {
        val tournament = tournamentDao.getTournamentById(tournamentId) ?: return Result.failure(Exception("Tournament not found"))
        val winnerUser = userDao.getUserById(winnerUserId) ?: return Result.failure(Exception("Winner user not found"))

        // Update tournament status to Completed and set winner
        tournamentDao.updateTournament(tournament.copy(status = "Completed", winnerUserId = winnerUserId))

        // Update participant result
        val participant = participantDao.getParticipant(winnerUserId, tournamentId)
        if (participant != null) {
            participantDao.updateParticipant(participant.copy(result = "Winner"))
        }

        // Credit prize pool to winner's wallet
        val newBalance = winnerUser.walletBalance + tournament.prizePool
        userDao.updateUser(winnerUser.copy(walletBalance = newBalance))

        // Record transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = winnerUserId,
                amount = tournament.prizePool,
                type = "credit",
                description = "Won tournament prize: ${tournament.title}"
            )
        )

        return Result.success("Winner declared and prize pool ₹${tournament.prizePool} distributed successfully!")
    }
}
