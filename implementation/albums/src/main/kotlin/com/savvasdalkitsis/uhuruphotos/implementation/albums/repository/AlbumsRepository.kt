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
package com.savvasdalkitsis.uhuruphotos.implementation.albums.repository

import com.savvasdalkitsis.uhuruphotos.api.albums.repository.AlbumsRepository
import com.savvasdalkitsis.uhuruphotos.api.albums.service.AlbumsService
import com.savvasdalkitsis.uhuruphotos.api.albums.service.model.Album
import com.savvasdalkitsis.uhuruphotos.api.albums.service.model.AlbumsByDate
import com.savvasdalkitsis.uhuruphotos.api.albums.service.model.toAlbum
import com.savvasdalkitsis.uhuruphotos.api.albums.service.model.toAutoAlbums
import com.savvasdalkitsis.uhuruphotos.api.albums.service.model.toUserAlbums
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.Database
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.AlbumsQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.AutoAlbumPeopleQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.AutoAlbumPhotosQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.AutoAlbumQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.AutoAlbums
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.AutoAlbumsQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.GetAlbums
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.GetAutoAlbum
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.GetPeopleForAutoAlbum
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.GetPersonAlbums
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.GetTrash
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.GetUserAlbum
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.UserAlbum
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.UserAlbumPhotosQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.UserAlbumQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.UserAlbums
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.albums.UserAlbumsQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.extensions.async
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.extensions.await
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.extensions.awaitSingle
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.media.RemoteMediaItemDetailsQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.media.RemoteMediaItemSummaryQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.media.RemoteMediaTrashQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.people.PeopleQueries
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.person.PersonQueries
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.model.toDbModel
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.model.toTrash
import com.savvasdalkitsis.uhuruphotos.feature.people.domain.api.service.model.toDbModel
import com.savvasdalkitsis.uhuruphotos.foundation.coroutines.api.safelyOnStartIgnoring
import com.savvasdalkitsis.uhuruphotos.foundation.group.api.model.Group
import com.savvasdalkitsis.uhuruphotos.foundation.group.api.model.groupBy
import com.savvasdalkitsis.uhuruphotos.foundation.log.api.runCatchingWithLog
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import javax.inject.Inject

