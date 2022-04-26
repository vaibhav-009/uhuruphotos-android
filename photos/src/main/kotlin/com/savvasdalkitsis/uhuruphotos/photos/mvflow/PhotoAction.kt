package com.savvasdalkitsis.uhuruphotos.photos.mvflow

import com.google.android.gms.maps.model.LatLng

sealed class PhotoAction {
    object ToggleUI : PhotoAction()
    object NavigateBack : PhotoAction()
    object Refresh : PhotoAction()
    object DismissErrorMessage : PhotoAction()
    object ShowInfo : PhotoAction()
    object HideInfo : PhotoAction()
    object AskForPhotoDeletion : PhotoAction()
    object DismissPhotoDeletionDialog : PhotoAction()
    object DeletePhoto : PhotoAction()
    object SharePhoto : PhotoAction()
    object FullImageLoaded : PhotoAction()
    data class ClickedOnMap(val gps: LatLng) : PhotoAction()
    data class LoadPhoto(val id: String, val isVideo: Boolean) : PhotoAction()
    data class SetFavourite(val favourite: Boolean) : PhotoAction()
    data class ClickedOnGps(val gps: LatLng) : PhotoAction()
}