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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apphider.ui.components.AppGridItem
import com.apphider.ui.theme.HiddenAccent
import com.apphider.ui.theme.HiddenBgEnd
import com.apphider.ui.theme.HiddenBgStart
import com.apphider.ui.theme.HiddenCardText

/**
 * Hidden space screen — dark, premium, glassmorphism design.
 * Displays all hidden applications in a grid with card-style items.
 * Accessible only through the disguised calculator entry.
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
                        fontWeight = FontWeight.SemiBold,
                        color = HiddenCardText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = HiddenCardText)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置", tint = HiddenCardText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HiddenBgStart.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = onNavigateToAppList,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HiddenAccent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加",
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    "添加应用",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = HiddenBgStart
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HiddenBgStart, HiddenBgEnd)
                    )
                )
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中…", color = HiddenCardText.copy(alpha = 0.6f))
                }
            } else if (uiState.hiddenApps.isEmpty()) {
                // Elegant empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🔒",
                        fontSize = 56.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无隐藏应用",
                        style = MaterialTheme.typography.titleLarge,
                        color = HiddenCardText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击下方按钮添加要隐藏的应用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HiddenCardText.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // App grid with glassmorphism cards
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp
                    ),
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
                            enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                            exit = scaleOut(targetScale = 0.8f) + fadeOut()
                        ) {
                            AppGridItem(
                                appName = app.appName,
                                icon = uiState.appIcons[app.packageName],
                                onClick = { viewModel.launchApp(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}