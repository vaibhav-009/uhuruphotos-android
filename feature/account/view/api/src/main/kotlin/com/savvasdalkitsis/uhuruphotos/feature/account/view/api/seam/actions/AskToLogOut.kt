package com.savvasdalkitsis.uhuruphotos.feature.account.view.api.seam.actions

import com.savvasdalkitsis.uhuruphotos.feature.account.view.api.seam.AccountOverviewActionsContext
import com.savvasdalkitsis.uhuruphotos.feature.account.view.api.seam.AccountOverviewEffect
import com.savvasdalkitsis.uhuruphotos.feature.account.view.api.seam.AccountOverviewMutation.*
import com.savvasdalkitsis.uhuruphotos.feature.account.view.api.ui.state.AccountOverviewState
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import kotlinx.coroutines.flow.flowOf

object AskToLogOut : AccountOverviewAction() {
    context(AccountOverviewActionsContext) override fun handle(
        state: AccountOverviewState,
        effect: EffectHandler<AccountOverviewEffect>
    ) = flowOf(ShowLogOutConfirmation)
}