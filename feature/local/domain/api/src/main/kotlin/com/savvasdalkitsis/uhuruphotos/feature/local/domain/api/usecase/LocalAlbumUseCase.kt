/*
Copyright 2022 Savvas Dalkitsis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.savvasdalkitsis.uhuruphotos.feature.local.domain.api.usecase

import com.savvasdalkitsis.uhuruphotos.api.albums.model.Album
import com.savvasdalkitsis.uhuruphotos.feature.collage.view.api.ui.state.PredefinedCollageDisplay
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.LocalMediaFolder
import kotlinx.coroutines.flow.Flow

interface LocalAlbumUseCase {

    fun observeLocalAlbum(albumId: Int): Flow<Pair<LocalMediaFolder, List<Album>>>
    suspend fun refreshLocalAlbum(albumId: Int): Result<Unit>
    fun getLocalAlbumGalleryDisplay(albumId: Int): PredefinedCollageDisplay
    suspend fun setLocalAlbumGalleryDisplay(albumId: Int, galleryDisplay: PredefinedCollageDisplay)
    suspend fun getLocalAlbum(albumId: Int): List<Album>
}