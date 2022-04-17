package com.savvasdalkitsis.librephotos.feedpage.viewmodel

import com.savvasdalkitsis.librephotos.albums.usecase.AlbumsUseCase
import com.savvasdalkitsis.librephotos.account.usecase.AccountUseCase
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction.*
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction.ChangeDisplay
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction.HideFeedDisplayChoice
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction.ShowFeedDisplayChoice
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageEffect
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageEffect.OpenPhotoDetails
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageEffect.ReloadApp
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageMutation
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageMutation.*
import com.savvasdalkitsis.librephotos.feedpage.usecase.FeedPageUseCase
import com.savvasdalkitsis.librephotos.feedpage.view.state.FeedPageState
import com.savvasdalkitsis.librephotos.userbadge.usecase.UserBadgeUseCase
import com.savvasdalkitsis.librephotos.viewmodel.Handler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FeedPageHandler @Inject constructor(
    private val albumsUseCase: AlbumsUseCase,
    private val userBadgeUseCase: UserBadgeUseCase,
    private val accountUseCase: AccountUseCase,
    private val feedPageUseCase: FeedPageUseCase,
) : Handler<FeedPageState, FeedPageEffect, FeedPageAction, FeedPageMutation> {

    override fun invoke(
        state: FeedPageState,
        action: FeedPageAction,
        effect: suspend (FeedPageEffect) -> Unit,
    ): Flow<FeedPageMutation> = when (action) {
        is LoadFeed -> merge(
            feedPageUseCase
                .getFeedDisplay()
                .distinctUntilChanged()
                .map(FeedPageMutation::ChangeDisplay),
            flowOf(Loading),
            albumsUseCase.getAlbums(refresh = false)
                .debounce(200)
                .map(::ShowAlbums),
            userBadgeUseCase.getUserBadgeState()
                .map(::UserBadgeUpdate),
        )
        UserBadgePressed -> flowOf(ShowAccountOverview)
        DismissAccountOverview -> flowOf(HideAccountOverview)
        LogOut -> flow {
            accountUseCase.logOut()
            effect(ReloadApp)
        }
        RefreshAlbums -> flow {
            emit(StartRefreshing)
            albumsUseCase.startRefreshAlbumsWork()
            delay(200)
            emit(StopRefreshing)
        }
        is SelectedPhoto -> flow {
            effect(OpenPhotoDetails(action.photo.id!!, action.center, action.scale))
        }
        is ChangeDisplay -> flow {
            feedPageUseCase.setFeedDisplay(action.display)
            emit(FeedPageMutation.HideFeedDisplayChoice)
        }
        HideFeedDisplayChoice -> flowOf(FeedPageMutation.HideFeedDisplayChoice)
        ShowFeedDisplayChoice -> flowOf(FeedPageMutation.ShowFeedDisplayChoice)
    }
}