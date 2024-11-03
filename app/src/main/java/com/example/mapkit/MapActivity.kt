package com.example.mapkit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapkit.databinding.ActivityMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var locationManager: LocationManager
    private lateinit var mapObjectCollection: MapObjectCollection
    private lateinit var placeMarkMapObject: PlacemarkMapObject
    private lateinit var startLocation: Point

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        requestLocation()

        binding.trafficBT.setOnClickListener{

            val trafficLayer =
                MapKitFactory.getInstance().createTrafficLayer(binding.mapView.mapWindow)
            trafficLayer.isTrafficVisible = !trafficLayer.isTrafficVisible

        }

    }
    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, art) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) ?: return null
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun setMarkInStartLocation(){
        val marker = createBitmapFromVector(R.drawable.ic_place)
        mapObjectCollection = binding.mapView.map.mapObjects
        placeMarkMapObject = mapObjectCollection.addPlacemark(
            startLocation,
            ImageProvider.fromBitmap(marker)
        )
        placeMarkMapObject.opacity = 0.5f
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                .requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        locationManager
            .requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 10f, locationListener)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            startLocation = Point(latitude, longitude)
            setMapPosition(startLocation)
            setMarkInStartLocation()
            locationManager.removeUpdates(this)
        }

    }

    private fun setMapPosition(location: Point) {
        binding.mapView.map.move(CameraPosition(Point(location.latitude, location.longitude), 15f, 0f, 0f))
        binding.mapView.map.mapObjects.addPlacemark(Point(location.latitude, location.longitude))
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        }
    }
    private fun setApiKey(savedInstanceState: Bundle?){
    val haveApiKey = savedInstanceState?.getBoolean("haveApiKey") ?: false
    if (!haveApiKey)MapKitFactory.setApiKey(Utils.MAP_KIT_API_KEY)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("haveApiKey", true)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

}
