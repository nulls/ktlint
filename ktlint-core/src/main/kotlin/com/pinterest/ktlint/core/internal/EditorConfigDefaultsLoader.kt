package com.pinterest.ktlint.core.internal

import com.pinterest.ktlint.core.api.EditorConfigDefaults
import com.pinterest.ktlint.core.api.EditorConfigDefaults.Companion.EMPTY_EDITOR_CONFIG_DEFAULTS
import com.pinterest.ktlint.core.initKtLintKLogger
import com.pinterest.ktlint.core.internal.ThreadSafeEditorConfigCache.Companion.THREAD_SAFER_EDITOR_CONFIG_CACHE
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import mu.KotlinLogging
import org.ec4j.core.EditorConfigLoader
import org.ec4j.core.Resource
import org.ec4j.core.model.Version

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * Load all properties from an ".editorconfig" file without filtering on a glob.
 */
internal class EditorConfigDefaultsLoader {
    private val editorConfigLoader: EditorConfigLoader = EditorConfigLoader.of(Version.CURRENT)

    /**
     * Loads properties from [path]. [path] may either locate a file (also allows specifying a file with a name other
     * than ".editorconfig") or a directory in which a file with name ".editorconfig" is expected to exist. Properties
     * from all globs are returned.
     *
     * If [path] is not valid then the [EMPTY_EDITOR_CONFIG_DEFAULTS] is returned.
     *
     * The property "root" which denotes whether the parent directory is to be checked for the existence of a fallback
     * ".editorconfig" is ignored entirely.
     */
    fun load(path: Path?): EditorConfigDefaults {
        if (path == null || path.pathString.isBlank()) {
            return EMPTY_EDITOR_CONFIG_DEFAULTS
        }

        val editorConfigFilePath = path.editorConfigFilePath()
        if (editorConfigFilePath.notExists()) {
            LOGGER.warn { "File or directory '$path' is not found. Can not load '.editorconfig' properties" }
            return EMPTY_EDITOR_CONFIG_DEFAULTS
        }

        return THREAD_SAFER_EDITOR_CONFIG_CACHE
            .get(editorConfigFilePath.resource(), editorConfigLoader)
            .also {
                LOGGER.trace {
                    it
                        .toString()
                        .split("\n")
                        .joinToString(
                            prefix = "Loaded .editorconfig-properties from file '$editorConfigFilePath':\n\t",
                            separator = "\n\t",
                        )
                }
            }.let { EditorConfigDefaults(it) }
    }

    private fun Path.editorConfigFilePath() =
        if (isDirectory()) {
            pathString
                .plus(
                    fileSystem.separator.plus(".editorconfig"),
                ).let { path -> fileSystem.getPath(path) }
        } else {
            this
        }

    private fun Path.resource() =
        Resource.Resources.ofPath(this, StandardCharsets.UTF_8)
}
