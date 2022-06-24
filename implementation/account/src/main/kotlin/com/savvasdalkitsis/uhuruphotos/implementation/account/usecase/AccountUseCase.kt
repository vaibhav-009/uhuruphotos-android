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
package com.savvasdalkitsis.uhuruphotos.implementation.account.usecase

import com.savvasdalkitsis.uhuruphotos.api.account.usecase.AccountUseCase
import com.savvasdalkitsis.uhuruphotos.api.db.Database
import com.savvasdalkitsis.uhuruphotos.api.db.extensions.async
import com.savvasdalkitsis.uhuruphotos.api.image.cache.ImageCacheController
import com.savvasdalkitsis.uhuruphotos.api.video.VideoCache
import com.savvasdalkitsis.uhuruphotos.api.worker.WorkScheduler
import okhttp3.Cache
import javax.inject.Inject

class AccountUseCase @Inject constructor(
    private val db: Database,
    private val imageCacheController: ImageCacheController,
    @VideoCache
    private val videoCache: Cache,
    private val workScheduler: WorkScheduler,
) : AccountUseCase {

    override suspend fun logOut() {
        async {
            workScheduler.cancelAllScheduledWork()
            with(db) {
                albumsQueries.clearAll()
                autoAlbumQueries.clearAll()
                autoAlbumPeopleQueries.clearAll()
                autoAlbumPhotosQueries.clearAll()
                autoAlbumsQueries.clearAll()
                peopleQueries.clearAll()
                personQueries.clearAll()
                photoDetailsQueries.clearAll()
                photoSummaryQueries.clearAll()
                searchQueries.clearAll()
                tokenQueries.clearAll()
                userQueries.clearAll()
                userAlbumQueries.clearAll()
                userAlbumPhotosQueries.clearAll()
                userAlbumsQueries.clearAll()
            }
            imageCacheController.clear()
            videoCache.evictAll()
        }
    }
}