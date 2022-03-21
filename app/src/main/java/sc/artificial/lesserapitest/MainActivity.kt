package sc.artificial.lesserapitest

import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import sc.artificial.lesserapi.camera.GazeAnalysisDelegate
import sc.artificial.lesserapi.camera.GazeAnalysisManager
import sc.artificial.lesserapi.camera.GazeAnalysisType
import sc.artificial.lesserapi.permissions.PermissionControl
import sc.artificial.lesserapitest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (PermissionControl.checkAndGetPermissions(this)) {
            Log.d("ksjayce_c","permission is done.")
            GazeAnalysisManager.initAnalyzer(object : GazeAnalysisDelegate {
                override fun handleConcentration(isConcentrating: Boolean, timestamp: Long) {
//                    super.handleConcentration(isConcentrating, timestamp)
                    Log.d("ksjayce_c","isconcentrating : $isConcentrating -- time : $timestamp")
                }

                override fun handleGazePoint(gazePoint: PointF, timestamp: Long) {
//                    super.handleGazePoint(gazePoint, timestamp)
                    Log.d("ksjayce_g","x : ${gazePoint.x}, y : ${gazePoint.y} -- time : $timestamp")
                }
            }, arrayOf(GazeAnalysisType.concentraiton, GazeAnalysisType.gaze))
        }
        GazeAnalysisManager.bindCamera(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        GazeAnalysisManager.unbindCamera()
    }
//
//    override fun handleConcentration(isConcentrating: Boolean, timestamp: Long) {
//        super.handleConcentration(isConcentrating, timestamp)
//
//    }
//
//    override fun handleGazePoint(gazePoint: PointF, timestamp: Long) {
//        super.handleGazePoint(gazePoint, timestamp)
//
//    }
}