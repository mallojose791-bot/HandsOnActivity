package com.example.handsonactivity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.handsonactivity.ui.theme.HandsOnActivityTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.system.exitProcess

// ============= RETROFIT API SETUP =============

// Data Models
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String
)

// API Interface
interface JsonPlaceholderApi {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("users")
    suspend fun getUsers(): List<User>
}

// Retrofit Instance
object RetrofitInstance {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    val api: JsonPlaceholderApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JsonPlaceholderApi::class.java)
    }
}

// UiState sealed class
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// ViewModel
class ApiViewModel : ViewModel() {
    private val _postsState = mutableStateOf<UiState<List<Post>>>(UiState.Idle)
    val postsState: State<UiState<List<Post>>> = _postsState

    private val _usersState = mutableStateOf<UiState<List<User>>>(UiState.Idle)
    val usersState: State<UiState<List<User>>> = _usersState

    fun fetchPosts() {
        viewModelScope.launch {
            _postsState.value = UiState.Loading
            try {
                val posts = RetrofitInstance.api.getPosts()
                _postsState.value = UiState.Success(posts)
            } catch (e: Exception) {
                _postsState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _usersState.value = UiState.Loading
            try {
                val users = RetrofitInstance.api.getUsers()
                _usersState.value = UiState.Success(users)
            } catch (e: Exception) {
                _usersState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

// ============= THEME COLORS =============

private val Purple = Color(0xFF6B46C1)
private val PurpleLight = Color(0xFF9C27B0)
private val Background = Color(0xFFFFFBFE)
private val Surface = Color(0xFFFFFBFE)
private val PrimaryContainer = Color(0xFFE8DEF8)
private val SurfaceVariant = Color(0xFFE7E0EC)
private val OnSurfaceVariant = Color(0xFF49454F)
private val Outline = Color(0xFF79747E)
private val ErrorRed = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = Purple,
    secondary = PurpleLight,
    background = Background,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Color(0xFF21005D),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = ErrorRed
)

@Composable
fun HandsOnActivityTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}

// ============= NAVIGATION =============

sealed class Screen {
    object Profile : Screen()
    object PersonalInfo : Screen()
    object Notifications : Screen()
    object TimeSpent : Screen()
    object Following : Screen()
    object Privacy : Screen()
    object Terms : Screen()
    object FAQ : Screen()
    object ApiData : Screen()
}

data class MenuItem(val title: String, val icon: ImageVector, val screen: Screen)

// ============= MAIN ACTIVITY =============

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandsOnActivityTheme {
                MiniProfileApp()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveAllData()
    }

    override fun onStop() {
        super.onStop()
        saveAllData()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveAllData()
    }

    private fun saveAllData() {
        val sharedPreferences = getSharedPreferences("MiniProfilePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply()
    }
}

// ============= MAIN APP =============

@Composable
fun MiniProfileApp() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MiniProfilePrefs", Context.MODE_PRIVATE) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Profile) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load saved data
    var name by remember { mutableStateOf(sharedPreferences.getString("name", "Jose Mallo") ?: "Jose Mallo") }
    var email by remember { mutableStateOf(sharedPreferences.getString("email", "Mallojose791@gmail.com") ?: "Mallojose791@gmail.com") }
    var profileImageUri by remember {
        mutableStateOf<Uri?>(
            sharedPreferences.getString("profileImage", null)?.let { Uri.parse(it) }
        )
    }
    var isPublisher by remember { mutableStateOf(sharedPreferences.getBoolean("isPublisher", false)) }

    // Save data function
    fun saveData() {
        sharedPreferences.edit().apply {
            putString("name", name)
            putString("email", email)
            putString("profileImage", profileImageUri?.toString())
            putBoolean("isPublisher", isPublisher)
            apply()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            saveData()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            Screen.Profile -> ProfileScreen(
                name, email, profileImageUri, isPublisher,
                { name = it; saveData() },
                { email = it; saveData() },
                { profileImageUri = it; saveData() },
                { isPublisher = it; saveData() },
                { currentScreen = it },
                { showLogoutDialog = true }
            )
            Screen.PersonalInfo -> PersonalInformationFragment(
                name = name,
                email = email,
                onBack = { currentScreen = Screen.Profile }
            )
            Screen.Notifications -> NotificationsFragment { currentScreen = Screen.Profile }
            Screen.TimeSpent -> TimeSpentFragment { currentScreen = Screen.Profile }
            Screen.Following -> FollowingFragment { currentScreen = Screen.Profile }
            Screen.Privacy -> PrivacyPolicyFragment { currentScreen = Screen.Profile }
            Screen.Terms -> TermsFragment { currentScreen = Screen.Profile }
            Screen.FAQ -> FAQFragment { currentScreen = Screen.Profile }
            Screen.ApiData -> ApiDataScreen { currentScreen = Screen.Profile }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        saveData()
                        exitProcess(0)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ============= API DATA SCREEN (WITH RETROFIT & UISTATE) =============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiDataScreen(onBack: () -> Unit) {
    val viewModel: ApiViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }

    // Fetch data when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchPosts()
        viewModel.fetchUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PrimaryContainer,
                contentColor = Color(0xFF21005D)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Posts") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Users") }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> PostsContent(viewModel.postsState.value)
                1 -> UsersContent(viewModel.usersState.value)
            }
        }
    }
}

