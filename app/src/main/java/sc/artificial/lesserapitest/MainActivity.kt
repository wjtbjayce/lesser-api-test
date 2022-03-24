package sc.artificial.lesserapitest

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import sc.artificial.lesserapi.camera.GazeAnalysisDelegate
import sc.artificial.lesserapi.camera.GazeBitmapAnalysisManager
import sc.artificial.lesserapi.camera.GazeBitmapAnalysisType
import sc.artificial.lesserapi.camera.GazeBitmapAnalyzer
import sc.artificial.lesserapi.permissions.PermissionControl
import sc.artificial.lesserapitest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), GazeAnalysisDelegate {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (PermissionControl.checkAndGetPermissions(this)) {
            Log.d("ksjayce_c","permission is done.")
            GazeBitmapAnalysisManager.initAnalyzer(this, arrayOf(GazeBitmapAnalysisType.concentraiton, GazeBitmapAnalysisType.gaze))
        }
        GazeBitmapAnalysisManager.bindCamera(this)

        // CameraX가 아닌 Camera2등을 사용하는 경우 Bitmap을 얻어와 다음을 참고해 사용 - Bitmap Analyze
        // Local Image Test Code
        val assetManager: AssetManager = this.assets
        try {
            val inputStream = assetManager.open("test.png")
            val rawBitmap = BitmapFactory.decodeStream(inputStream, null, null)!!

            GazeBitmapAnalyzer(this, this, arrayOf(GazeBitmapAnalysisType.concentraiton, GazeBitmapAnalysisType.gaze))
                .bitmapAnalyze(rawBitmap) // Local Image가 아닌 카메라를 통해 얻은 Bitmap인 경우, rawBitmap대신 현재 카메라를 통해 얻은 Bitmap 삽입
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun handleGazePoint(gazePoint: PointF, timestamp: Long) {
        Log.d("ksjayce_c","point ( ${gazePoint.x}, ${gazePoint.y} ) >>> time : $timestamp")
    }

    override fun handleConcentration(isConcentrating: Boolean, timestamp: Long) {
        Log.d("ksjayce_c","isConcentrating : $isConcentrating >>> time : $timestamp")
    }
}