package com.kmhinfratech.safetytaskmanager

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var kmlLayer: KmlLayer? = null
    private var overlayListener: ListenerRegistration? = null
    private val projectList = mutableListOf<Project>()
    private lateinit var projectAdapter: ArrayAdapter<String>
    private val dynamicOverlays = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        setupProjectSpinner()
    }

    private fun setupProjectSpinner() {
        val spinner = findViewById<Spinner>(R.id.projectSpinner)
        projectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf())
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = projectAdapter

        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { userDoc ->
                val assignedProjectIds = userDoc.get("projectIds") as? List<String>
                if (assignedProjectIds.isNullOrEmpty()) return@addOnSuccessListener

                db.collection("projects")
                    .whereIn(FieldPath.documentId(), assignedProjectIds)
                    .get()
                    .addOnSuccessListener { docs ->
                        projectList.clear()
                        val names = mutableListOf<String>()
                        for (doc in docs) {
                            val p = Project(
                                doc.id,
                                doc.getString("name") ?: "Unknown",
                                doc.getString("kmlUrl") ?: "",
                                doc.getDouble("defaultLat") ?: 24.0,
                                doc.getDouble("defaultLng") ?: 55.0
                            )
                            projectList.add(p)
                            names.add(p.name)
                        }
                        projectAdapter.clear()
                        projectAdapter.addAll(names)
                    }
            }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (::mMap.isInitialized && projectList.isNotEmpty()) {
                    updateMapForProject(projectList[pos])
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
    }

    private fun updateMapForProject(project: Project) {
        val center = LatLng(project.defaultLat, project.defaultLng)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))

        if (project.kmlUrl.isNotEmpty()) {
            downloadKml(project.id, project.kmlUrl)
        } else {
            kmlLayer?.removeLayerFromMap()
        }

        syncOverlays(project.id)
    }

    private fun downloadKml(projectId: String, url: String) {
        val cacheFile = File(cacheDir, "$projectId.kml")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!cacheFile.exists()) {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    kmlLayer?.removeLayerFromMap()
                    kmlLayer = KmlLayer(mMap, cacheFile.inputStream(), applicationContext)
                    kmlLayer?.addLayerToMap()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun syncOverlays(projectId: String) {
        overlayListener?.remove()
        overlayListener = db.collection("MapOverlays")
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshots, _ ->
                if (!::mMap.isInitialized) return@addSnapshotListener
                clearDynamicItems()

                snapshots?.forEach { doc ->
                    val type = doc.getString("type") ?: ""
                    val title = doc.getString("title") ?: ""
                    val colorStr = doc.getString("color") ?: "#FF0000"
                    val iconType = doc.getString("iconType") ?: ""

                    // Fetching 'points' as the Array of GeoPoints defined in your schema
                    val points = doc.get("points") as? List<com.google.firebase.firestore.GeoPoint>

                    if (!points.isNullOrEmpty()) {
                        when (type) {
                            "road" -> drawRoad(points, title, colorStr)
                            "marker" -> {
                                // Loop through the points array to place markers
                                points.forEach { geoPoint ->
                                    drawCustomMarker(geoPoint, title, iconType)
                                }
                            }
                        }
                    }
                }
            }
    }
    private fun drawCustomMarker(geoPoint: com.google.firebase.firestore.GeoPoint, title: String, iconType: String) {
        val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)

        // Select icon based on the 'iconType' property in your schema
        val iconRes = when (iconType) {
            "Crane" -> R.drawable.crane
            "Hazard" -> R.drawable.dangerous_24px
            "Electricity" -> R.drawable.electric_bolt_24px
            "DeepHole" -> R.drawable.donut_small_24px
            else -> null // Uses default marker
        }

        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)

        iconRes?.let {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(it))
        }

        val marker = mMap.addMarker(markerOptions)
        if (marker != null) dynamicOverlays.add(marker)
    }
    private fun drawRoad(points: List<com.google.firebase.firestore.GeoPoint>, title: String, colorString: String) {
        val latLngs = points.map { LatLng(it.latitude, it.longitude) }
        val color = try { Color.parseColor(colorString) } catch (e: Exception) { Color.RED }

        val polyline = mMap.addPolyline(PolylineOptions()
            .addAll(latLngs)
            .color(color)
            .width(10f))

        polyline.tag = title
        dynamicOverlays.add(polyline)
    }

    // Fixed: Now accepts a GeoPoint and uses the correct 'mMap' reference
    private fun drawMarkerAtPoint(geoPoint: com.google.firebase.firestore.GeoPoint, title: String) {
        val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
        if (marker != null) {
            dynamicOverlays.add(marker)
        }
    }

    private fun clearDynamicItems() {
        dynamicOverlays.forEach { item ->
            when (item) {
                is Polyline -> item.remove()
                is Marker -> item.remove()
            }
        }
        dynamicOverlays.clear()
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        overlayListener?.remove()
    }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState) }
}