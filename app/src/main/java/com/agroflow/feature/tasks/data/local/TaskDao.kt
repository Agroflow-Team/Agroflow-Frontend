package com.agroflow.feature.tasks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE trabajadorId = :trabajadorId")
    fun getTasksByWorker(trabajadorId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: TaskEntity)

    @Query("UPDATE tasks SET estado = :nuevoEstado, horasReales = :nuevasHoras, novedades = :novedades, isSyncPending = 1 WHERE id = :taskId")
    fun updateProgress(taskId: String, nuevoEstado: String, nuevasHoras: Double, novedades: String)

    @Query("SELECT * FROM tasks WHERE isSyncPending = 1")
    fun getPendingSyncTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isSyncPending = 0 WHERE id = :taskId")
    fun markAsSynced(taskId: String)
}
