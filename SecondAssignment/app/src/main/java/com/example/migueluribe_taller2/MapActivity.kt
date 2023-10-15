package com.example.migueluribe_taller2

import android.Manifest
import android.content.Context
import android.os.Handler
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.migueluribe_taller2.databinding.ActivityMapBinding
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date

class MapActivity : AppCompatActivity(), SensorEventListener {
    // Acà hago la inicializaciòn de todas las variables necesarias para esta interfaz.
    // Acà decalro la distancia mínima de 30 metros que nos pide el taller para la actualizacoòn del archivo json.
    private val MIN_DISTANCE_FOR_UPDATE = 30.0
    // Y acà se encuentran el resto de inicializaciòn de variables necesarias para mi aplicaciòn.
    private val JSON_FILE_NAME = "location_records.json"
    private val TAG = "MapActivity"
    private var lastLocation: Location? = null
    private lateinit var showRouteButton: Button
    private var jsonFile: File? = null
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: MapView
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private var currentMarker: Marker? = null
    private lateinit var locationManager: LocationManager
    private val REQUEST_LOCATION_PERMISSION = 1
    private val geocoder: Geocoder by lazy { Geocoder(this) }
    private lateinit var addressEditText: EditText
    private lateinit var searchButton: Button
    private val userLocationMarkers = ArrayList<Marker>()
    private val searchMarkers = ArrayList<Marker>()
    private lateinit var roadManager: RoadManager
    private var roadOverlay: Polyline? = null
    private val bogota = GeoPoint(4.62, -74.07)

