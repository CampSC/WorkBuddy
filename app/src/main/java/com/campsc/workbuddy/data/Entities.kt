package com.campsc.workbuddy.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * Cada classe anotada com @Entity é uma TABELA da base de dados (pensa numa
 * folha de Excel): cada propriedade é uma coluna e cada objeto é uma linha.
 *
 * Como as tabelas se ligam:
 *
 *   Plan 1───* PlanDay 1───* Exercise          <- o PLANO (o que devias fazer)
 *                 │
 *                 1
 *                 │
 *                 * WorkoutSession 1───* SetLog <- o REGISTO (o que fizeste)
 *
 * "1───*" lê-se "um para muitos": um plano tem vários dias, um dia tem vários
 * exercícios, etc. A ligação é feita com chaves estrangeiras (ForeignKey).
 */

/** Um plano de treino, p.ex. "Push/Pull/Legs" ou "Full body 3x". */
@Entity(tableName = "plans")
data class Plan(
    // autoGenerate: o SQLite escolhe o id quando inserimos (0 = "ainda não tem id").
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

/** Um dia de treino dentro de um plano, p.ex. "Dia 1 - Peito e tríceps". */
@Entity(
    tableName = "plan_days",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            // Se o plano for apagado, os dias dele também são.
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("planId")],
)
data class PlanDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val name: String,
    /** Ordem do dia dentro do plano (0, 1, 2...). */
    val position: Int,
)

/** Um exercício planeado para um dia. */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = PlanDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dayId")],
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    /** Peso em kg; null = sem carga (peso corporal). */
    val weightKg: Double?,
    val notes: String = "",
    /** Ordem do exercício dentro do dia. */
    val position: Int,
)

/**
 * Uma sessão de treino: um dia concreto em que foste ao ginásio.
 *
 * Guardamos uma CÓPIA dos nomes (planName, dayName) em vez de depender só do
 * dayId. Assim, se mais tarde mudares ou apagares o plano, o histórico mantém-se.
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PlanDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            // Se o dia for apagado, a sessão fica, mas sem ligação ao dia.
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("dayId")],
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long?,
    val planName: String,
    val dayName: String,
    /** Data/hora de início, em milissegundos desde 1/1/1970 (formato "epoch"). */
    val startedAt: Long,
)

/** Uma série de um exercício, registada durante uma sessão. */
@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("sessionId"), Index("exerciseId")],
)
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long?,
    // Cópias do plano no momento em que o treino começou (ver WorkoutSession).
    val exerciseName: String,
    val exercisePosition: Int,
    /** Número da série, a começar em 1. */
    val setNumber: Int,
    val targetReps: Int,
    /** Repetições que fizeste; null = ainda não registaste. */
    val reps: Int?,
    val weightKg: Double?,
)

/*
 * As classes abaixo NÃO são tabelas: são o "formato" do resultado de algumas
 * consultas (queries) do WorkoutDao.
 */

/** Um plano com o número de dias que tem (para a lista de planos). */
data class PlanSummary(
    val id: Long,
    val name: String,
    val dayCount: Int,
)

/**
 * Um dia com a lista dos seus exercícios. O @Relation diz ao Room para ir
 * buscar à tabela exercises as linhas cujo dayId é igual ao id do dia.
 * Atenção: o Room não garante a ordem dos exercícios; ordena por position.
 */
data class DayWithExercises(
    @Embedded val day: PlanDay,
    @Relation(parentColumn = "id", entityColumn = "dayId")
    val exercises: List<Exercise>,
)

/** Uma linha do histórico de treinos. */
data class SessionSummary(
    val id: Long,
    val planName: String,
    val dayName: String,
    val startedAt: Long,
    val totalSets: Int,
    val doneSets: Int,
)
