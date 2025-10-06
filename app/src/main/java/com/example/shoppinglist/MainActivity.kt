package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shoppinglist.components.ItemInput
import com.example.shoppinglist.components.SearchInput
import com.example.shoppinglist.components.ShoppingList
import com.example.shoppinglist.components.Title
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import kotlinx.coroutines.launch   // <-- PENTING

@OptIn(ExperimentalMaterial3Api::class) // <-- tambahkan ini
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShoppingListTheme { AppRoot() } }
    }
}

sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "Home")
    data object Profile : Screen("profile", "Profile")
    data object Settings : Screen("settings", "Settings")
}

data class BottomItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class) // <-- aman untuk komponen Material3 di sini juga
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()                    // <-- PENTING

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val showBottomBar = currentDestination?.route in listOf(Screen.Home.route, Screen.Profile.route)

    val bottomItems = listOf(
        BottomItem(Screen.Home.route, "Home") { Icon(Icons.Default.Home, contentDescription = "Home") },
        BottomItem(Screen.Profile.route, "Profile") { Icon(Icons.Default.Person, contentDescription = "Profile") },
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Menu", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        // tutup drawer setelah navigasi
                        scope.launch { drawerState.close() }   // <-- panggil dalam coroutine
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentDestination?.route) {
                                Screen.Profile.route -> "Profile"
                                Screen.Settings.route -> "Settings"
                                else -> "ShoppingList"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { // <-- in coroutine
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(items = bottomItems, navController = navController, currentDestination = currentDestination)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { FadeScreen { HomeScreen() } }
                composable(Screen.Profile.route) { FadeScreen { ProfileScreen() } }
                composable(Screen.Settings.route) { FadeScreen { SettingsScreen() } }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    items: List<BottomItem>,
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.route == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { item.icon() },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun FadeScreen(duration: Int = 300, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(duration)), exit = fadeOut(tween(duration))) {
        content()
    }
}

/* ========== Screens ========== */

@Composable
fun HomeScreen() {
    var newItemText by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val shoppingItems = remember { mutableStateListOf<String>() }
    val filteredItems by remember(searchQuery, shoppingItems) {
        derivedStateOf { if (searchQuery.isBlank()) shoppingItems else shoppingItems.filter { it.contains(searchQuery, ignoreCase = true) } }
    }
    Column(
        Modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues()).padding(horizontal = 16.dp)
    ) {
        Title()
        ItemInput(text = newItemText, onTextChange = { newItemText = it }) {
            if (newItemText.isNotBlank()) { shoppingItems.add(newItemText); newItemText = "" }
        }
        Spacer(Modifier.height(16.dp))
        SearchInput(query = searchQuery, onQueryChange = { searchQuery = it })
        Spacer(Modifier.height(16.dp))
        ShoppingList(items = filteredItems)
    }
}

@Composable
fun ProfileScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Profil Mahasiswa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ganti 'profile_photo' sesuai nama file kamu di drawable
                    Image(
                        painter = painterResource(id = R.drawable.profile_photo),
                        contentDescription = "Foto diri",
                        modifier = Modifier.size(84.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text("Nama  : Muhammad Farhan")
                        Text("NIM   : 2211522004")
                        Text("Hobi  : Gaming, Desain")
                    }
                }
                HorizontalDivider() // ganti Divider -> HorizontalDivider
                Text("TTL   : Pariaman, 18-12-2003")
                Text("Peminatan: Mobile Programming")
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Ini halaman Pengaturan", style = MaterialTheme.typography.titleMedium)
    }
}
