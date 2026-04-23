package com.adrianobispo

import com.adrianobispo.shared.TasksTable
import com.adrianobispo.shared.UsersTable
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Resets the database schema by dropping and recreating all tables.
 *
 * This function is intended for use in test scenarios where a clean database state
 * is required. It performs the following operations:
 * 1. Drops existing TasksTable and UsersTable
 * 2. Creates fresh instances of UsersTable and TasksTable
 *
 * The operations are executed within a database transaction.
 */
fun resetDatabaseSchema() {
    transaction {
        SchemaUtils.drop(TasksTable, UsersTable)
        SchemaUtils.create(UsersTable, TasksTable)
    }
}

