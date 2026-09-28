package com.campsc.workbuddy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO = "Data Access Object": todas as operações sobre a base de dados.
 *
 * Só escreves a interface; o Room gera a implementação quando compilas
 * (e verifica o SQL! Se tiveres um erro numa query, o build falha).
 *
 * - Funções que devolvem Flow são "vivas": sempre que a tabela muda, o Flow
 *   emite a lista nova e o ecrã atualiza-se sozinho.
 * - Funções "suspend" fazem uma operação e terminam. Correm numa corrotina,
 *   fora da thread principal, para não bloquear a interface.
 */
@Dao
interface WorkoutDao {

    // ---------- Planos ----------

    // LEFT JOIN para incluir também planos que ainda não têm dias (dayCount = 0).
    @Query(
        """
        SELECT plans.id, plans.name, COUNT(plan_days.id) AS dayCount
        FROM plans
        LEFT JOIN plan_days ON plan_days.planId = plans.id
        GROUP BY plans.id
        ORDER BY plans.name COLLATE NOCASE
        """
    )
    fun observePlanSummaries(): Flow<List<PlanSummary>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    fun observePlan(planId: Long): Flow<Plan?>

    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getPlan(planId: Long): Plan?

    /** Devolve o id gerado para o novo plano. */
    @Insert
    suspend fun insertPlan(plan: Plan): Long

    @Update
    suspend fun updatePlan(plan: Plan)

    @Query("DELETE FROM plans WHERE id = :planId")
    suspend fun deletePlan(planId: Long)

    // ---------- Dias ----------

    // @Transaction porque o Room faz 2 consultas (dias + exercícios) e queremos
    // que sejam consistentes entre si.
    @Transaction
    @Query("SELECT * FROM plan_days WHERE planId = :planId ORDER BY position")
    fun observeDaysWithExercises(planId: Long): Flow<List<DayWithExercises>>

    @Query("SELECT * FROM plan_days WHERE id = :dayId")
    fun observeDay(dayId: Long): Flow<PlanDay?>

    @Query("SELECT * FROM plan_days WHERE id = :dayId")
    suspend fun getDay(dayId: Long): PlanDay?

    /** A posição a seguir à última (0 se o plano ainda não tiver dias). */
    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM plan_days WHERE planId = :planId")
    suspend fun nextDayPosition(planId: Long): Int

    @Insert
    suspend fun insertDay(day: PlanDay): Long

    @Insert
    suspend fun insertDays(days: List<PlanDay>)

    @Update
    suspend fun updateDay(day: PlanDay)

    @Query("DELETE FROM plan_days WHERE id = :dayId")
    suspend fun deleteDay(dayId: Long)

    // ---------- Exercícios ----------

    @Query("SELECT * FROM exercises WHERE dayId = :dayId ORDER BY position")
    fun observeExercises(dayId: Long): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE dayId = :dayId ORDER BY position")
    suspend fun getExercises(dayId: Long): List<Exercise>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM exercises WHERE dayId = :dayId")
    suspend fun nextExercisePosition(dayId: Long): Int

    @Insert
    suspend fun insertExercise(exercise: Exercise): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    // ---------- Sessões de treino ----------

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSetLogs(setLogs: List<SetLog>)

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun observeSession(sessionId: Long): Flow<WorkoutSession?>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY exercisePosition, setNumber")
    fun observeSetLogs(sessionId: Long): Flow<List<SetLog>>

    @Query("UPDATE set_logs SET reps = :reps WHERE id = :setLogId")
    suspend fun updateReps(setLogId: Long, reps: Int?)

    @Query("UPDATE set_logs SET weightKg = :weightKg WHERE id = :setLogId")
    suspend fun updateWeight(setLogId: Long, weightKg: Double?)

    // COUNT(coluna) só conta as linhas em que a coluna NÃO é null.
    // Por isso COUNT(set_logs.reps) = número de séries já registadas.
    @Query(
        """
        SELECT sessions.id, sessions.planName, sessions.dayName, sessions.startedAt,
               COUNT(set_logs.id) AS totalSets,
               COUNT(set_logs.reps) AS doneSets
        FROM sessions
        LEFT JOIN set_logs ON set_logs.sessionId = sessions.id
        GROUP BY sessions.id
        ORDER BY sessions.startedAt DESC
        """
    )
    fun observeSessionSummaries(): Flow<List<SessionSummary>>

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
