package com.savvasdalkitsis.librephotos.photos.mvflow

import com.google.android.gms.maps.model.LatLng

sealed class PhotoEffect {
    data class LaunchMap(val gps: LatLng) : PhotoEffect()
    object HideSystemBars : PhotoEffect()
    object ShowSystemBars : PhotoEffect()
    object NavigateBack : PhotoEffect()
}