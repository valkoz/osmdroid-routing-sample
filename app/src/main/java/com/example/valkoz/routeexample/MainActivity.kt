package com.example.valkoz.routeexample

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.bonuspack.routing.RoadManager


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_CODE = 103
    private val START_POINT = GeoPoint(55.754155, 37.620286)
    private val END_POINT = GeoPoint(55.748896, 37.582880)
    private val serviceUrl = "http://router.project-osrm.org/route/v1/foot/"

    private var subscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
    }

    private fun onStartUp() {
        view_map.apply {
            isClickable = true
            setBuiltInZoomControls(false)
            setMultiTouchControls(true)
            minZoomLevel = 10.0
            maxZoomLevel = 17.9
        }
        view_map.controller.setCenter(START_POINT)
        view_map.controller.setZoom(14.0)

        val roadManager = OSRMRoadManager(this)
        roadManager.setService(serviceUrl)
        subscription = Observable.fromCallable { roadManager.getRoad(arrayListOf(START_POINT, END_POINT)) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val roadOverlay = RoadManager.buildRoadOverlay(it)
                view_map.overlays.add(roadOverlay)
                view_map.invalidate()
            }
    }

    override fun onStop() {
        super.onStop()
        subscription?.dispose()
    }

    private fun checkPermissions() {
        if (!checkHasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !checkHasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            requestPermission(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CODE
            )
        } else {
            onStartUp()
        }
    }

    private fun checkHasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isEmpty() ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "Permissions has been denied by user")
                } else {
                    onStartUp()
                    Log.d(TAG, "Permissions has been granted by user")
                }
            }
        }

    }
}
