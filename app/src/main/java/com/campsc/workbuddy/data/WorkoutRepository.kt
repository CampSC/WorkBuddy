package com.campsc.workbuddy.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

/**
 * O repositório é a "porta de entrada" para os dados. Os ViewModels falam com
 * ele e nunca diretamente com o DAO.
 *
 * A maior parte das funções só passa o pedido ao DAO. As interessantes são as
 * que juntam vários passos numa transação (createPlan, startSession): ou
 * correm todos, ou nenhum.
 */
class WorkoutRepository(private val database: WorkBuddyDatabase) {

    private val dao = database.workoutDao()

    // ---------- Planos ----------

    fun observePlans(): Flow<List<PlanSummary>> = dao.observePlanSummaries()

    fun observePlan(planId: Long): Flow<Plan?> = dao.observePlan(planId)

    /** Cria um plano já com [numberOfDays] dias ("Dia 1", "Dia 2", ...). Devolve o id do plano. */
    suspend fun createPlan(name: String, numberOfDays: Int): Long = database.withTransaction {
        val planId = dao.insertPlan(Plan(name = name))
        val days = (1..numberOfDays).map { number ->
            PlanDay(planId = planId, name = "Dia $number", position = number - 1)
        }
        dao.insertDays(days)
        planId
    }

    suspend fun renamePlan(plan: Plan, newName: String) {
        dao.updatePlan(plan.copy(name = newName))
    }

    suspend fun deletePlan(planId: Long) {
        dao.deletePlan(planId)
    }

    // ---------- Dias ----------

    fun observeDaysWithExercises(planId: Long): Flow<List<DayWithExercises>> =
        dao.observeDaysWithExercises(planId)

    fun observeDay(dayId: Long): Flow<PlanDay?> = dao.observeDay(dayId)

    suspend fun addDay(planId: Long, name: String) {
        database.withTransaction {
            val position = dao.nextDayPosition(planId)
            dao.insertDay(PlanDay(planId = planId, name = name, position = position))
        }
    }

    suspend fun renameDay(day: PlanDay, newName: String) {
        dao.updateDay(day.copy(name = newName))
    }

    suspend fun deleteDay(dayId: Long) {
        dao.deleteDay(dayId)
    }

    // ---------- Exercícios ----------

    fun observeExercises(dayId: Long): Flow<List<Exercise>> = dao.observeExercises(dayId)

    suspend fun addExercise(
        dayId: Long,
        name: String,
        sets: Int,
        reps: Int,
        weightKg: Double?,
        notes: String,
    ) {
        database.withTransaction {
            val position = dao.nextExercisePosition(dayId)
            dao.insertExercise(
                Exercise(
                    dayId = dayId,
                    name = name,
                    sets = sets,
                    reps = reps,
                    weightKg = weightKg,
                    notes = notes,
                    position = position,
                )
            )
        }
    }

    suspend fun updateExercise(exercise: Exercise) {
        dao.updateExercise(exercise)
    }

    suspend fun deleteExercise(exercise: Exercise) {
        dao.deleteExercise(exercise)
    }

    // ---------- Sessões de treino ----------

    /**
     * Começa um treino do dia [dayId]: cria a sessão e uma linha (SetLog) por
     * cada série de cada exercício, já com o peso planeado preenchido.
     * Devolve o id da sessão criada.
     */
    suspend fun startSession(dayId: Long): Long = database.withTransaction {
        val day = checkNotNull(dao.getDay(dayId)) { "Dia $dayId não existe" }
        val plan = checkNotNull(dao.getPlan(day.planId)) { "Plano ${day.planId} não existe" }
        val exercises = dao.getExercises(dayId)

        val sessionId = dao.insertSession(
            WorkoutSession(
                dayId = day.id,
                planName = plan.name,
                dayName = day.name,
                startedAt = System.currentTimeMillis(),
            )
        )

        val setLogs = exercises.flatMap { exercise ->
            (1..exercise.sets).map { setNumber ->
                SetLog(
                    sessionId = sessionId,
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    exercisePosition = exercise.position,
                    setNumber = setNumber,
                    targetReps = exercise.reps,
                    reps = null,
                    weightKg = exercise.weightKg,
                )
            }
        }
        dao.insertSetLogs(setLogs)
        sessionId
    }

    fun observeSession(sessionId: Long): Flow<WorkoutSession?> = dao.observeSession(sessionId)

    fun observeSetLogs(sessionId: Long): Flow<List<SetLog>> = dao.observeSetLogs(sessionId)

    suspend fun updateReps(setLogId: Long, reps: Int?) {
        dao.updateReps(setLogId, reps)
    }

    suspend fun updateWeight(setLogId: Long, weightKg: Double?) {
        dao.updateWeight(setLogId, weightKg)
    }

    fun observeHistory(): Flow<List<SessionSummary>> = dao.observeSessionSummaries()

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSession(sessionId)
    }
}
