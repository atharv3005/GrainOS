package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Robust database migration script to upgrade database schema to the new architectural standard.
 * Creates Party Master, Document Sequences, Audit Trails, Inventory Movements, and Payment Allocations tables,
 * and adds sync metadata columns.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create Parties Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `parties` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `party_type` TEXT NOT NULL,
                `legal_name` TEXT NOT NULL,
                `trade_name` TEXT NOT NULL,
                `mobile` TEXT NOT NULL,
                `alt_phone` TEXT NOT NULL,
                `email` TEXT NOT NULL,
                `village` TEXT NOT NULL,
                `taluka` TEXT NOT NULL,
                `district` TEXT NOT NULL,
                `state` TEXT NOT NULL,
                `pincode` TEXT NOT NULL,
                `pan` TEXT NOT NULL,
                `is_pan_verified` INTEGER NOT NULL,
                `gstin` TEXT NOT NULL,
                `bank_account_name` TEXT NOT NULL,
                `bank_account_number` TEXT NOT NULL,
                `bank_ifsc` TEXT NOT NULL,
                `bank_name` TEXT NOT NULL,
                `bank_branch` TEXT NOT NULL,
                `cumulative_purchases_in_fy` REAL NOT NULL,
                `running_balance` REAL NOT NULL,
                `is_active` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `sync_status` TEXT NOT NULL,
                `synced_at` INTEGER,
                `idempotency_key` TEXT NOT NULL,
                `device_id` TEXT NOT NULL,
                `organization_id` TEXT NOT NULL,
                `schema_version` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_parties_uuid` ON `parties` (`uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_party_type` ON `parties` (`party_type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_mobile` ON `parties` (`mobile`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_pan` ON `parties` (`pan`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_gstin` ON `parties` (`gstin`)")

        // 2. Create Document Sequences Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `document_sequences` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `financial_year` TEXT NOT NULL,
                `facility_id` TEXT NOT NULL,
                `document_type` TEXT NOT NULL,
                `series_code` TEXT NOT NULL,
                `current_sequence` INTEGER NOT NULL,
                `last_used_timestamp` INTEGER NOT NULL,
                `is_locked` INTEGER NOT NULL,
                `sync_status` TEXT NOT NULL,
                `synced_at` INTEGER,
                `idempotency_key` TEXT NOT NULL,
                `device_id` TEXT NOT NULL,
                `organization_id` TEXT NOT NULL,
                `schema_version` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_document_sequences_uuid` ON `document_sequences` (`uuid`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_document_sequences_financial_year_facility_id_document_type` ON `document_sequences` (`financial_year`, `facility_id`, `document_type`)")

        // 3. Create Audit Trails Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `audit_trails` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `entity_type` TEXT NOT NULL,
                `entity_id` TEXT NOT NULL,
                `action` TEXT NOT NULL,
                `previous_state_json` TEXT,
                `new_state_json` TEXT,
                `user_id` TEXT NOT NULL,
                `device_id` TEXT NOT NULL,
                `reason` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `sync_status` TEXT NOT NULL,
                `synced_at` INTEGER,
                `idempotency_key` TEXT NOT NULL,
                `organization_id` TEXT NOT NULL,
                `schema_version` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_audit_trails_uuid` ON `audit_trails` (`uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_trails_entity_type_entity_id` ON `audit_trails` (`entity_type`, `entity_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_trails_action` ON `audit_trails` (`action`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_trails_timestamp` ON `audit_trails` (`timestamp`)")

        // 4. Create Inventory Movements Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `inventory_movements` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `movement_type` TEXT NOT NULL,
                `source_entity_type` TEXT NOT NULL,
                `source_entity_uuid` TEXT NOT NULL,
                `facility_id` TEXT NOT NULL,
                `batch_id` TEXT NOT NULL,
                `crop_type` TEXT NOT NULL,
                `quantity_kg` REAL NOT NULL,
                `quantity_grams` INTEGER NOT NULL,
                `quantity_basis` TEXT NOT NULL,
                `cost_per_quintal_paise` INTEGER NOT NULL,
                `total_value_paise` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `user_id` TEXT NOT NULL,
                `device_id` TEXT NOT NULL,
                `reason` TEXT NOT NULL,
                `sync_status` TEXT NOT NULL,
                `synced_at` INTEGER,
                `idempotency_key` TEXT NOT NULL,
                `organization_id` TEXT NOT NULL,
                `schema_version` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_inventory_movements_uuid` ON `inventory_movements` (`uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_movements_facility_id` ON `inventory_movements` (`facility_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_movements_batch_id` ON `inventory_movements` (`batch_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_movements_movement_type` ON `inventory_movements` (`movement_type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_movements_timestamp` ON `inventory_movements` (`timestamp`)")

        // 5. Create Payment Allocations Table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `payment_allocations` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `uuid` TEXT NOT NULL,
                `payment_uuid` TEXT NOT NULL,
                `payable_uuid` TEXT NOT NULL,
                `allocated_amount_paise` INTEGER NOT NULL,
                `allocated_amount_rupees` REAL NOT NULL,
                `allocation_type` TEXT NOT NULL,
                `remaining_payable_balance_paise` INTEGER NOT NULL,
                `allocation_timestamp` INTEGER NOT NULL,
                `notes` TEXT NOT NULL,
                `sync_status` TEXT NOT NULL,
                `synced_at` INTEGER,
                `idempotency_key` TEXT NOT NULL,
                `device_id` TEXT NOT NULL,
                `organization_id` TEXT NOT NULL,
                `schema_version` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_payment_allocations_uuid` ON `payment_allocations` (`uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocations_payment_uuid` ON `payment_allocations` (`payment_uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocations_payable_uuid` ON `payment_allocations` (`payable_uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocations_allocation_timestamp` ON `payment_allocations` (`allocation_timestamp`)")
    }
}
