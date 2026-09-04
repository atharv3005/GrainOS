package com.example.data.model

/**
 * Synchronization lifecycle status for Room entities supporting offline-first and cloud replication.
 */
enum class SyncStatus {
    PENDING,
    SYNCED,
    CONFLICT,
    FAILED
}
