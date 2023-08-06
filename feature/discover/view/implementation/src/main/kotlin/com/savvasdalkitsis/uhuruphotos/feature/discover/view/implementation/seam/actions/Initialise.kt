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
package com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.seam.actions

import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.people.People
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.seam.DiscoverActionsContext
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.seam.DiscoverMutation
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.seam.DiscoverMutation.*
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.seam.effects.DiscoverEffect
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.seam.effects.ErrorRefreshingPeople
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.ui.state.DiscoverState
import com.savvasdalkitsis.uhuruphotos.feature.discover.view.implementation.ui.state.SearchSuggestion
import com.savvasdalkitsis.uhuruphotos.feature.people.view.api.ui.state.toPerson
import com.savvasdalkitsis.uhuruphotos.foundation.coroutines.api.onErrors
import com.savvasdalkitsis.uhuruphotos.foundation.coroutines.api.onErrorsIgnore
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.math.max
import kotlin.math.min

data object Initialise : DiscoverAction() {
    context(DiscoverActionsContext) override fun handle(
        state: DiscoverState,
        effect: EffectHandler<DiscoverEffect>
    ) = merge(
        showLibrary(),
        showHeatMap(),
        showFeedDisplay(),
        showServerSearchSuggestion(),
        showPeopleSuggestion(effect),
        showSearchSuggestions()
    )

    context(DiscoverActionsContext)
    private fun showHeatMap() = heatMapUseCase.observeViewport()
        .map(::ChangeMapViewport)

    context(DiscoverActionsContext)
    private fun showSearchSuggestions() = combine(
        searchUseCase.getRecentTextSearches()
            .map {
                it.map(SearchSuggestion::RecentSearchSuggestion)
            },
        peopleUseCase.observePeopleByPhotoCount()
            .onErrorsIgnore()
            .toPeople()
            .map {
                it.map(SearchSuggestion::PersonSearchSuggestion)
            },
        searchUseCase.getSearchSuggestions()
            .map {
                it.map(SearchSuggestion::ServerSearchSuggestion)
            },
        queryFilter,
    ) { recentSearches, people, searchSuggestions, query ->
        when {
            query.isEmpty() -> emptyList()
            else -> recentSearches + people + searchSuggestions
        }.filterQuery(query)
    }.map(DiscoverMutation::ShowSearchSuggestions)

    context(DiscoverActionsContext)
    private fun showPeopleSuggestion(effect: EffectHandler<DiscoverEffect>) =
        peopleUseCase.observePeopleByPhotoCount()
            .onErrors {
                effect.handleEffect(ErrorRefreshingPeople)
            }
            .toPeople()
            .map { it.subList(0, max(0, min(10, it.size - 1))) }
            .map(DiscoverMutation::ShowPeople)

    context(DiscoverActionsContext)
    private fun showServerSearchSuggestion() =
        settingsUseCase.observeSearchSuggestionsEnabledMode().flatMapLatest { enabled ->
            if (enabled)
                searchUseCase.getRandomSearchSuggestion()
                    .map(DiscoverMutation::ShowSearchSuggestion)
            else
                flowOf(HideSuggestions)
        }

    context(DiscoverActionsContext)
    private fun showLibrary() = settingsUseCase.observeShowLibrary()
        .map(DiscoverMutation::ShowLibrary)

    context(DiscoverActionsContext)
    private fun showFeedDisplay() = feedUseCase
        .getFeedDisplay()
        .distinctUntilChanged()
        .map(DiscoverMutation::ChangeFeedDisplay)

    context(DiscoverActionsContext)
    private fun Flow<List<People>>.toPeople() = map { people ->
        val serverUrl = serverUseCase.getServerUrl()!!
        people.map {
            it.toPerson { url -> "$serverUrl$url" }
        }
    }

    private fun List<SearchSuggestion>.filterQuery(query: String): List<SearchSuggestion> =
        filter { it.filterable.contains(query, ignoreCase = true) }
}