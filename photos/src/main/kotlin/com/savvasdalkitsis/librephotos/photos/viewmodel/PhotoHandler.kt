package com.savvasdalkitsis.librephotos.photos.viewmodel

import androidx.work.WorkInfo.State.*
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.*
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.DismissErrorMessage
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.HideInfo
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.NavigateBack
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.SharePhoto
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.ShowInfo
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoEffect
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoEffect.*
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoMutation
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoMutation.*
import com.savvasdalkitsis.librephotos.photos.usecase.PhotosUseCase
import com.savvasdalkitsis.librephotos.photos.view.state.PhotoState
import com.savvasdalkitsis.librephotos.photos.worker.PhotoDetailsRetrieveWorker
import com.savvasdalkitsis.librephotos.viewmodel.Handler
import com.savvasdalkitsis.librephotos.worker.usecase.WorkerStatusUseCase
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class PhotoHandler @Inject constructor(
    private val photosUseCase: PhotosUseCase,
    private val workerStatusUseCase: WorkerStatusUseCase,
) : Handler<PhotoState, PhotoEffect, PhotoAction, PhotoMutation> {

    override fun invoke(
        state: PhotoState,
        action: PhotoAction,
        effect: suspend (PhotoEffect) -> Unit
    ): Flow<PhotoMutation> = when(action) {
        is LoadPhoto -> flow {
            emit(with(photosUseCase) { ReceivedUrl(
                id = action.id,
                lowResUrl = action.id.toThumbnailUrlFromId(),
                fullResUrl = action.id.toFullSizeUrlFromId(),
            )})
            emitAll(merge(
                photosUseCase.getPhoto(action.id)
                    .map(::ReceivedDetails),
                workerStatusUseCase.monitorUniqueJobStatus(
                    PhotoDetailsRetrieveWorker.workName(action.id)
                ).map {
                    when (it) {
                        BLOCKED, CANCELLED, FAILED -> ShowErrorMessage("Error loading photo details")
                        SUCCEEDED -> FinishedLoading
                        ENQUEUED, RUNNING -> Loading
                    }
                }
            ))
        }
        ToggleUI -> flow {
            if (state.showUI) {
                emit(HideUI)
                effect(HideSystemBars)
            } else {
                emit(ShowUI)
                effect(ShowSystemBars)
            }
        }
        NavigateBack -> flow {
            emit(HideUI)
            effect(PhotoEffect.NavigateBack)
        }
        is SetFavourite -> flow {
            photosUseCase.setPhotoFavourite(state.id, action.favourite)
        }
        Refresh -> flow {
            photosUseCase.refreshDetails(state.id)
        }
        DismissErrorMessage -> flowOf(PhotoMutation.DismissErrorMessage)
        ShowInfo -> flowOf(PhotoMutation.ShowInfo)
        HideInfo -> flowOf(PhotoMutation.HideInfo)
        is ClickedOnMap -> flow {
            effect(LaunchMap(action.gps))
        }
        is ClickedOnGps -> flow {
            effect(CopyToClipboard(action.gps.toString()))
        }
        AskForPhotoDeletion -> flowOf(ShowDeletionConfirmationDialog)
        DismissPhotoDeletionDialog -> flowOf(HideDeletionConfirmationDialog)
        DeletePhoto -> flow {
            emit(Loading)
            emit(HideDeletionConfirmationDialog)
            photosUseCase.deletePhoto(state.id)
            effect(PhotoEffect.NavigateBack)
        }
        SharePhoto -> flow {
            effect(PhotoEffect.SharePhoto(state.fullResUrl))
        }
        FullImageLoaded -> flowOf(ShowShareIcon)
    }
}
