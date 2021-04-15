package net.bnb1.commons.tasks

import kotlinx.coroutines.Runnable

/**
 * Defines an [action] to be first run after [delay] milliseconds
 * and then periodically every [period] milliseconds.
 */
data class TaskDefinition(val delay: Long, val period: Long, val action: Runnable)
