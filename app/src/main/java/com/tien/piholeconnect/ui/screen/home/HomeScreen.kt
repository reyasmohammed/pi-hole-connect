package com.tien.piholeconnect.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tien.piholeconnect.R
import com.tien.piholeconnect.model.PiHoleOverTimeData
import com.tien.piholeconnect.model.PiHoleSummary
import com.tien.piholeconnect.repository.PiHoleRepository
import com.tien.piholeconnect.ui.component.*
import com.tien.piholeconnect.ui.theme.info
import com.tien.piholeconnect.ui.theme.success
import com.tien.piholeconnect.ui.theme.toColorInt
import com.tien.piholeconnect.ui.theme.warning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat.getTimeInstance

@Composable
fun HomeScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    var isRefreshing by remember { mutableStateOf(false) }

    val totalQueries: Int by animateIntAsState(viewModel.totalQueries)
    val totalBlockedQueries: Int by animateIntAsState(viewModel.totalBlockedQueries)
    val queryBlockingPercentage: Float by animateFloatAsState(viewModel.queryBlockingPercentage.toFloat())
    val blockedDomainListCount: Int by animateIntAsState(viewModel.blockedDomainListCount)

    val successColorInt = MaterialTheme.colors.success.toColorInt()
    val errorColorInt = MaterialTheme.colors.error.toColorInt()

    val queriesOverTimeData = remember(viewModel.queriesOverTime) {
        LineChartData(
            label = "Queries over time",
            viewModel.queriesOverTime.map { Pair(it.key.toFloat() * 1000L, it.value.toFloat()) }) {
            color = successColorInt
            fillColor = successColorInt
        }
    }

    val adsOverTimeData = remember(viewModel.adsOverTime) {
        LineChartData(
            label = "Ads over time",
            viewModel.adsOverTime.map { Pair(it.key.toFloat() * 1000L, it.value.toFloat()) }) {
            color = errorColorInt
            fillColor = errorColorInt
        }
    }

    DisposableEffect(Unit) {
        val job = viewModel.viewModelScope.launch {
            while (true) {
                viewModel.refresh()
                delay(5000)
            }
        }

        onDispose {
            job.cancel()
        }
    }

    Column(
        modifier
            .padding(5.dp)
            .fillMaxHeight()
    ) {
        SwipeToRefreshLayout(
            refreshingState = isRefreshing,
            onRefresh = {
                viewModel.viewModelScope.launch {
                    isRefreshing = true
                    viewModel.refresh()
                    isRefreshing = false
                }
            }) {
            Column {
                Row {
                    StatsCard(
                        name = "Total Queries",
                        statistics = "%,d".format(totalQueries),
                        backGroundColor = MaterialTheme.colors.success,
                        modifier = Modifier
                            .padding(end = 2.5.dp)
                            .weight(1f)
                    )
                    StatsCard(
                        name = "Queries Blocked",
                        statistics = "%,d".format(totalBlockedQueries),
                        backGroundColor = MaterialTheme.colors.info,
                        modifier = Modifier
                            .padding(start = 2.5.dp)
                            .weight(1f)
                    )
                }
                Row(Modifier.padding(top = 5.dp)) {
                    StatsCard(
                        name = "Percent Blocked",
                        statistics = "%.2f%%".format(queryBlockingPercentage),
                        backGroundColor = MaterialTheme.colors.warning,
                        modifier = Modifier
                            .padding(end = 2.5.dp)
                            .weight(1f)
                    )
                    StatsCard(
                        name = "Blocklist",
                        statistics = "%,d".format(blockedDomainListCount),
                        backGroundColor = MaterialTheme.colors.error,
                        modifier = Modifier
                            .padding(start = 2.5.dp)
                            .weight(1f)
                    )
                }
            }
        }
        Card(
            Modifier
                .padding(vertical = 15.dp)
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            var value: Iterable<SelectedValue> by remember { mutableStateOf(listOf()) }
            val permittedQueriesCount =
                value.firstOrNull { it.label == queriesOverTimeData.label }?.value?.second?.toInt()
            val blockedQueriesCount =
                value.firstOrNull { it.label == adsOverTimeData.label }?.value?.second?.toInt()
            val dateFormat = remember { getTimeInstance() }
            val startTime = value.firstOrNull { it.value != null }?.value?.first
            val endTime = startTime?.let { it + 600_000 }
            val formattedStartTime = startTime?.let { dateFormat.format(it) }
            val formattedEndTime = endTime?.let { dateFormat.format(endTime) }

            Column {
                Column(Modifier.padding(15.dp)) {
                    Text(
                        stringResource(R.string.home_queries_chart_title),
                        style = MaterialTheme.typography.h6
                    )
                    Column(Modifier.alpha(if (permittedQueriesCount != null && blockedQueriesCount != null) 1f else 0f)) {
                        Text(
                            "${stringResource(R.string.home_queries_chart_time_info_1)} $formattedStartTime ${
                                stringResource(
                                    R.string.home_queries_chart_time_info_2
                                )
                            } $formattedEndTime", style = MaterialTheme.typography.caption
                        )
                        Text(
                            "%s: %d".format(
                                stringResource(R.string.home_queries_chart_permitted_queries_label),
                                permittedQueriesCount
                            ), style = MaterialTheme.typography.caption
                        )
                        Text(
                            "%s: %d".format(
                                stringResource(R.string.home_queries_chart_blocked_queries_label),
                                blockedQueriesCount
                            ), style = MaterialTheme.typography.caption
                        )
                    }
                }
                LineChart(
                    lineData = listOf(queriesOverTimeData, adsOverTimeData),
                    onValueSelected = { value = it },
                    modifier = Modifier.fillMaxSize()
                ) {
                    xAxis.labelCount = 5
                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String =
                            dateFormat.format(value)
                    }
                    axisLeft.axisMinimum = 0f
                    axisRight.axisMinimum = 0f
                }
            }
        }
    }
}

@Suppress("UNREACHABLE_CODE")
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ScaffoldPreview {
        HomeScreen(
            HomeViewModel(object : PiHoleRepository {
                override suspend fun getStatusSummary(): PiHoleSummary = PiHoleSummary(
                    TODO("Not yet implemented")
                )

                override suspend fun getOverTimeData10Minutes(): PiHoleOverTimeData {
                    TODO("Not yet implemented")
                }
            })
        )
    }
}