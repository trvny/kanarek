package com.kanarek.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kanarek.R

@Composable
internal fun ReaderSourcePicker(
    sources: List<String>,
    selectedSources: Set<String>,
    favoriteSources: Set<String>,
    onSelectSource: (String) -> Unit,
    onClearSources: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (sources.isEmpty()) return

    val sortedSources =
        remember(sources, favoriteSources) {
            sources.sortedWith(
                compareByDescending<String> { source ->
                    favoriteSources.any { it.equals(source, ignoreCase = true) }
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it },
            )
        }
    val selectedSource =
        sources.firstOrNull { source ->
            selectedSources.any { it.equals(source, ignoreCase = true) }
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "source-picker:all") {
                FilterChip(
                    selected = selectedSources.isEmpty(),
                    onClick = onClearSources,
                    label = { Text(stringResource(R.string.filter_all_sources)) },
                )
            }
            items(sortedSources, key = { "source-picker:source:$it" }) { source ->
                val selected = selectedSources.any { it.equals(source, ignoreCase = true) }
                FilterChip(
                    selected = selected,
                    onClick = { onSelectSource(source) },
                    label = {
                        Text(
                            text = source,
                            modifier = Modifier.widthIn(max = 220.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
        selectedSource?.let { source ->
            val favorite = favoriteSources.any { it.equals(source, ignoreCase = true) }
            IconButton(onClick = { onToggleFavorite(source) }) {
                Icon(
                    if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription =
                        stringResource(
                            if (favorite) {
                                R.string.remove_source_favorite
                            } else {
                                R.string.add_source_favorite
                            },
                        ),
                )
            }
        }
    }
}
