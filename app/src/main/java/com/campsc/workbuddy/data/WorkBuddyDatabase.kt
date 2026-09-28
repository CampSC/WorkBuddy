package com.campsc.workbuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * A base de dados da app (um ficheiro SQLite chamado "workbuddy.db" no telemóvel).
 *
 * IMPORTANTE: se mudares alguma @Entity (p.ex. adicionar uma coluna), tens de
 * subir o "version" e dizer ao Room como migrar os dados antigos.
 * Vê o desafio sobre migrações em EXERCICIOS.md.
 */
@Database(
    entities = [
        Plan::class,
        PlanDay::class,
        Exercise::class,
        WorkoutSession::class,
        SetLog::class,
    ],
    version = 1,
)
abstract class WorkBuddyDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        fun create(context: Context): WorkBuddyDatabase =
            Room.databaseBuilder(context, WorkBuddyDatabase::class.java, "workbuddy.db")
                .build()
    }
}
