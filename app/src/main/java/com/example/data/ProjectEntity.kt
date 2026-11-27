package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val artboardWidth: Int,
    val artboardHeight: Int,
    val documentJson: String,
    val thumbnailPath: String?,
    val createdAt: Long,
    val updatedAt: Long
)
