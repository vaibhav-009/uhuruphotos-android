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
package com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.viewmodel

import androidx.lifecycle.viewModelScope
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.api.navigation.SettingsNavigationRoute
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.SettingsActionsContext
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.SettingsEffectsContext
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.actions.LoadSettings
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.actions.SettingsAction
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.effects.SettingsEffect
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.ui.state.SettingsState
import com.savvasdalkitsis.uhuruphotos.foundation.navigation.api.viewmodel.NavigationViewModel
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.ActionHandlerWithContext
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandlerWithContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    settingsActionsContext: SettingsActionsContext,
    settingsEffectsContext: SettingsEffectsContext,
) : NavigationViewModel<SettingsState, SettingsEffect, SettingsAction, SettingsNavigationRoute>(
    ActionHandlerWithContext(settingsActionsContext),
    EffectHandlerWithContext(settingsEffectsContext),
    SettingsState()
) {

    init {
        viewModelScope.launch {
            action(LoadSettings)
        }
    }
}