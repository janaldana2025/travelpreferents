package com.example.travel_preferents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector



// --- DATA MODEL
data class City(
    val id: String,
    val name: String,
    val country: String,
    val imageRes: Int,
    val description: String,
    val bestTime: String,
    val hours: String,
    val highlights: List<String>
)

// --- SAMPLE DATA
fun sampleCities() = listOf(
    City(
        id = "barcelona",
        name = "Barcelona",
        country = "España",
        imageRes = R.drawable.barcelona,
        description = "Una vibrante ciudad mediterránea conocida por su arquitectura única de Gaudí, deliciosa gastronomía y rica cultura catalana.",
        bestTime = "Mayo - Septiembre",
        hours = "24/7 para la ciudad, atracciones varían",
        highlights = listOf("Sagrada Família", "Park Güell", "Las Ramblas", "Gothic Quarter", "Camp Nou")
    ),
    City(
        id = "paris",
        name = "París",
        country = "Francia",
        imageRes = R.drawable.paris,
        description = "La ciudad de la luz: Eiffel, Louvre y paseos por el Sena.",
        bestTime = "Abril - Junio / Septiembre - Octubre",
        hours = "Atracciones con horarios variables",
        highlights = listOf("Torre Eiffel", "Louvre", "Montmartre")
    ),
    City(
        id = "roma",
        name = "Roma",
        country = "Italia",
        imageRes = R.drawable.roma,
        description = "Historia, Colosseum, Vaticano y excelente comida italiana.",
        bestTime = "Abril - Junio / Septiembre - Octubre",
        hours = "Atracciones mayormente con horario fijo",
        highlights = listOf("Coliseo", "Vaticano", "Foro Romano")
    ),
    City(
        id = "tokio",
        name = "Tokio",
        country = "Japón",
        imageRes = R.drawable.tokio,
        description = "Una mezcla de modernidad y tradición: templos, tecnología y gastronomía.",
        bestTime = "Marzo - Mayo / Octubre - Noviembre",
        hours = "Atracciones con horarios variables",
        highlights = listOf("Shibuya", "Asakusa", "Templos")
    )
)

// --- ACTIVITY
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelAppRoot()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelAppRoot() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val cities = remember { sampleCities() }
    var currentCityId by remember { mutableStateOf(cities.first().id) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(   // 👈 esto le da un fondo por defecto (Material3)
                drawerContainerColor = MaterialTheme.colorScheme.surface, // puedes poner Color.White si prefieres
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                DrawerContent(
                    cities = cities,
                    currentCityId = currentCityId,
                    onCitySelected = { cityId ->
                        currentCityId = cityId
                        scope.launch { drawerState.close() }
                        navController.navigate("city/$cityId") {
                            launchSingleTop = true
                        }
                    },
                    onInfoClick = { infoType ->
                        scope.launch { drawerState.close() }
                        navController.navigate("city/$currentCityId/$infoType") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ){
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    title = {
                        // Muestra nombre de la ciudad si estamos en la ruta city/... sino título general
                        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                        val title = if (currentRoute?.startsWith("city/") == true) {
                            cities.firstOrNull { it.id == currentCityId }?.name ?: "Ciudad"
                        } else {
                            "Guía de Viajes"
                        }
                        Text(title)
                    }
                )
            }
        ) { innerPadding ->
            NavHostContainer(navController = navController, modifier = Modifier.padding(innerPadding), cities = cities, onCityRequest = { id -> currentCityId = id })
        }
    }
}

