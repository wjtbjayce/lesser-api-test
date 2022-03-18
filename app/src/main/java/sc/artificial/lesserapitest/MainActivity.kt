package sc.artificial.lesserapitest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import sc.artificial.lesserapi.camera.GazeAnalysisDelegate
import sc.artificial.lesserapi.camera.GazeAnalysisManager
import sc.artificial.lesserapi.camera.GazeAnalysisType
import sc.artificial.lesserapi.permissions.PermissionControl
import sc.artificial.lesserapitest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), GazeAnalysisDelegate {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (PermissionControl.checkAndGetPermissions(this)) {
            GazeAnalysisManager.initAnalyzer(this, arrayOf(GazeAnalysisType.concentraiton, GazeAnalysisType.gaze))
        }
        GazeAnalysisManager.bindCamera(this)
    }
}