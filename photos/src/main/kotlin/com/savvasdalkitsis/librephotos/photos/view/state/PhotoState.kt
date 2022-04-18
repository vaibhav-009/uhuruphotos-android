package com.savvasdalkitsis.librephotos.photos.view.state

import androidx.compose.material.ModalBottomSheetValue
import com.google.android.gms.maps.model.LatLng

data class PhotoState(
    val id: String = "",
    val isLoading: Boolean = false,
    val fullResUrl: String = "",
    val lowResUrl: String = "",
    val errorMessage: String? = null,
    val showUI: Boolean = true,
    val showRefresh: Boolean = false,
    val showInfoButton: Boolean = false,
    val showPhotoDeletionConfirmationDialog: Boolean = false,
    val showShareIcon: Boolean = false,
    val isFavourite: Boolean? = null,
    val infoSheetState: ModalBottomSheetValue = ModalBottomSheetValue.Hidden,
    val dateAndTime: String = "",
    val location: String = "",
    val gps: LatLng? = null,
)