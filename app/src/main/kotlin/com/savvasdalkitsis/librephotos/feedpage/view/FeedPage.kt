package com.savvasdalkitsis.librephotos.feed.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.savvasdalkitsis.librephotos.accountoverview.view.AccountOverviewPopUp
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction
import com.savvasdalkitsis.librephotos.feedpage.mvflow.FeedPageAction.*
import com.savvasdalkitsis.librephotos.feedpage.view.state.FeedPageState
import com.savvasdalkitsis.librephotos.home.view.HomeScaffold
import com.savvasdalkitsis.librephotos.infrastructure.extensions.blurIf
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun FeedPage(
    controllersProvider: com.savvasdalkitsis.librephotos.navigation.ControllersProvider,
    state: FeedPageState,
    action: (FeedPageAction) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    HomeScaffold(
        modifier = Modifier.blurIf(state.showAccountOverview),
        navController = controllersProvider.navController!!,
        userBadgeState = state.userBadgeState,
        feedDisplay = state.feedState.feedDisplay,
        userBadgePressed = { action(UserBadgePressed) },
        onReselected = {
            coroutineScope.launch {
                listState.animateScrollToItem(0, 0)
                gridState.animateScrollToItem(0, 0)
            }
        },
        actionBarContent = {
            FeedDisplayActionButton(
                onShow = { action(ShowFeedDisplayChoice) },
                onHide = { action(HideFeedDisplayChoice) },
                onChange = { action(ChangeDisplay(it)) },
                expanded = state.showFeedDisplayChoice,
                currentFeedDisplay = state.feedState.feedDisplay
            )
        },
    ) { contentPadding ->
        SwipeRefresh(
            indicatorPadding = contentPadding,
            state = rememberSwipeRefreshState(isRefreshing = state.isRefreshing),
            onRefresh = { action(RefreshAlbums) }
        ) {
            Feed(
                contentPadding,
                state.feedState,
                listState = listState,
                gridState = gridState,
                onPhotoSelected = { photo, center, scale ->
                    action(SelectedPhoto(photo, center, scale))
                },
                onChangeDisplay = { action(ChangeDisplay(it)) }
            )
        }
        AccountOverviewPopUp(
            visible = state.showAccountOverview,
            userBadgeState = state.userBadgeState,
            onDismiss = { action(DismissAccountOverview) },
            onLogoutClicked = { action(LogOut) },
        )
    }
}