/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.zhanghai.android.files.compat.reversedCompat
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.NaturalOrderComparator
import java.util.Comparator

@Parcelize
data class FileSortOptions(
    val by: By,
    val order: Order,
    val isDirectoriesFirst: Boolean
) : Parcelable {
    fun createComparator(): Comparator<FileItem> {
        val namePrefixComparator = compareBy<String> { name ->
            NAME_UNIMPORTANT_PREFIXES.any { name.startsWith(it) }
        }
        var comparator = compareBy<FileItem, String>(
            namePrefixComparator.then(NaturalOrderComparator)
        ) { it.name }
        when (by) {
            // Nothing to do.
            By.NAME -> {}
            By.TYPE ->
                comparator = compareBy<FileItem, String>(String.CASE_INSENSITIVE_ORDER) {
                    it.extension
                }.then(comparator)
            By.SIZE -> comparator = compareBy<FileItem> { it.attributes.size() }.then(comparator)
            By.LAST_MODIFIED ->
                comparator = compareBy<FileItem> { it.attributes.lastModifiedTime() }
                    .then(comparator)
        }
        when (order) {
            Order.ASCENDING -> {}
            Order.DESCENDING -> comparator = comparator.reversedCompat()
        }
        if (isDirectoriesFirst) {
            val isDirectoryComparator = compareBy<FileItem> { it.attributes.isDirectory }
                .reversedCompat()
            comparator = isDirectoryComparator.then(comparator)
        }
        return comparator
    }

    companion object {
        // Same behavior as Nautilus.
        private val NAME_UNIMPORTANT_PREFIXES = listOf(".", "#")
    }

    enum class By {
        NAME,
        TYPE,
        SIZE,
        LAST_MODIFIED
    }

    enum class Order {
        ASCENDING,
        DESCENDING
    }
}
