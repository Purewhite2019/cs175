package com.purewhite.purewhitodo.db

import androidx.room.*

@Entity
data class TodoEntry(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "priority") var priority: Int,
    @ColumnInfo(name = "state") var state: Boolean
)

@Dao
interface TodoDao {
    @Query("SELECT * FROM todoentry")
    fun getAll() : List<TodoEntry>

    @Query("SELECT * FROM todoentry WHERE id IN (:Ids)")
    fun loadAllByIds(Ids: IntArray) : List<TodoEntry>

    @Insert
    fun insert(todoEntry: TodoEntry)

    @Update
    fun update(todoEntry: TodoEntry)

    @Delete
    fun delete(todoEntry: TodoEntry)
}

@Database(entities = [TodoEntry::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao() : TodoDao
}