    //Acà me encargo dejar todo correctamente configurado e iniciado para esta interfaz.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Acà cargo la configuraciòn dentro del mapa.
        Configuration.getInstance().load(
            this,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        )
        // Por otra parte acà configuro el mapa, establezco el tipo de fuente de celesto y habilito la funcionalidad
        // multitouch, ademàs de que también agrego un evento de superposición al mapa.
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.overlays.add(createOverlayEvents())
        // Acà Inicializo el sensor de luz y registro la actividad como oyente de eventos del sensor de luz.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        // Inicializo elementos de la interfaz de usuario como 'addressEditText' y 'searchButton',
        // y configuro un controlador de clic para el botón de búsqueda.
        addressEditText = findViewById(R.id.addressEditText)
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val address = addressEditText.text.toString()
            searchLocation(address)
        }
        // Finalmente aquì configuro un administrador de carreteras, ajusto el zoom del mapa y obtengo la
        // ubicación del usuario si es que ya tiene permiso mi app para ello, ademàs, preparo el botón "showRouteButton" para mostrar
        // la ruta basada en registros de ubicación cuando se hace clic.
        roadManager = OSRMRoadManager(this, "ANDROID")
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        showRouteButton = findViewById(R.id.showRouteButton)
        jsonFile = File(filesDir, JSON_FILE_NAME)
        showRouteButton.setOnClickListener {
            // Acà llamo a la función para mostrar la ruta basada en los registros de la ubicaciòn ubicación.
            showLocationRoute()
        }
    }

    // Este método se llama cuando la actividad pasa a primer plano y realizo las siguientes acciones:
        // 1. Registro la actividad como oyente de eventos del sensor de luz.
        // 2. Si tiene permiso para acceder a la ubicación, configuro el servicio de ubicación para recibir actualizaciones de ubicación constantemente y muestro la ubicación del usuario en el mapa.
        // 3. Verifico si se ha realizado una búsqueda de ubicación y, si es así, dibujo la ruta desde la ubicación actual hasta el punto de búsqueda.
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,   // Tiempo mínimo entre actualizaciones en milisegundos (1 segundo en total)
                10.0f,  // Distancia mínima entre actualizaciones en metros
                locationListener
            )
            map.onResume()
            map.controller.setZoom(18.0)
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                map.controller.animateTo(userGeoPoint)
            } else {
                // Acà preferì no decir nada, simplemente no cargo el mapa si no existe la posibilidad de acceder a la ubicaciòn
                // Pues la propia aplicaciòn es la que se encarga de pedir permisos si es que no los tiene o la ubicaciòn esta
                // desactivada.
            }
            showUserLocation()
            // Acà verifico si hay un destino en la barra de búsqueda (si se ha presionado en el mapa o buscado en la barra de busqueda).
            if (searchMarkers.isNotEmpty()) {
                val destination = searchMarkers.firstOrNull()?.position
                if (destination != null) {
                    val userLocation = userLocationMarkers.firstOrNull()?.position
                    if (userLocation != null) {
                        drawRoute(userLocation, destination)
                    }
                }
            }
        } else {
            map.onResume()
            map.controller.setZoom(18.0)
            requestLocationPermission()
        }
    }

    // Esta función se llama para solicitar permiso de ubicación al usuario y muestra un cuadro de diálogo
    // explicando la necesidad del permiso y permite al usuario otorgarlo o denegarlo si asì lo desea.
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setTitle("Permiso de ubicación necesario")
                .setMessage("La aplicación necesita acceder a su ubicación para mostrar el mapa.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    // Acà decidi que no pasa nada si el usuario decide cancelar el permiso,
                    // pues ya es cuestiòn de èl aceptaro no hacerlo..
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Este método se llama cuando se obtiene una respuesta a una solicitud de permisos, ademàs, compruebo si
    // el usuario otorgó o denegó el permiso de ubicación y actúa en consecuencia.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Acà el usuario aceptò conceder el permiso y usa su ubicaciòn en consecuencia.
                    onResume()
                } else {
                    // El usuario denegó el permiso, preferì no hacer nada màs si se niega el permiso..
                }
            }
        }
    }

    // Este método se llama cuando cambian los valores del sensor de luz y ajusta el brillo del mapa
    // en función de la intensidad de la luz ambiente.
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            // Acà establecì 80 para probarlo normalmente desde mi celular ya qye no uso emulador y allì usarìa valores màs altos.
            val threshold = 80.0
            if (lightValue < threshold) {
                // Reduce el brillo del mapa (invierte realmente los colores).
                map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
            } else {
                // Restablece el brillo del mapa a su estado original blanquito.
                map.overlayManager.tilesOverlay.setColorFilter(null)
            }
        }
    }

    // Esta función actualiza la ruta en el mapa entre dos puntos geográficos.
    private fun updateRoute(start: GeoPoint, finish: GeoPoint) {
        var routePoints = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)
        val road = roadManager.getRoad(routePoints)
        Log.i("MapsApp", "Route length: " + road.mLength + " klm")
        Log.i("MapsApp", "Duration: " + road.mDuration / 60 + " min")
        if (map != null) {
            if (roadOverlay != null) {
                map.overlays.remove(roadOverlay)
            }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay!!.outlinePaint.color = Color.CYAN
            roadOverlay!!.outlinePaint.strokeWidth = 10F
            map.overlays.add(roadOverlay)
            map.invalidate()
        }
    }

    //  Acà lo que hago es poner a un oyente de ubicación que responde a cambios en la ubicación del usuario y actualiza
    //  la ubicación del usuario en el mapa, ademàs, guarda registros de ubicación y dibuja la ruta si es necesario.
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            val geoPoint = GeoPoint(latitude, longitude)
            userLocationMarkers.forEach { map.overlays.remove(it) }
            userLocationMarkers.clear()
            val address = "Ubicación Actual"
            val userLocationMarker = createMarker(geoPoint, address, R.drawable.puntero1)
            userLocationMarkers.add(userLocationMarker)
            map.overlays.add(userLocationMarker)
            // Acà compruebo si hay un movimiento significativo.
            if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location)
                if (distance > MIN_DISTANCE_FOR_UPDATE) {
                    // Se ha detectado un movimiento de más de 30 metros.
                    saveLocationRecord(location)
                }
            }
            // Acà se actualiza la ubicación anterior.
            lastLocation = location
            // Por acà se actualiza la ruta desde la ubicación actual al punto de búsqueda (si hay uno).
            if (searchMarkers.isNotEmpty()) {
                val searchMarker = searchMarkers.first()
                val searchGeoPoint = searchMarker.position
                updateRoute(geoPoint, searchGeoPoint)
            }
        }
    }

    // Acà crep un objeto MapEventsOverlay que lo utilizo para manejar eventos de toque largo en el mapa.
    private fun createOverlayEvents(): MapEventsOverlay {
        return MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }
            override fun longPressHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    longPressOnMap(p)
                }
                return true
            }
        })
    }

    // A esta función la llamo cuando realizo un toque largo en el mapa, permito al usuario
    // seleccionar un punto en el mapa y realiza acciones como buscar la dirección, actualizar
    // la ruta y mostrar información relacionada.
    private fun longPressOnMap(p: GeoPoint) {
        currentMarker?.title = ""
        val addressText = findAddress(p)
        val titleText: String = addressText ?: ""
        // Crear un nuevo marcador o actualizar el marcador existente
        if (currentMarker == null) {
            currentMarker = createMarker(p, titleText, R.drawable.puntero2)
            searchMarkers.add(currentMarker!!)
            map.overlays.add(currentMarker)
        } else {
            currentMarker?.title = titleText
            currentMarker?.position = p
        }
        val userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (userLocation != null) {
            val userGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
            val distance = calculateDistance(userGeoPoint, p)
            val distanceMessage = "Distancia total entre puntos: $distance km"
            Toast.makeText(this, distanceMessage, Toast.LENGTH_SHORT).show()
        }
        val address = findAddress(p)
        val snippet: String = address ?: ""
        searchMarkers.forEach { map.overlays.remove(it) }
        searchMarkers.clear()
        val marker = createMarker(p, snippet, R.drawable.puntero2)
        searchMarkers.add(marker)
        map.overlays.add(marker)
        // Acà verifico si tengo permiso de ubicación o no.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                updateRoute(userGeoPoint, p)
            }
        } else {
            requestLocationPermission()
        }
    }

    // Esta función simplemente busca una dirección a partir de las coordenadas geográficas.
    private fun findAddress(latLng: LatLng): String? {
        val addresses: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) ?: emptyList()
        if (addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            return address.getAddressLine(0)
        }
        return null
    }

    // Acà simplemente calculo la distancia en kilómetros entre dos puntos geográficos utilizando
    // la fórmula de la distancia haversine.
    private fun calculateDistance(start: GeoPoint, finish: GeoPoint): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en kilómetros
        val dLat = Math.toRadians(finish.latitude - start.latitude)
        val dLng = Math.toRadians(finish.longitude - start.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(finish.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    //  Acà creo y devuelvo un marcador en el mapa con la posición, título e icono especificados.
    private fun createMarker(p: GeoPoint, title: String, iconID: Int): Marker {
        val marker = Marker(map)
        marker.title = title
        val myIcon = ContextCompat.getDrawable(this, iconID)
        marker.icon = myIcon
        marker.position = p
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        return marker
    }

    //  Esta función busca una dirección a partir de las coordenadas geográficas,
    //  esta es una segunda funciòn de este estilo, lo hice asì para hacer pruebas.
    private fun findAddress(geoPoint: GeoPoint): String? {
        val addresses: List<Address> = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1) ?: emptyList()
        if (addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            return address.getAddressLine(0)
        }
        return null
    }

    // En esta función se busca una dirección a partir de una cadena de dirección proporcionada por el
    // usuario, luego actualizo la ubicación en el mapa y muestro la ruta desde la ubicación actual al destino.
    private fun searchLocation(address: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (userLocation != null) {
                val userGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
                val geocodeResults = geocoder.getFromLocationName(address, 1)
                if (geocodeResults != null && geocodeResults.isNotEmpty() && geocodeResults[0] != null) {
                    val foundAddress = geocodeResults[0]!!
                    val latitude = foundAddress.latitude
                    val longitude = foundAddress.longitude
                    val geoPoint = GeoPoint(latitude, longitude)
                    // Acà asigno la dirección como título del marcador.
                    val addressAsTitle = foundAddress.getAddressLine(0)
                    // Acà llamo a la drawRoute antes de agregar el nuevo marcador.
                    drawRoute(userGeoPoint, geoPoint)
                    searchMarkers.forEach { map.overlays.remove(it) }
                    searchMarkers.clear()
                    val marker = createMarker(geoPoint, addressAsTitle, R.drawable.puntero2)
                    searchMarkers.add(marker)
                    map.overlays.add(marker)
                    val distance = calculateDistance(userGeoPoint, geoPoint)
                    val distanceMessage = "Distancia total entre puntos: $distance km"
                    Toast.makeText(this, distanceMessage, Toast.LENGTH_SHORT).show()
                    map.controller.animateTo(geoPoint)
                } else {
                    Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si es que no tengo el permiso, acà lo pido.
            requestLocationPermission()
        }
    }

    // Acà simplemente mestro la ubicación del usuario en el mapa si es que tengo permiso.
    private fun showUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                val userLocationMarker = createMarker(userGeoPoint, "Mi Ubicación", R.drawable.puntero1)
                userLocationMarkers.add(userLocationMarker)
                map.overlays.add(userLocationMarker)
            }
        }
    }

    // Acà dibujo una ruta en el mapa entre dos puntos geográficos.
    fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        var routePoints = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)
        val road = roadManager.getRoad(routePoints)
        Log.i("MapsApp", "Route length: " + road.mLength + " klm")
        Log.i("MapsApp", "Duration: " + road.mDuration / 60 + " min")
        if (map != null) {
            if (roadOverlay != null) {
                map.overlays.remove(roadOverlay) // Elimino a la ruta anterior
            }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay!!.outlinePaint.color = Color.CYAN
            roadOverlay!!.outlinePaint.strokeWidth = 10F
            map.overlays.add(roadOverlay) // Agrego la nueva ruta
        }
    }

    // Acà guardo registros de ubicación en el archivo JSON necesario para este taller.
    private fun saveLocationRecord(location: Location) {
        try {
            val locationRecord = JSONObject()
            locationRecord.put("latitude", location.latitude)
            locationRecord.put("longitude", location.longitude)
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            locationRecord.put("timestamp", currentTime)
            var jsonArray = JSONArray()
            val file = File(filesDir, JSON_FILE_NAME)
            if (file.exists()) {
                // Acà logro leer el contenido existente del archivo .json.
                val jsonStr = FileReader(file).readText()
                jsonArray = JSONArray(jsonStr)
            }
            jsonArray.put(locationRecord)
            // Finalmente aca voy guardando el contenido actualizado en el archivo .json.
            FileWriter(file).use { it.write(jsonArray.toString()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Acà leo y muestro el contenido del archivo .json que contiene registros de ubicación en un cuadro de diálogo.
        // CABE ACLARAR QUE ESTA FUNCIÒN SOLO LA UTILICÈ PARA COMPROBAR QUE SE GUARDARA TODO EN EL ARCHIVO .JSON Y NO ES
        // REALMENTE RELEVANTE PARA EL USO FINAL DE LA APLICACIÒN.
    private fun showJsonContents() {
        val file = File(filesDir, JSON_FILE_NAME)
        if (file.exists()) {
            try {
                var jsonArray = JSONArray()  // Acà declaro un jsonArray como una variable mutable.
                val jsonStr = file.readText()
                jsonArray = JSONArray(jsonStr)  // Acà asigno  el contenido del archivo .json a un jsonArray.
                for (i in 0 until jsonArray.length()) {
                    val locationRecord = jsonArray.getJSONObject(i)
                    val latitude = locationRecord.getDouble("latitude")
                    val longitude = locationRecord.getDouble("longitude")
                    val timestamp = locationRecord.getString("timestamp")
                    // Acà miestro los datos como tal de cada una de las lineas del archivo.
                    val message = "Latitud: $latitude\nLongitud: $longitude\nTimestamp: $timestamp"
                    showAlertDialog("Registro de Ubicación", message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Por si es que el archivo JSON no existe, muestro un mensaje.
            showAlertDialog("Archivo no encontrado", "No se encontró el archivo JSON.")
        }
    }

    // Acà muestro la ruta en el mapa basada en los registros de ubicación guardados en el archivo .json.
    private fun showLocationRoute() {
        if (jsonFile?.exists() == true) {
            // Leo el contenido del archivo .json.
            val jsonStr = FileReader(jsonFile).readText()
            try {
                val jsonArray = JSONArray(jsonStr)
                val routePoints = ArrayList<GeoPoint>()
                for (i in 0 until jsonArray.length()) {
                    val locationRecord = jsonArray.getJSONObject(i)
                    val latitude = locationRecord.getDouble("latitude")
                    val longitude = locationRecord.getDouble("longitude")
                    // Agrego la ubicación a la lista de puntos de la ruta.
                    routePoints.add(GeoPoint(latitude, longitude))
                }
                if (routePoints.size >= 2) {
                    // Acà por lo que leì estoy creando una "polilínea" que conecte a todos los puntos de la ruta del archivo .json.
                    val routePolyline = Polyline()
                    routePolyline.setPoints(routePoints)
                    routePolyline.color = Color.YELLOW  // Cambiar el color a amarillo
                    routePolyline.width = 5.0f
                    // Acà agrego la polilínea al mapa
                    map.overlays.add(routePolyline)
                    map.invalidate()
                    // Ajusto el zoom para mostrar toda la ruta
                    map.zoomToBoundingBox(routePolyline.bounds, true)
                    // Para hacer aun màs funcional todo acà programè la eliminación de la ruta después de 5 segundos de su apariciòn-
                    Handler().postDelayed({
                        if (map.overlays.contains(routePolyline)) {
                            map.overlays.remove(routePolyline)
                            map.invalidate()
                            // Toast.makeText(this, "La ruta se ha eliminado", Toast.LENGTH_SHORT).show()
                        }
                    }, 5000) // 5000 ms = 5 segundos
                } else {
                    Toast.makeText(this, "No hay suficientes registros de ubicación para mostrar una ruta.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Y si es que el archivo .json no existe, muestra un mensaje apropiado
            showAlertDialog("Archivo no encontrado", "No se encontró el archivo JSON.")
        }
    }

    // Acà se muestra un cuadro de diálogo con un título y un mensaje.
    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    // Este es simplemente un método de la interfaz SensorEventListener que se llama cuando cambia
    // la precisión del sensor de luminosidad, era necesario crearla para usarla aunque no tenga
    // realmente un contenido.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}