// --- DRAWER
@Composable
fun DrawerContent(
    cities: List<City>,
    currentCityId: String,
    onCitySelected: (String) -> Unit,
    onInfoClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Guía de Viajes", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Descubre lugares increíbles", color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

        LazyColumn {
            items(cities) { city ->
                val selected = city.id == currentCityId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onCitySelected(city.id) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(city.imageRes),
                        contentDescription = city.name,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(city.name, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        Text(city.country, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        Text("Información", modifier = Modifier.padding(top = 12.dp, bottom = 8.dp), fontWeight = FontWeight.SemiBold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoTile(icon = Icons.Default.Person, label = "Perfil") { onInfoClick("profile") }
            InfoTile(icon = Icons.Default.CameraAlt, label = "Fotos") { onInfoClick("photos") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoTile(icon = Icons.Default.VideoLibrary, label = "Video") { onInfoClick("video") }
            InfoTile(icon = Icons.Default.Public, label = "Web") { onInfoClick("web") }
        }
    }
}

@Composable
fun RowScope.InfoTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)   // ✅ ahora sí funciona porque estamos en un RowScope
            .height(56.dp)
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(icon, contentDescription = label)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}

// --- NAV HOST
@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier = Modifier, cities: List<City>, onCityRequest: (String) -> Unit) {
    NavHost(navController = navController, startDestination = "inicio", modifier = modifier) {
        composable("inicio") {
            InicioScreen()
        }
        composable("cities") {
            CitiesListScreen(cities = cities, onCityClick = { id ->
                onCityRequest(id)
                navController.navigate("city/$id")
            })
        }
        composable("city/{cityId}") { back ->
            val cityId = back.arguments?.getString("cityId") ?: cities.first().id
            onCityRequest(cityId)
            val city = cities.first { it.id == cityId }
            CityScreen(city = city)
        }
        // info subroutes
        composable("city/{cityId}/profile") { back ->
            val cityId = back.arguments?.getString("cityId") ?: cities.first().id
            val city = cities.first { it.id == cityId }
            GenericInfoScreen(title = "${city.name} - Perfil", text = city.description)
        }
        composable("city/{cityId}/photos") { back ->
            val cityId = back.arguments?.getString("cityId") ?: cities.first().id
            val city = cities.first { it.id == cityId }
            GenericInfoScreen(title = "${city.name} - Fotos", text = "Galería de fotos de ${city.name} (ejemplo).")
        }
        composable("city/{cityId}/video") { back ->
            val cityId = back.arguments?.getString("cityId") ?: cities.first().id
            GenericInfoScreen(title = "Video", text = "Videos relacionados")
        }
        composable("city/{cityId}/web") { back ->
            GenericInfoScreen(title = "Web", text = "Enlaces y web oficial")
        }
    }
}

// --- SCREENS
@Composable
fun InicioScreen() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopStart) {
        Text("Bienvenido a Travel Preferents")
    }
}

@Composable
fun CitiesListScreen(cities: List<City>, onCityClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        items(cities) { city ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onCityClick(city.id) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(city.imageRes), contentDescription = city.name, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(city.name, fontWeight = FontWeight.Bold)
                        Text(city.country, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun GenericInfoScreen(title: String, text: String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text)
    }
}

@Composable
fun CityScreen(city: City) {
    // Main vertical scroll
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
        ) {
            val painter: Painter = painterResource(city.imageRes)
            Image(painter = painter, contentDescription = city.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            // gradient overlay bottom
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA000000)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
            )

            // Title & location at bottom-left
            Column(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
            ) {
                Text(city.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = "Ubicación", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(city.country, color = Color.White, fontSize = 13.sp)
                }
            }

            // Action buttons bottom-right
            Row(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
            ) {
                // share
                Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 4.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 4.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Agregar", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        // Content
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            InfoCard(title = "Acerca de ${city.name}", content = city.description, icon = Icons.Default.Info)
            InfoCard(title = "Horarios de Visita", content = city.hours, icon = Icons.Default.AccessTime)
            InfoCard(title = "Mejor Época", content = city.bestTime, icon = Icons.Default.CalendarToday)
            HighlightCard(title = "Lugares Destacados", items = city.highlights)
        }
    }
}

@Composable
fun InfoCard(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(content, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun HighlightCard(title: String, items: List<String>) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "destacados")
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // simple wrapping: multiple rows if needed (2 per row)
            val chunked = items.chunked(3)
            chunked.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    row.forEach { tag ->
                        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 1.dp, modifier = Modifier.padding(end = 8.dp)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCity() {
    CityScreen(sampleCities().first())
}
