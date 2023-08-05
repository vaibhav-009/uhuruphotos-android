/*
Copyright 2023 Savvas Dalkitsis

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
package com.savvasdalkitsis.uhuruphotos.foundation.upload.implementation.service

import android.content.Context
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.savvasdalkitsis.uhuruphotos.feature.auth.domain.api.usecase.AuthenticationHeadersUseCase
import com.savvasdalkitsis.uhuruphotos.feature.auth.domain.api.usecase.ServerUseCase
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.model.toMediaItemHash
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.Md5Hash
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.usecase.RemoteMediaUseCase
import com.savvasdalkitsis.uhuruphotos.feature.user.domain.api.usecase.UserUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import javax.inject.Inject

class UploadService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverUseCase: ServerUseCase,
    private val authenticationHeadersUseCase: AuthenticationHeadersUseCase,
    private val remoteMediaUseCase: RemoteMediaUseCase,
    private val userUseCase: UserUseCase,
) {

    fun upload(contentUri: String): String? {
        val baseUrl = serverUseCase.getServerUrl() ?: return null
        val url = "$baseUrl/api/upload/"
        val headers = authenticationHeadersUseCase.headers(url)
        return MultipartUploadRequest(context, baseUrl)
            .addFileToUpload(contentUri, parameterName = "file", fileName = "blob")
            .setUsesFixedLengthStreamingMode(false)
            .run {
                headers.fold(this) { c, (key, value) -> c.addHeader(key, value) }
            }
            .setMethod("POST")
            .startUpload()
    }

    suspend fun exists(md5: Md5Hash): Result<Boolean, Throwable> =
        userUseCase.getUserOrRefresh().andThen { user ->
            remoteMediaUseCase.exists(md5.toMediaItemHash(user.id))
        }
}