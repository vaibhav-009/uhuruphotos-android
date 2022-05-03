package com.savvasdalkitsis.uhuruphotos.search.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.savvasdalkitsis.uhuruphotos.map.Locations
import com.savvasdalkitsis.uhuruphotos.map.view.MapView
import com.savvasdalkitsis.uhuruphotos.search.mvflow.SearchAction
import com.savvasdalkitsis.uhuruphotos.search.mvflow.SearchAction.LoadHeatMap
import com.savvasdalkitsis.uhuruphotos.search.view.state.SearchState

@Composable
fun SearchLocations(
    action: (SearchAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = "Locations"
        )
        Box(
            modifier = Modifier
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { action(LoadHeatMap) },
        ) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                location = Locations.TRAFALGAR_SQUARE,
                zoom = 3f,
                onMapClick = { action(LoadHeatMap) },
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable { action(LoadHeatMap) }
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "Photo map",
                style = MaterialTheme.typography.h4,
                color = Color.White,
            )
        }
    }
}