@Composable
fun PostsContent(uiState: UiState<List<Post>>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is UiState.Idle -> {
                Text("Ready to load posts", color = Color.Gray)
            }
            is UiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Purple)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading posts...", color = Color.Gray)
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data) { post ->
                        PostItem(post)
                    }
                }
            }
            is UiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = ErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Error: ${uiState.message}",
                        color = ErrorRed,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UsersContent(uiState: UiState<List<User>>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is UiState.Idle -> {
                Text("Ready to load users", color = Color.Gray)
            }
            is UiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Purple)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading users...", color = Color.Gray)
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.data) { user ->
                        UserItem(user)
                    }
                }
            }
            is UiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = ErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Error: ${uiState.message}",
                        color = ErrorRed,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Purple.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userId.toString(),
                        color = Purple,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "User ${post.userId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black.copy(0.6f)
                    )
                    Text(
                        text = "Post #${post.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(0.4f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(0.7f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = user.name,
                    tint = Purple,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Purple
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color.Black.copy(0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(0.6f)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color.Black.copy(0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = user.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(0.6f)
                    )
                }
            }
        }
    }
}

// ============= PROFILE SCREEN =============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String, email: String, profileImageUri: Uri?, isPublisher: Boolean,
    onNameChange: (String) -> Unit, onEmailChange: (String) -> Unit,
    onImageChange: (Uri?) -> Unit, onPublisherChange: (Boolean) -> Unit,
    onNavigate: (Screen) -> Unit, onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageChange(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                ),
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer)
                    .clickable(enabled = isEditing) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(80.dp),
                        tint = Purple
                    )
                }
            }

            if (isEditing) {
                Text(
                    "Tap image to change",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Name Field
            TextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Email Field
            TextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Publisher Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Publisher Account", modifier = Modifier.weight(1f))
                Switch(
                    checked = isPublisher,
                    onCheckedChange = onPublisherChange,
                    enabled = isEditing
                )
            }

            Spacer(Modifier.height(20.dp))

            // Menu Items
            menuItems.forEach { item ->
                MenuItemRow(item) { onNavigate(item.screen) }
            }

            Spacer(Modifier.height(20.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

val menuItems = listOf(
    MenuItem("Personal Information", Icons.Default.Person, Screen.PersonalInfo),
    MenuItem("Notifications", Icons.Default.Notifications, Screen.Notifications),
    MenuItem("Time Spent", Icons.Default.Timer, Screen.TimeSpent),
    MenuItem("Following", Icons.Default.Favorite, Screen.Following),
    MenuItem("Privacy Policy", Icons.Default.Lock, Screen.Privacy),
    MenuItem("Terms & Conditions", Icons.Default.Info, Screen.Terms),
    MenuItem("FAQ", Icons.Default.Help, Screen.FAQ),
    MenuItem("API Data", Icons.Default.Cloud, Screen.ApiData)
)

@Composable
fun MenuItemRow(item: MenuItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, contentDescription = item.title, tint = Purple, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Go", tint = Color.Gray)
    }
}

// ============= SCREEN FRAGMENTS =============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationFragment(name: String, email: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Name: $name", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
            Text("Email: $email", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Notification settings will appear here", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSpentFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Time Spent") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Time spent analytics will appear here", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Following") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Following list will appear here", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Your privacy is important to us. This privacy policy explains how we collect and use your information.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Terms & Conditions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("By using this application, you agree to these terms and conditions.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryContainer,
                    titleContentColor = Color(0xFF21005D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Frequently Asked Questions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Q: How do I update my profile?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("A: Tap the edit button on your profile screen.", style = MaterialTheme.typography.bodySmall)
        }
    }
}