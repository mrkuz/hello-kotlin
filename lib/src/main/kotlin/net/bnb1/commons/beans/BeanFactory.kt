package net.bnb1.commons.beans

/**
 * Used by [BeanContainer] to create beans.
 */
typealias BeanFactory<T> = suspend () -> T
