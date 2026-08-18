package com.apphider.ui.hidden

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apphider.ui.components.AppGridItem
import com.apphider.ui.theme.HiddenSpaceAccent
import com.apphider.ui.theme.HiddenSpaceBgEnd
import com.apphider.ui.theme.HiddenSpaceBgStart
import com.apphider.ui.theme.HiddenSpaceCard
import com.apphider.ui.theme.HiddenSpaceCardText

/**
 * Hidden space screen that displays all hidden applications in a grid.
 * Accessible only through the disguised calculator entry.
 * Features a dark gradient background with card-style app grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenSpaceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAppList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HiddenSpaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "隐藏空间",
                        color = HiddenSpaceCardText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = HiddenSpaceCardText)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置", tint = HiddenSpaceCardText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HiddenSpaceBgStart.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = onNavigateToAppList,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HiddenSpaceAccent
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加", modifier = Modifier.padding(end = 4.dp))
                Text("添加应用")
            }
        },
        containerColor = HiddenSpaceBgStart
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(HiddenSpaceBgStart, HiddenSpaceBgEnd)
                    )
                )
        ) {
            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中…", color = HiddenSpaceCardText)
                }
            } else if (uiState.hiddenApps.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "暂无隐藏应用",
                        style = MaterialTheme.typography.headlineMedium,
                        color = HiddenSpaceCardText.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "点击下方按钮添加要隐藏的应用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HiddenSpaceCardText.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // App grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.hiddenApps,
                        key = { it.packageName }
                    ) { app ->
                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            AppGridItem(
                                appName = app.appName,
                                icon = uiState.appIcons[app.packageName],
                                onClick = { viewModel.launchApp(app.packageName) },
                                containerColor = HiddenSpaceCard
                            )
                        }
                    }
                }
            }
        }
    }
}