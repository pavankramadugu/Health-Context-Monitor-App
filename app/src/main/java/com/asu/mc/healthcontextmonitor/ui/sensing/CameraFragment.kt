package com.asu.mc.healthcontextmonitor.ui.sensing

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asu.mc.healthcontextmonitor.R
import com.asu.mc.healthcontextmonitor.databinding.FragmentCameraBinding
import com.asu.mc.healthcontextmonitor.helper.HeartRate
import com.asu.mc.healthcontextmonitor.model.HeartRateEntity
import com.asu.mc.healthcontextmonitor.ui.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), HeartRate.HeartRateCallback {

    private lateinit var binding: FragmentCameraBinding
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var progressDialog: ProgressDialog

    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    private var counter = 45
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            binding.videoCaptureButton.text = getString(R.string.timer_text, counter)
            binding.videoCaptureButton.isEnabled = false
            binding.videoCaptureButton.setBackgroundColor(Color.parseColor("#AA9E9E9E"))
            counter--
            if (counter >= 0) {
                handler.postDelayed(this, 1000)
            } else {
                stopRecording()
                binding.videoCaptureButton.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater)
        return binding.root
    }

    private fun showHeartRateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Heart Rate")
            .setMessage("Please softly press your index finger on the camera lens while covering the flash light.")
            .setPositiveButton("OK", null)
            .show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHeartRateDialog()
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        progressDialog = ProgressDialog(context).apply {
            setMessage("Calculating heart rate...")
            setCancelable(false)
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            startCamera()
        }

        binding.videoCaptureButton.setOnClickListener {
            if (recording == null) {
                startTimer()
                captureVideo()
                binding.videoCaptureButton.isEnabled = false
            } else {
                stopRecording()
                binding.videoCaptureButton.isEnabled = true
            }
        }
    }

    private fun startTimer() {
        counter = 45
        handler.postDelayed(runnable, 0)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

                camera.cameraInfo.torchState.observe(viewLifecycleOwner) { state ->
                    if (state == TorchState.OFF) {
                        camera.cameraControl.enableTorch(true)
                    }
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireContext().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        //
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg =
                                "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            progressDialog.show()
                            val path = convertMediaUriToPath(recordEvent.outputResults.outputUri)
                            HeartRate(requireContext()).SlowTask(this).execute(path)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
                        }
                        binding.videoCaptureButton.apply {
                            text = getString(R.string.start_capture)
                        }
                        binding.videoCaptureButton.setBackgroundColor(Color.parseColor("#FF6200EE"))
                    }
                }
            }
    }

    fun convertMediaUriToPath(uri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        uri?.let {
            val cursor = requireContext().contentResolver
                .query(it, proj, null, null, null) ?: return null
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(column_index)
            cursor.close()
            return path
        }
        return null
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
        handler.removeCallbacks(runnable)
        counter = 45
        binding.videoCaptureButton.text = getString(R.string.start_capture)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onHeartRateCalculated(heartRate: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            heartRate?.toFloatOrNull()?.let { rate ->
                withContext(Dispatchers.IO) {
                    saveHeartRateToDatabase(rate)
                }
            }
            progressDialog.dismiss()
            binding.videoCaptureButton.isEnabled = true
            AlertDialog.Builder(requireContext())
                .setTitle("Heart Rate")
                .setMessage("Your calculated heart rate is: $heartRate")
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                    val action = CameraFragmentDirections
                        .actionCameraFragmentToRespFragment(heartRate?.toFloatOrNull() ?: 0.0f)
                    findNavController().navigate(action)
                }
                .show()
        }
    }

    private fun saveHeartRateToDatabase(heartRate: Float) {
        val time = System.currentTimeMillis()
        val heartRateEntity = HeartRateEntity(0, time, heartRate)
        database.heartRateDao().insert(heartRateEntity)
    }

    override fun onHeartRateCalculationFailed() {
        progressDialog.dismiss()
        binding.videoCaptureButton.isEnabled = true
        Toast.makeText(requireContext(), "Failed to calculate heart rate", Toast.LENGTH_SHORT)
            .show()
    }
}
