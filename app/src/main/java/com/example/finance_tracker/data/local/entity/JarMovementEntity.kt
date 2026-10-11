package com.example.finance_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.finance_tracker.domain.model.JarMovementType

/**
 * A deposit into or withdrawal from a savings jar.
 * Movements are removed together with their jar (CASCADE).
 */
@Entity(
    tableName = "jar_movements",
    foreignKeys = [
        ForeignKey(
            entity = SavingsJarEntity::class,
            parentColumns = ["id"],
            childColumns = ["jarId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jarId"])]
)
data class JarMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val jarId: Long,
    val amount: Double,
    val type: JarMovementType,
    val date: Long,
    val note: String? = null
)
