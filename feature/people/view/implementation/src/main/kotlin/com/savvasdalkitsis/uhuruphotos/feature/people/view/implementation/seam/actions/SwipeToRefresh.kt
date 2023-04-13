package com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.seam.actions

import com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.seam.PeopleActionsContext
import com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.seam.PeopleEffect
import com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.seam.PeopleMutation
import com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.ui.state.PeopleState
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import kotlinx.coroutines.flow.flow

object SwipeToRefresh : PeopleAction() {

    context(PeopleActionsContext) override fun handle(
        state: PeopleState,
        effect: EffectHandler<PeopleEffect>
    ) = flow<PeopleMutation> {
        refresh(effect)
    }
}