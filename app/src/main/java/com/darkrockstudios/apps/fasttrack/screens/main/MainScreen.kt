package com.darkrockstudios.apps.fasttrack.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
	Fasting,
	Log,
	Profile,
	Food,
	Schedule,
	Competition;
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

	val pagerState = rememberPagerState(
		initialPage = 0,
		pageCount = { visiblePages.size },
	)
	val coroutineScope = rememberCoroutineScope()

	var showMenu by remember { mutableStateOf(false) }
	val shareEnabled = remember { mutableStateOf(repository.getFastStart() != null) }

	val fastingTitle = stringResource(id = R.string.title_fasting)
	val logTitle = stringResource(id = R.string.title_log)
	val profileTitle = stringResource(id = R.string.title_profile)
	val foodTitle = stringResource(id = R.string.title_food)
	val scheduleTitle = stringResource(id = R.string.title_schedule)
	val competitionTitle = stringResource(id = R.string.title_competition)

	fun pageTitle(page: ScreenPages) = when (page) {
		ScreenPages.Fasting -> fastingTitle
		ScreenPages.Log -> logTitle
		ScreenPages.Profile -> profileTitle
		ScreenPages.Food -> foodTitle
		ScreenPages.Schedule -> scheduleTitle
		ScreenPages.Competition -> competitionTitle
	}

	fun pageIcon(page: ScreenPages) = when (page) {
		ScreenPages.Fasting -> R.drawable.ic_fasting
		ScreenPages.Log -> R.drawable.ic_log
		ScreenPages.Profile -> R.drawable.ic_profile
		ScreenPages.Food -> R.drawable.ic_food
		ScreenPages.Schedule -> R.drawable.ic_schedule
		ScreenPages.Competition -> R.drawable.ic_competition
	}

	val currentPage = visiblePages.getOrNull(pagerState.currentPage) ?: ScreenPages.Fasting
	val currentTitle = pageTitle(currentPage)

	LaunchedEffect(repository.isFasting()) {
		shareEnabled.value = repository.getFastStart() != null
	}

	Scaffold(
		topBar = {
			TopAppBar(
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				),
				modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary),
				title = {
					Text(text = currentTitle, style = MaterialTheme.typography.headlineMedium)
				},
				actions = {
					IconButton(onClick = onShareClick, enabled = shareEnabled.value) {
						Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
					}
					IconButton(onClick = onInfoClick) {
						Icon(Icons.Default.Info, contentDescription = stringResource(R.string.action_info))
					}
					IconButton(onClick = { showMenu = !showMenu }) {
						Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_button_description))
					}
					DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
						DropdownMenuItem(
							text = { Text(stringResource(R.string.action_about)) },
							onClick = { onAboutClick(); showMenu = false },
						)
						DropdownMenuItem(
							text = { Text(stringResource(R.string.action_settings)) },
							onClick = { onSettingsClick(); showMenu = false },
						)
					}
				},
			)
		},
		bottomBar = {
			val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
			val compactHeight = windowSizeClass.minHeightDp < windowSizeClass.minWidthDp
			if (!compactHeight) {
				NavigationBar(
					modifier = Modifier.background(MaterialTheme.colorScheme.primary).fillMaxWidth()
				) {
					visiblePages.forEachIndexed { index, page ->
						NavigationBarItem(
							icon = {
								Icon(painterResource(pageIcon(page)), contentDescription = pageTitle(page))
							},
							label = { Text(pageTitle(page)) },
							selected = pagerState.currentPage == index,
							onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
						)
					}
				}
			}
		},
	) { paddingValues ->
		val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
		val compactHeight = windowSizeClass.minHeightDp < windowSizeClass.minWidthDp

		if (compactHeight) {
			Row(
				modifier = Modifier
					.padding(top = paddingValues.calculateTopPadding())
					.fillMaxSize()
			) {
				NavigationRail {
					visiblePages.forEachIndexed { index, page ->
						NavigationRailItem(
							icon = { Icon(painterResource(pageIcon(page)), contentDescription = pageTitle(page)) },
							label = { Text(pageTitle(page)) },
							selected = pagerState.currentPage == index,
							onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
						)
					}
				}
				Content(
					modifier = Modifier.weight(1f),
					contentPaddingValues = PaddingValues(
						end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
						bottom = paddingValues.calculateBottomPadding(),
					),
					pagerState = pagerState,
					visiblePages = visiblePages,
					externalRequests = externalRequests,
				)
			}
		} else {
			Box(modifier = Modifier.fillMaxSize()) {
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
		ScreenPages.Fasting -> FastingScreen(
			contentPaddingValues = contentPaddingValues,
			externalRequests = externalRequests,
		)
		ScreenPages.Log -> LogScreen(contentPaddingValues)
		ScreenPages.Profile -> {
			val context = LocalContext.current
			ProfileScreen(
				contentPaddingValues = contentPaddingValues,
				onShowInfoDialog = { titleRes, contentRes ->
					Utils.showInfoDialog(titleRes, contentRes, context)
				},
			)
		}
		ScreenPages.Food -> FoodScreen(contentPaddingValues = contentPaddingValues)
		ScreenPages.Schedule -> ScheduleScreen(contentPaddingValues = contentPaddingValues)
		ScreenPages.Competition -> CompetitionScreen(contentPaddingValues = contentPaddingValues)
	}
}
