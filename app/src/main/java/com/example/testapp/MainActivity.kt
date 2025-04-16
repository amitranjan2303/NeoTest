package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.testapp.dto.NatureItem
import com.example.testapp.ui.theme.NatureListAppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NatureListAppTheme {
                NatureListScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NatureListAppTheme {
        NatureListScreen()
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NatureListScreen() {
    var currentPage by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberLazyListState() // Track scroll position

    val currentHeader = headerImages[currentPage]
    val currentItems = pageData[currentHeader] ?: emptyList()
    val filteredItems = currentItems.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(
            searchQuery,
            ignoreCase = true
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                coroutineScope.launch { sheetState.hide() }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.4f),
            tonalElevation = 8.dp
        ) {
            BottomSheetStats(currentItems)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                    coroutineScope.launch {
                        if (sheetState.isVisible) {
                            sheetState.hide()
                            showBottomSheet = false
                        } else {
                            showBottomSheet = true
                            sheetState.show()
                        }
                    }
                },
                containerColor = Color(0xFF87CEEB) // Sky blue color
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "FAB Icon")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header section (not sticky)
            item {
                Column {
                    if (currentHeader.second) {
                        Image(
                            painter = painterResource(id = currentHeader.first as Int),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .clickable {
                                    currentPage = (currentPage + 1) % headerImages.size
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = currentHeader.first,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .clickable {
                                    currentPage = (currentPage + 1) % headerImages.size
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        headerImages.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .padding(2.dp)
                                    .background(
                                        if (index == currentPage) Color(0xFF87CEEB) else Color.Gray,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            // Sticky search bar
            stickyHeader {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // List items
            items(filteredItems) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text(item.subtitle) },
                    leadingContent = {
                        if (item.isLocal) {
                            val resId = when (item.imageUrl) {
                                "forest_item_1" -> R.drawable.n_one
                                "forest_item_2" -> R.drawable.n_two
                                else -> R.drawable.ic_launcher_foreground
                            }
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color(0xFFBEE1C1)),
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}


@Composable
fun BottomSheetStats(items: List<NatureItem>) {
//    val charCounts = items.flatMap { it.title +""+ it.subtitle }
    val charCounts = items
        .flatMap { (it.title + it.subtitle).toList() } // Converts to List<Char>
        .filter { it.isLetter() }
        .groupingBy { it.lowercaseChar() }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Items Count: ${items.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        charCounts.forEach { (char, count) ->
            Text("$char = $count")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomSheetStatsPreview() {
    NatureListAppTheme {
        BottomSheetStats(
            items = listOf(
                NatureItem("Apple", "Fresh red", "", false),
                NatureItem("Banana", "Tropical fruit", "", false),
                NatureItem("Orange", "Citrus zing", "", false),
                NatureItem("Blueberry", "Tiny & sweet", "", false)
            )
        )
    }
}


val headerImages = listOf(
    Pair(R.drawable.n_one, true),
    Pair(
        "https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
        false
    ),
    Pair("https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg", false)
)

val pageData = mapOf(
    headerImages[0] to listOf(
        NatureItem("Forest Path", "Misty trees", "forest_item_1", isLocal = true),
        NatureItem("Woodland Walk", "Green paradise", "forest_item_2", isLocal = true)
    ),
    headerImages[1] to listOf(
        NatureItem(
            "Golden Dunes",
            "Sandy escape",
            "https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
        ),
        NatureItem(
            "Cactus Valley",
            "Desert bloom",
            "https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
        )
    ),
    headerImages[2] to listOf(
        NatureItem(
            "Snowy Pines",
            "Chilly silence",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ),
        NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ),
        NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ),
        NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ), NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        ),
        NatureItem(
            "Frozen Lake",
            "Crystal ice",
            "https://images.pexels.com/photos/33109/fall-autumn-red-season.jpg"
        )
    )
)