internal class AlbumsRepository @Inject constructor(
    private val db: Database,
    private val albumsService: AlbumsService,
    private val albumsQueries: AlbumsQueries,
    private val autoAlbumsQueries: AutoAlbumsQueries,
    private val autoAlbumQueries: AutoAlbumQueries,
    private val autoAlbumPhotosQueries: AutoAlbumPhotosQueries,
    private val autoAlbumPeopleQueries: AutoAlbumPeopleQueries,
    private val personQueries: PersonQueries,
    private val peopleQueries: PeopleQueries,
    private val remoteMediaItemSummaryQueries: RemoteMediaItemSummaryQueries,
    private val remoteMediaItemDetailsQueries: RemoteMediaItemDetailsQueries,
    private val userAlbumsQueries: UserAlbumsQueries,
    private val userAlbumQueries: UserAlbumQueries,
    private val userAlbumPhotosQueries: UserAlbumPhotosQueries,
    private val remoteMediaTrashQueries: RemoteMediaTrashQueries,
    private val settingsUseCase: com.savvasdalkitsis.uhuruphotos.feature.settings.domain.api.usecase.SettingsUseCase,
) : AlbumsRepository {

    private var allAlbums: Group<String, GetAlbums> = Group(emptyMap())

    override suspend fun hasAlbums() = albumsQueries.albumsCount().awaitSingle() > 0

    override fun observeAlbumsByDate() : Flow<Group<String, GetAlbums>> =
        albumsQueries.getAlbums(limit = -1).asFlow()
            .onStart {
                if (allAlbums.items.isEmpty()) {
                    emitAll(albumsQueries.getAlbums(limit = 100).asFlow().take(1))
                }
            }
            .mapToList().groupBy(GetAlbums::id)
            .onStart {
                if (allAlbums.items.isNotEmpty()) {
                    emit(allAlbums)
                }
            }
            .onEach {
                allAlbums = it
            }
            .distinctUntilChanged()

    override suspend fun getAlbumsByDate() : Group<String, GetAlbums> =
        albumsQueries.getAlbums(limit = -1).await().groupBy(GetAlbums::id).let(::Group)

    override suspend fun refreshAlbums(shallow: Boolean, onProgressChange: suspend (Int) -> Unit) =
        process(
            albumsFetcher = { albumsService.getAlbumsByDate() },
            albumFetcher = getAlbumAllPages(),
            shallow = shallow,
            onProgressChange = onProgressChange,
            incompleteAlbumsProcessor = { albums ->
                albumsQueries.transaction {
                    albumsQueries.clearAll()
                    for (album in albums.map { it.toAlbum() }) {
                        albumsQueries.insert(album)
                    }
                }
            }
        )

    override suspend fun refreshAlbum(albumId: String) {
        process(
            albumsFetcher = { AlbumsByDate(
                results = listOf(Album.IncompleteAlbum(albumId, null, "", true, 1))
            ) },
            albumFetcher = getAlbumAllPages(),
            shallow = false,
        )
    }


    override fun observePersonAlbums(personId: Int) : Flow<Group<String, GetPersonAlbums>> =
        albumsQueries.getPersonAlbums(personId).asFlow().mapToList().groupBy(GetPersonAlbums::id)
            .distinctUntilChanged()
            .safelyOnStartIgnoring {
                downloadPersonAlbums(personId)
            }

    override suspend fun getPersonAlbums(personId: Int) : Group<String, GetPersonAlbums> =
        albumsQueries.getPersonAlbums(personId).await().groupBy(GetPersonAlbums::id).let(::Group)


    override fun observeAutoAlbums(): Flow<List<AutoAlbums>> =
        autoAlbumsQueries.getAutoAlbums().asFlow().mapToList()
            .distinctUntilChanged()

    override suspend fun getAutoAlbums(): List<AutoAlbums> =
        autoAlbumsQueries.getAutoAlbums().await()

    override fun observeAutoAlbum(albumId: Int): Flow<List<GetAutoAlbum>> =
        autoAlbumQueries.getAutoAlbum(albumId.toString()).asFlow().mapToList()
            .distinctUntilChanged()

    override suspend fun getAutoAlbum(albumId: Int): Group<String, GetAutoAlbum> =
        autoAlbumQueries.getAutoAlbum(albumId.toString()).await().groupBy(GetAutoAlbum::id).let(::Group)

    override fun observeAutoAlbumPeople(albumId: Int): Flow<List<GetPeopleForAutoAlbum>> =
        autoAlbumPeopleQueries.getPeopleForAutoAlbum(albumId.toString())
            .asFlow().mapToList()

    override suspend fun refreshAutoAlbums() = runCatchingWithLog {
        val albums = albumsService.getAutoAlbums()
        autoAlbumsQueries.transaction {
            autoAlbumsQueries.clearAll()
            for (album in albums.results) {
                autoAlbumsQueries.insert(album.toAutoAlbums())
            }
        }
    }

    override suspend fun refreshAutoAlbum(albumId: Int): Result<Unit> = runCatchingWithLog {
        val album = albumsService.getAutoAlbum(albumId.toString())
        db.transaction {
            autoAlbumQueries.insert(
                id = albumId.toString(),
                title = album.title,
                timestamp = album.timestamp,
                createdOn = album.createdOn,
                isFavorite = album.isFavorite,
                gpsLat = album.gpsLat,
                gpsLon = album.gpsLon,
            )
            autoAlbumPeopleQueries.removePeopleForAlbum(albumId.toString())
            for (person in album.people) {
                peopleQueries.insertPerson(person.toDbModel())
                autoAlbumPeopleQueries.insert(person.id, albumId.toString())
            }
            autoAlbumPhotosQueries.removePhotosForAlbum(albumId.toString())
            for (photo in album.photos) {
                remoteMediaItemDetailsQueries.insert(photo.toDbModel())
                autoAlbumPhotosQueries.insert(photo.imageHash, albumId.toString())
            }
        }
    }


    override fun observeUserAlbums(): Flow<List<UserAlbums>> =
        userAlbumsQueries.getUserAlbums().asFlow().mapToList()
            .distinctUntilChanged()

    override suspend fun getUserAlbums(): List<UserAlbums> =
        userAlbumsQueries.getUserAlbums().await()

    override suspend fun getUserAlbum(albumId: Int): Group<String, GetUserAlbum> =
        userAlbumQueries.getUserAlbum(albumId.toString()).await().groupBy(GetUserAlbum::id).let(::Group)

    override fun observeUserAlbum(albumId: Int): Flow<Group<String, GetUserAlbum>> =
        userAlbumQueries.getUserAlbum(albumId.toString())
            .asFlow().mapToList().groupBy(GetUserAlbum::id)
            .distinctUntilChanged()

    override suspend fun refreshUserAlbums() = runCatchingWithLog {
        val albums = albumsService.getUserAlbums()
        userAlbumsQueries.transaction {
            userAlbumsQueries.clearAll()
            for (album in albums.results) {
                userAlbumsQueries.insert(album.toUserAlbums())
            }
        }
    }

    override suspend fun refreshUserAlbum(albumId: Int): Result<Unit> = runCatchingWithLog {
        val album = albumsService.getUserAlbum(albumId.toString())
        db.transaction {
            userAlbumQueries.insert(
                UserAlbum(
                    id = albumId.toString(),
                    title = album.title,
                    date = album.date,
                    location = album.location,
                )
            )
            userAlbumPhotosQueries.removePhotosForAlbum(albumId.toString())
            for (photo in album.groups.flatMap { it.photos }) {
                remoteMediaItemSummaryQueries.insert(photo.toDbModel(albumId.toString()))
                userAlbumPhotosQueries.insert(photo.id, albumId.toString())
            }
        }
    }


    override fun observeTrash(): Flow<Group<String, GetTrash>> =
        albumsQueries.getTrash().asFlow()
            .mapToList().groupBy(GetTrash::id)
            .distinctUntilChanged()

    override suspend fun hasTrash() = remoteMediaTrashQueries.count().awaitSingle() > 0

    override suspend fun getTrash(): Group<String, GetTrash> =
        albumsQueries.getTrash().await().groupBy(GetTrash::id).let(::Group)

    override suspend fun refreshTrash() = runCatchingWithLog {
        val trash = albumsService.getTrash().results
        async {
            albumsQueries.transaction {
                for (album in trash) {
                    albumsQueries.insert(album.toAlbum())
                }
            }
        }
        async { remoteMediaTrashQueries.clear() }
        for (incompleteAlbum in trash) {
            val id = incompleteAlbum.id
            val completeAlbum = albumsService.getTrashAlbum(id).results
            async {
                completeAlbum.items
                    .map { it.toTrash(id) }
                    .forEach {
                        remoteMediaTrashQueries.insert(it)
                    }
            }
        }
    }


    private suspend fun downloadPersonAlbums(personId: Int) {
        process(
            albumsFetcher = { albumsService.getAlbumsForPerson(personId) },
            albumFetcher = { albumsService.getAlbumForPerson(it, personId).results },
            shallow = false,
            incompleteAlbumsProcessor = { albums ->
                albumsQueries.transaction {
                    for (album in albums.map { it.toAlbum() }) {
                        albumsQueries.insert(album)
                    }
                }
            },
            completeAlbumProcessor = { album ->
                for (photo in album.items) {
                    personQueries.insert(
                        id = null,
                        personId = personId,
                        photoId = photo.id
                    )
                }
            }
        )
    }

    private fun getAlbumAllPages(): suspend (String) -> Album.CompleteAlbum = { id ->
        var page = 1
        val albums = mutableListOf<Album.CompleteAlbum>()
        do {
            val album = albumsService.getAlbum(id, page).results
            albums += album
            page++
        } while (albums.sumOf { it.items.size } < album.numberOfItems)
        albums.reduce { acc, completeAlbum ->
            acc.copy(
                items = acc.items + completeAlbum.items
            )
        }
    }

    private suspend fun process(
        albumsFetcher: suspend () -> AlbumsByDate,
        albumFetcher: suspend (String) -> Album.CompleteAlbum,
        shallow: Boolean,
        onProgressChange: suspend (Int) -> Unit = {},
        incompleteAlbumsProcessor: suspend (List<Album.IncompleteAlbum>) -> Unit = {},
        completeAlbumProcessor: suspend (Album.CompleteAlbum) -> Unit = {},
    ): Result<Unit> = runCatchingWithLog {
        onProgressChange(0)
        val albums = albumsFetcher()
        incompleteAlbumsProcessor(albums.results)
        val albumsToDownloadSummaries = when {
            shallow -> albums.results.take(settingsUseCase.getFeedDaysToRefresh())
            else -> albums.results
        }
        for ((index, incompleteAlbum) in albumsToDownloadSummaries.withIndex()) {
            val id = incompleteAlbum.id
            updateSummaries(id, albumFetcher, completeAlbumProcessor)
            onProgressChange((100 * ((index + 1) / albumsToDownloadSummaries.size.toFloat())).toInt())
        }
    }

    private suspend fun updateSummaries(
        id: String,
        albumFetcher: suspend (String) -> Album.CompleteAlbum,
        completeAlbumProcessor: suspend (Album.CompleteAlbum) -> Unit,
    ) {
        val completeAlbum = albumFetcher(id)
        completeAlbumProcessor(completeAlbum)
        async {
            remoteMediaItemSummaryQueries.transaction {
                remoteMediaItemSummaryQueries.deletePhotoSummariesforAlbum(id)
                completeAlbum.items
                    .map { it.toDbModel(id) }
                    .forEach {
                        remoteMediaItemSummaryQueries.insert(it)
                    }
            }
        }
    }
}