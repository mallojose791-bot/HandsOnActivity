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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.handsonactivity.ui.theme.HandsOnActivityTheme
import kotlin.system.exitProcess


// Theme Colors
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
fun forceCloseAppButton() {
    Button(onClick = {
        exitProcess(0)
    } ) {
        Text("Exit Application")
    }
}

@Composable
fun HandsOnActivityTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}

// Navigation
sealed class Screen {
    object Profile : Screen()
    object PersonalInfo : Screen()
    object Notifications : Screen()
    object TimeSpent : Screen()
    object Following : Screen()
    object Privacy : Screen()
    object Terms : Screen()
    object FAQ : Screen()
}

data class MenuItem(val title: String, val icon: ImageVector, val screen: Screen)

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
        // Save data when app goes to background
        saveAllData()
    }

    override fun onStop() {
        super.onStop()
        // Save data when app is stopped
        saveAllData()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Save data when app is destroyed
        saveAllData()
    }

    private fun saveAllData() {
        val sharedPreferences = getSharedPreferences("MiniProfilePrefs", Context.MODE_PRIVATE)
        // Data is already being saved in real-time, but this ensures it's persisted
        sharedPreferences.edit().apply()
    }
}

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

    // Save data when composable is disposed (app closes)
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
                        saveData() // Save before exiting
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
        ) {
            // Header - Now just shows avatar and name/email from state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(Purple, PurpleLight)))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, if (isEditing) Color.White else Color.Transparent, CircleShape)
                            .clickable(enabled = isEditing) { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            // Show user's custom image
                            Image(
                                painter = rememberAsyncImagePainter(profileImageUri),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (isEditing) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        "Edit",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else {
                            // Show default cat image from drawable
                            Image(
                                painter = painterResource(id = R.drawable.cat4),
                                contentDescription = "Default Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                            if (isEditing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        "Edit",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(email, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.9f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Form Section - Only show when editing
            if (isEditing) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        name, onNameChange, label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, "Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        email, onEmailChange, label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, "Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "Star", tint = Purple)
                                Spacer(Modifier.width(12.dp))
                                Text("Become a Publisher")
                            }
                            Switch(isPublisher, onPublisherChange)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Publisher Badge - Show when not editing and user is publisher
            if (!isEditing && isPublisher) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, "Publisher", tint = Purple)
                            Spacer(Modifier.width(12.dp))
                            Text("Publisher Account", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            } else if (!isEditing) {
                Spacer(Modifier.height(16.dp))
            }

            // Menus
            Text(
                "Account Settings",
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            MenuCard(
                listOf(
                    MenuItem("Personal Information", Icons.Default.Person, Screen.PersonalInfo),
                    MenuItem("Notifications", Icons.Default.Notifications, Screen.Notifications),
                    MenuItem("Time Spent", Icons.Default.DateRange, Screen.TimeSpent),
                    MenuItem("Following", Icons.Default.Favorite, Screen.Following)
                ),
                onNavigate
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Help & Support",
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            MenuCard(
                listOf(
                    MenuItem("Privacy Policy", Icons.Default.Lock, Screen.Privacy),
                    MenuItem("Terms & Conditions", Icons.Default.Info, Screen.Terms),
                    MenuItem("FAQ & Help", Icons.Default.Star, Screen.FAQ)
                ),
                onNavigate
            )

            Spacer(Modifier.height(16.dp))

            // Logout
            Button(
                onLogout,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, "Exit", modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationFragment(
    name: String,
    email: String,
    onBack: () -> Unit
) {
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Personal Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your basic account information",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            // Name Card
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            "Name",
                            tint = Purple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Name",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black.copy(0.6f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Email Card
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            "Email",
                            tint = Purple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Email",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black.copy(0.6f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                email,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(items: List<MenuItem>, onClick: (Screen) -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            items.forEachIndexed { i, item ->
                Row(
                    Modifier.fillMaxWidth().clickable { onClick(item.screen) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, item.title, tint = Purple, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(item.title)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Arrow", tint = Color.Black.copy(0.5f))
                }
                if (i < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Outline.copy(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalDivider(modifier: Modifier, color: Color) {
    Divider(modifier, color = color, thickness = 1.dp)
}

@Composable
fun InfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, label, tint = Purple, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(0.6f))
                Spacer(Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsFragment(onBack: () -> Unit) {
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var likesEnabled by remember { mutableStateOf(true) }
    var commentsEnabled by remember { mutableStateOf(true) }
    var followersEnabled by remember { mutableStateOf(true) }
    var messagesEnabled by remember { mutableStateOf(false) }

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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Notification Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Manage how you receive notifications",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            // Notification Methods
            Text(
                "Notification Methods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    NotificationToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        description = "Receive notifications on your device",
                        checked = pushEnabled,
                        onCheckedChange = { pushEnabled = it }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    NotificationToggleItem(
                        icon = Icons.Default.Email,
                        title = "Email Notifications",
                        description = "Receive notifications via email",
                        checked = emailEnabled,
                        onCheckedChange = { emailEnabled = it }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Activity Notifications
            Text(
                "Activity Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    NotificationToggleItem(
                        icon = Icons.Default.Favorite,
                        title = "Likes",
                        description = "When someone likes your post",
                        checked = likesEnabled,
                        onCheckedChange = { likesEnabled = it }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    NotificationToggleItem(
                        icon = Icons.Default.List,
                        title = "Comments",
                        description = "When someone comments on your post",
                        checked = commentsEnabled,
                        onCheckedChange = { commentsEnabled = it }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    NotificationToggleItem(
                        icon = Icons.Default.Person,
                        title = "New Followers",
                        description = "When someone follows you",
                        checked = followersEnabled,
                        onCheckedChange = { followersEnabled = it }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    NotificationToggleItem(
                        icon = Icons.Default.Email,
                        title = "Messages",
                        description = "When you receive a new message",
                        checked = messagesEnabled,
                        onCheckedChange = { messagesEnabled = it }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Purple,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Purple,
                checkedTrackColor = Purple.copy(0.5f)
            )
        )
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Today's Time
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Today",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF21005D)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "2h 34m",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "12 sessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF21005D).copy(0.7f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Weekly Statistics
            Text(
                "This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    TimeSpentItem("Monday", "1h 45m", 0.7f)
                    Spacer(Modifier.height(12.dp))
                    TimeSpentItem("Tuesday", "2h 15m", 0.9f)
                    Spacer(Modifier.height(12.dp))
                    TimeSpentItem("Wednesday", "3h 02m", 1.0f)
                    Spacer(Modifier.height(12.dp))
                    TimeSpentItem("Thursday", "2h 34m", 0.85f)
                    Spacer(Modifier.height(12.dp))
                    TimeSpentItem("Friday", "1h 20m", 0.55f)
                    Spacer(Modifier.height(12.dp))
                    TimeSpentItem("Saturday", "4h 10m", 1.0f)
                    Spacer(Modifier.height(12.dp))
                    TimeSpentItem("Sunday", "2h 05m", 0.7f)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Summary Stats
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Weekly Total",
                    value = "17h 11m",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Daily Average",
                    value = "2h 27m",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Total Sessions",
                    value = "84",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Longest Day",
                    value = "4h 10m",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TimeSpentItem(day: String, time: String, progress: Float) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                day,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                time,
                style = MaterialTheme.typography.bodyMedium,
                color = Purple,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Purple,
            trackColor = Purple.copy(0.2f)
        )
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Purple,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(0.6f),
                textAlign = TextAlign.Center
            )
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "People that i  Follow",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Manage your following list",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            // Following Stats
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "248",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            "Following",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF21005D).copy(0.7f)
                        )
                    }
                }
                Card(
                    Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "1.2K",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            "Followers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF21005D).copy(0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            // Following List
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    FollowingItem("Sarah Johnson", "@sarahjohnson", "2 hours ago")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    FollowingItem("Mike Chen", "@mikechen", "5 hours ago")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    FollowingItem("Emma Wilson", "@emmawilson", "1 day ago")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    FollowingItem("James Brown", "@jamesbrown", "2 days ago")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), Outline.copy(0.3f))
                    FollowingItem("Lisa Anderson", "@lisaanderson", "3 days ago")
                }
            }
        }
    }
}

@Composable
fun FollowingItem(name: String, username: String, time: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Purple.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = name,
                tint = Purple,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                username,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(0.6f)
            )
        }
        Text(
            time,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(0.5f)
        )
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Last updated: January 18, 2026",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            PrivacySection(
                title = "1. Information We Collect",
                content = "We collect information you provide directly to us, including your name, email address, profile information, and any other information you choose to provide."
            )

            PrivacySection(
                title = "2. How We Use Your Information",
                content = "We use the information we collect to provide, maintain, and improve our services, to communicate with you, and to personalize your experience."
            )

            PrivacySection(
                title = "3. Information Sharing",
                content = "We do not share your personal information with third parties except as described in this policy or with your consent."
            )

            PrivacySection(
                title = "4. Data Security",
                content = "We implement appropriate security measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction."
            )

            PrivacySection(
                title = "5. Your Rights",
                content = "You have the right to access, update, or delete your personal information at any time. You can do this through your account settings or by contacting us."
            )

            PrivacySection(
                title = "6. Cookies",
                content = "We use cookies and similar technologies to collect information about your browsing activities and to personalize your experience."
            )

            PrivacySection(
                title = "7. Children's Privacy",
                content = "Our services are not directed to children under 13. We do not knowingly collect personal information from children under 13."
            )

            PrivacySection(
                title = "8. Changes to This Policy",
                content = "We may update this privacy policy from time to time. We will notify you of any changes by posting the new policy on this page."
            )

            Spacer(Modifier.height(16.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Contact Us",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "If you have any questions about this Privacy Policy, please contact us at privacy@miniprofile.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF21005D).copy(0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(0.8f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(0.6f),
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(20.dp))
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Terms & Conditions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Last updated: January 18, 2026",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            PrivacySection(
                title = "1. Acceptance of Terms",
                content = "By accessing and using MiniProfile, you accept and agree to be bound by the terms and provision of this agreement."
            )

            PrivacySection(
                title = "2. User Accounts",
                content = "You are responsible for maintaining the confidentiality of your account and password. You agree to accept responsibility for all activities that occur under your account."
            )

            PrivacySection(
                title = "3. Content",
                content = "You retain ownership of all content you post. By posting content, you grant us a license to use, modify, and display that content in connection with our services."
            )

            PrivacySection(
                title = "4. Prohibited Activities",
                content = "You may not use our services for any illegal purposes or to violate any laws. You may not post content that is harmful, threatening, abusive, or otherwise objectionable."
            )

            PrivacySection(
                title = "5. Termination",
                content = "We may terminate or suspend your account immediately, without prior notice, for any reason, including breach of these Terms."
            )

            PrivacySection(
                title = "6. Disclaimer",
                content = "Our services are provided \"as is\" without warranties of any kind, either express or implied. We do not guarantee that our services will be uninterrupted or error-free."
            )

            PrivacySection(
                title = "7. Limitation of Liability",
                content = "We shall not be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your use of our services."
            )

            PrivacySection(
                title = "8. Changes to Terms",
                content = "We reserve the right to modify these terms at any time. Continued use of our services after changes constitutes acceptance of the modified terms."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQFragment(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ & Help") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Frequently Asked Questions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Find answers to common questions",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(0.6f)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            FAQItem(
                question = "How do I change my profile picture?",
                answer = "Tap the Edit icon on your profile, then tap on your profile picture to select a new image from your gallery."
            )

            FAQItem(
                question = "How do I update my email?",
                answer = "Go to your profile, tap the Edit icon, and update your email in the Email field. Don't forget to save your changes."
            )

            FAQItem(
                question = "Can I delete my account?",
                answer = "Yes, you can delete your account by going to Settings > Account > Delete Account. This action is permanent and cannot be undone."
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Privacy & Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            FAQItem(
                question = "Is my data secure?",
                answer = "Yes, we use industry-standard encryption and security measures to protect your personal information."
            )

            FAQItem(
                question = "Who can see my profile?",
                answer = "Your profile visibility depends on your privacy settings. You can control who sees your profile in the Privacy Settings."
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(0.6f)
            )
            Spacer(Modifier.height(12.dp))

            FAQItem(
                question = "What is a Publisher Account?",
                answer = "A Publisher Account gives you access to additional features like analytics, scheduled posts, and advanced content management tools."
            )

            FAQItem(
                question = "How do I enable notifications?",
                answer = "Go to Settings > Notifications and toggle on the types of notifications you want to receive."
            )

            Spacer(Modifier.height(24.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Email,
                        "Contact",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Still need help?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Contact our support team at support@miniprofile.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF21005D).copy(0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Info,
                    "Question",
                    tint = Purple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                answer,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(0.7f),
                lineHeight = 20.sp
            )
        }
    }
}