package com.dropbox.mobile.hypershard

import java.io.File

/**
 * https://github.com/ftomassetti/javadoc-extractor/blob/master/src/main/java/me/tomassetti/javadocextractor/support/DirExplorer.java
 */
internal class DirExplorer(private val filter: Filter, private val fileHandler: FileHandler) {
    interface FileHandler {
        fun handle(level: Int, path: String, file: File)
    }

    interface Filter {
        fun interested(level: Int, path: String, file: File): Boolean
    }

    fun explore(root: File) {
        explore(0, "", root)
    }

    private fun explore(level: Int, path: String, file: File) {
        if (file.isDirectory) {
            for (child in file.listFiles()!!) {
                explore(level + 1, path + "/" + child.name, child)
            }
        } else {
            if (filter.interested(level, path, file)) {
                fileHandler.handle(level, path, file)
            }
        }
    }
}
