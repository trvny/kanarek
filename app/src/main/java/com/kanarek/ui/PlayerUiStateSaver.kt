package com.kanarek.ui

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import com.kanarek.data.Station
import com.kanarek.data.StationKind

internal val PlayerScreenUiStateSaver =
    Saver<PlayerScreenUiState, Bundle>(
        save = { state -> state.toSavedBundle() },
        restore = ::restorePlayerScreenUiState,
    )

internal fun PlayerScreenUiState.toSavedBundle(): Bundle =
    Bundle().apply {
        putString(KEY_FILTER, filter.name)
        putBoolean(KEY_ADD_DIALOG, addDialogVisible)
        putBoolean(KEY_DISCOVERY_DIALOG, discoveryDialogVisible)
        editingStation?.let { station ->
            putBoolean(KEY_HAS_EDITING_STATION, true)
            putString(KEY_STATION_ID, station.id)
            putString(KEY_STATION_NAME, station.name)
            putString(KEY_STATION_URL, station.streamUrl)
            putString(KEY_STATION_LOGO, station.logoUrl)
            putString(KEY_STATION_GROUP, station.groupTitle)
            putString(KEY_STATION_TVG_ID, station.tvgId)
            putString(KEY_STATION_USER_AGENT, station.userAgent)
            putString(KEY_STATION_REFERRER, station.referrer)
            putString(KEY_STATION_KIND, station.kind.name)
        }
    }

internal fun restorePlayerScreenUiState(saved: Bundle): PlayerScreenUiState =
    PlayerScreenUiState(
        filter = saved.enumValue(KEY_FILTER, StationFilter.RADIO),
        editingStation = saved.restoreEditingStation(),
        addDialogVisible = saved.getBoolean(KEY_ADD_DIALOG),
        discoveryDialogVisible = saved.getBoolean(KEY_DISCOVERY_DIALOG),
    )

private fun Bundle.restoreEditingStation(): Station? {
    if (!getBoolean(KEY_HAS_EDITING_STATION)) return null
    val id = getString(KEY_STATION_ID).orEmpty()
    val name = getString(KEY_STATION_NAME).orEmpty()
    val streamUrl = getString(KEY_STATION_URL).orEmpty()
    if (id.isBlank() || streamUrl.isBlank()) return null
    return Station(
        id = id,
        name = name,
        streamUrl = streamUrl,
        logoUrl = getString(KEY_STATION_LOGO),
        groupTitle = getString(KEY_STATION_GROUP),
        tvgId = getString(KEY_STATION_TVG_ID),
        userAgent = getString(KEY_STATION_USER_AGENT),
        referrer = getString(KEY_STATION_REFERRER),
        kind = enumValue(KEY_STATION_KIND, StationKind.UNKNOWN),
    )
}

private inline fun <reified T : Enum<T>> Bundle.enumValue(
    key: String,
    fallback: T,
): T =
    getString(key)
        ?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
        ?: fallback

private const val KEY_FILTER = "filter"
private const val KEY_ADD_DIALOG = "addDialog"
private const val KEY_DISCOVERY_DIALOG = "discoveryDialog"
private const val KEY_HAS_EDITING_STATION = "hasEditingStation"
private const val KEY_STATION_ID = "stationId"
private const val KEY_STATION_NAME = "stationName"
private const val KEY_STATION_URL = "stationUrl"
private const val KEY_STATION_LOGO = "stationLogo"
private const val KEY_STATION_GROUP = "stationGroup"
private const val KEY_STATION_TVG_ID = "stationTvgId"
private const val KEY_STATION_USER_AGENT = "stationUserAgent"
private const val KEY_STATION_REFERRER = "stationReferrer"
private const val KEY_STATION_KIND = "stationKind"
