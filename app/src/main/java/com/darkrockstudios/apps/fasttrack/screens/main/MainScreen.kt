package com.darkrockstudios.apps.fasttrack.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.screens.competition.CompetitionScreen
import com.darkrockstudios.apps.fasttrack.screens.fasting.ExternalRequests
import com.darkrockstudios.apps.fasttrack.screens.fasting.FastingScreen
import com.darkrockstudios.apps.fasttrack.screens.food.FoodScreen
import com.darkrockstudios.apps.fasttrack.screens.log.LogScreen
import com.darkrockstudios.apps.fasttrack.screens.profile.ProfileScreen
import com.darkrockstudios.apps.fasttrack.screens.schedule.ScheduleScreen
import com.darkrockstudios.apps.fasttrack.utils.Utils
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

enum class ScreenPages {
	Fasting, Log, Profile, Food, Schedule, Competition;
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
@Composable
fun MainScreen(
	repository: ActiveFastRepository,
	onShareClick: () -> Unit,
	onInfoClick: () -> Unit,
	onAboutClick: () -> Unit,
	onSettingsClick: () -> Unit,
	externalRequests: ExternalRequests = ExternalRequests(),
) {
	val settings = koinInject<SettingsDatasource>()
	val onlineEnabled by settings.onlineSharingFlow().collectAsState(initial = settings.getOnlineSharing())

	val visiblePages = remember(onlineEnabled) {
		ScreenPages.entries.filter { it != ScreenPages.Competition || onlineEnabled }
	}

	val pagerState = rememberPagerState(initialPage = 0, pageCount = { visiblePages.size })
	val coroutineScope = rememberCoroutineScope()
	val drawerState = rememberDrawerState(DrawerValue.Closed)

	val shareEnabled = remember { mutableStateOf(repository.getFastStart() != null) }
	LaunchedEffect(repository.isFasting()) {
		shareEnabled.value = repository.getFastStart() != null
	}

	val fastingTitle    = stringResource(R.string.title_fasting)
	val logTitle        = stringResource(R.string.title_log)
	val profileTitle    = stringResource(R.string.title_profile)
	val foodTitle       = stringResource(R.string.title_food)
	val scheduleTitle   = stringResource(R.string.title_schedule)
	val competitionTitle = stringResource(R.string.title_competition)

	fun pageTitle(page: ScreenPages) = when (page) {
		ScreenPages.Fasting     -> fastingTitle
		ScreenPages.Log         -> logTitle
		ScreenPages.Profile     -> profileTitle
		ScreenPages.Food        -> foodTitle
		ScreenPages.Schedule    -> scheduleTitle
		ScreenPages.Competition -> competitionTitle
	}

	fun pageIcon(page: ScreenPages) = when (page) {
		ScreenPages.Fasting     -> R.drawable.ic_fasting
		ScreenPages.Log         -> R.drawable.ic_log
		ScreenPages.Profile     -> R.drawable.ic_profile
		ScreenPages.Food        -> R.drawable.ic_food
		ScreenPages.Schedule    -> R.drawable.ic_schedule
		ScreenPages.Competition -> R.drawable.ic_competition
	}

	val currentPage = visiblePages.getOrNull(pagerState.currentPage) ?: ScreenPages.Fasting

	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet {
				Spacer(Modifier.height(16.dp))
				Text(
					text = stringResource(R.string.app_name),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
				)
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
				visiblePages.forEachIndexed { index, page ->
					NavigationDrawerItem(
						icon = {
							Icon(painterResource(pageIcon(page)), contentDescription = null)
						},
						label = { Text(pageTitle(page)) },
						selected = pagerState.currentPage == index,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(index)
								drawerState.close()
							}
						},
						modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
					)
				}
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
				NavigationDrawerItem(
					icon = { Icon(Icons.Default.Info, contentDescription = null) },
					label = { Text(stringResource(R.string.action_info)) },
					selected = false,
					onClick = { coroutineScope.launch { drawerState.close() }; onInfoClick() },
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
				)
				NavigationDrawerItem(
					icon = {
						Icon(
							painterResource(R.drawable.ic_more_info),
							contentDescription = null,
							modifier = Modifier.size(24.dp),
						)
					},
					label = { Text(stringResource(R.string.action_about)) },
					selected = false,
					onClick = { coroutineScope.launch { drawerState.close() }; onAboutClick() },
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
				)
				NavigationDrawerItem(
					icon = {
						Icon(
							painterResource(R.drawable.ic_alert),
							contentDescription = null,
							modifier = Modifier.size(24.dp),
						)
					},
					label = { Text(stringResource(R.string.action_settings)) },
					selected = false,
					onClick = { coroutineScope.launch { drawerState.close() }; onSettingsClick() },
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
				)
			}
		},
	) {
		Scaffold(
			topBar = {
				TopAppBar(
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = MaterialTheme.colorScheme.primaryContainer,
						titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
						actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
						navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					),
					navigationIcon = {
						IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
							Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.nav_drawer_open))
						}
					},
					title = {
						Text(text = pageTitle(currentPage), style = MaterialTheme.typography.headlineMedium)
					},
					actions = {
						IconButton(onClick = onShareClick, enabled = shareEnabled.value) {
							Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
						}
					},
				)
			},
		) { paddingValues ->
			Content(
				modifier = Modifier.fillMaxSize(),
				contentPaddingValues = paddingValues,
				pagerState = pagerState,
				visiblePages = visiblePages,
				externalRequests = externalRequests,
			)
		}
	}
}

@Composable
private fun Content(
	modifier: Modifier,
	contentPaddingValues: PaddingValues,
	pagerState: PagerState,
	visiblePages: List<ScreenPages>,
	externalRequests: ExternalRequests,
) {
	val stateHolder = rememberSaveableStateHolder()
	HorizontalPager(
		modifier = modifier,
		state = pagerState,
		key = { page -> visiblePages.getOrNull(page)?.name ?: page },
		beyondViewportPageCount = pagerState.pageCount,
		userScrollEnabled = false,
	) { page ->
		stateHolder.SaveableStateProvider(key = visiblePages.getOrNull(page)?.name ?: page) {
			PageContainer(
				page = visiblePages.getOrNull(page) ?: ScreenPages.Fasting,
				contentPaddingValues = contentPaddingValues,
				externalRequests = externalRequests,
			)
		}
	}
}

@ExperimentalTime
@Composable
private fun PageContainer(
	page: ScreenPages,
	contentPaddingValues: PaddingValues,
	externalRequests: ExternalRequests,
) {
	when (page) {
		ScreenPages.Fasting     -> FastingScreen(contentPaddingValues = contentPaddingValues, externalRequests = externalRequests)
		ScreenPages.Log         -> LogScreen(contentPaddingValues)
		ScreenPages.Profile     -> {
			val context = LocalContext.current
			ProfileScreen(
				contentPaddingValues = contentPaddingValues,
				onShowInfoDialog = { titleRes, contentRes -> Utils.showInfoDialog(titleRes, contentRes, context) },
			)
		}
		ScreenPages.Food        -> FoodScreen(contentPaddingValues = contentPaddingValues)
		ScreenPages.Schedule    -> ScheduleScreen(contentPaddingValues = contentPaddingValues)
		ScreenPages.Competition -> CompetitionScreen(contentPaddingValues = contentPaddingValues)
	}
}
