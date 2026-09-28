package com.campsc.workbuddy

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import com.campsc.workbuddy.data.WorkBuddyDatabase
import com.campsc.workbuddy.data.WorkoutRepository

/**
 * A classe Application é criada uma única vez, quando a app arranca, e vive
 * enquanto a app estiver aberta. É o sítio certo para criar objetos que
 * queremos partilhar por toda a app (como a base de dados).
 *
 * Está registada no AndroidManifest.xml (android:name=".WorkBuddyApplication").
 */
class WorkBuddyApplication : Application() {

    /** "by lazy": só é criado na primeira vez que alguém o usa. */
    val repository: WorkoutRepository by lazy {
        WorkoutRepository(WorkBuddyDatabase.create(this))
    }
}

/** Vai buscar a nossa Application a partir das CreationExtras de um ViewModel. */
fun CreationExtras.workBuddyApplication(): WorkBuddyApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as WorkBuddyApplication
