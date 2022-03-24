package sc.artificial.lesserapitest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.SparseIntArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import sc.artificial.lesserapi.camera.GazeAnalysisDelegate
import sc.artificial.lesserapi.camera.GazeBitmapAnalysisType
import sc.artificial.lesserapi.camera.GazeBitmapAnalyzer
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity(), GazeAnalysisDelegate, SurfaceHolder.Callback {

    private lateinit var mSurfaceViewHolder: SurfaceHolder
    private lateinit var mImageReader: ImageReader
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mPreviewBuilder: CaptureRequest.Builder
    private lateinit var mSession: CameraCaptureSession
    private var mHandler: Handler? = null
    private var mainHandler:Handler = Handler(Looper.getMainLooper())

    private var mSurfaceView: SurfaceView? = null
    private lateinit var mAccelerometer: Sensor
    private lateinit var mMagnetometer: Sensor
    private lateinit var mSensorManager: SensorManager

    private var mHeight: Int = 300
    private var mWidth:Int = 200

    var mCameraId = CAMERA_FRONT

    companion object
    {
        const val CAMERA_BACK = "0"
        const val CAMERA_FRONT = "1"

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSurfaceView = findViewById(R.id.surface_area)
        mSurfaceViewHolder = mSurfaceView?.holder!!
        mSurfaceViewHolder.addCallback(this)

//        GazeBitmapAnalyzer(this, this, arrayOf(GazeBitmapAnalysisType.concentraiton, GazeBitmapAnalysisType.gaze))
//            .bitmapAnalyze(rawBitmap) // Local Image가 아닌 카메라를 통해 얻은 Bitmap인 경우, rawBitmap대신 현재 카메라를 통해 얻은 Bitmap 삽입

//        if (PermissionControl.checkAndGetPermissions(this)) {
//            Log.d("ksjayce_c","permission is done.")
//            GazeBitmapAnalysisManager.initAnalyzer(this, arrayOf(GazeBitmapAnalysisType.concentraiton, GazeBitmapAnalysisType.gaze))
//        }
//        GazeBitmapAnalysisManager.bindCamera(this)

        // CameraX가 아닌 Camera2등을 사용하는 경우 Bitmap을 얻어와 다음을 참고해 사용 - Bitmap Analyze
        // Local Image Test Code
//        val assetManager: AssetManager = this.assets
//        try {
//            val inputStream = assetManager.open("test.png")
//            val rawBitmap = BitmapFactory.decodeStream(inputStream, null, null)!!
//
//            GazeBitmapAnalyzer(this, this, arrayOf(GazeBitmapAnalysisType.concentraiton, GazeBitmapAnalysisType.gaze))
//                .bitmapAnalyze(rawBitmap) // Local Image가 아닌 카메라를 통해 얻은 Bitmap인 경우, rawBitmap대신 현재 카메라를 통해 얻은 Bitmap 삽입
//            inputStream.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    override fun handleGazePoint(gazePoint: PointF, timestamp: Long) {
        Log.d("ksjayce_c","point ( ${gazePoint.x}, ${gazePoint.y} ) >>> time : $timestamp")
    }

    override fun handleConcentration(isConcentrating: Boolean, timestamp: Long) {
        Log.d("ksjayce_c","isConcentrating : $isConcentrating >>> time : $timestamp")
    }

    private fun openCamera() {
        try {
            val mCameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = mCameraManager.getCameraCharacteristics(mCameraId)

            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val largestPreviewSize = map!!.getOutputSizes(ImageFormat.JPEG)[0]
            setAspectRatioTextureView(largestPreviewSize.height, largestPreviewSize.width)

            mImageReader = ImageReader.newInstance(
                largestPreviewSize.width,
                largestPreviewSize.height,
                ImageFormat.JPEG,
                7
            )
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) return

            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler)
        } catch (e: CameraAccessException) {
            Log.d("ksjayce_c","카메라오픈 실패")
        }
    }

    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try {
                takePreview()

                var bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)

            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCameraDevice.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d("ksjayce_c","카메라오픈 실패")
        }
    }

    @Throws(CameraAccessException::class)
    fun takePreview() {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewBuilder.addTarget(mImageReader.surface)
        mPreviewBuilder.addTarget(mSurfaceViewHolder.surface)

        mCameraDevice.createCaptureSession(
            listOf(mSurfaceViewHolder.surface, mImageReader.surface), mSessionPreviewStateCallback, mHandler
        )
    }

    private val mSessionPreviewStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            mSession = session
            try {
                // Key-Value 구조로 설정
                // 오토포커싱이 계속 동작
                mPreviewBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                //필요할 경우 플래시가 자동으로 켜짐
                mPreviewBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Toast.makeText(this@MainActivity, "카메라 구성 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
//        mSensorManager.unregisterListener(deviceOrientation.eventListener)
    }

    private fun setAspectRatioTextureView(ResolutionWidth: Int, ResolutionHeight: Int) {
        if (ResolutionWidth > ResolutionHeight) {
            val newWidth = mWidth
            val newHeight = mWidth * ResolutionWidth / ResolutionHeight
            updateTextureViewSize(newWidth, newHeight)

        } else {
            val newWidth = mWidth
            val newHeight = mWidth * ResolutionHeight / ResolutionWidth
            updateTextureViewSize(newWidth, newHeight)
        }
    }

    private fun updateTextureViewSize(viewWidth: Int, viewHeight: Int) {
        Log.d("ViewSize", "TextureView Width : $viewWidth TextureView Height : $viewHeight")
//        surfaceView.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
    }

    fun initCameraAndPreview() {
        val handlerThread = HandlerThread("CAMERA2")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)

        openCamera()
    }

    private val mOnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            val image: Image = reader.acquireNextImage()
            val buffer: ByteBuffer = image.getPlanes().get(0).getBuffer()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            GazeBitmapAnalyzer(this, this, arrayOf(GazeBitmapAnalysisType.concentraiton, GazeBitmapAnalysisType.gaze))
                .bitmapAnalyze(bitmap) // Local Image가 아닌 카메라를 통해 얻은 Bitmap인 경우, rawBitmap대신 현재 카메라를 통해 얻은 Bitmap 삽입
            image.close()
        }

    override fun surfaceCreated(p0: SurfaceHolder) {
        initCameraAndPreview()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        mCameraDevice.close()
    }
}