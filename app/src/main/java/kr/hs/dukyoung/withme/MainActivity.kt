package kr.hs.dukyoung.withme

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
	private var imageCapture: ImageCapture? = null

	private lateinit var outputDirectory: File
	private lateinit var cameraExecutor: ExecutorService

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// Request camera permissions
		if (allPermissionsGranted()) {
			startCamera()
		} else {
			ActivityCompat.requestPermissions(
				this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
			)
		}

		// Set up the listener for take photo button
		camera_capture_button.setOnClickListener { takePhoto() }

		outputDirectory = getOutputDirectory()

		cameraExecutor = Executors.newSingleThreadExecutor()
	}

	@SuppressLint("RestrictedApi")
	private fun takePhoto() {
		// Get a stable reference of the modifiable image capture use case
		val imageCapture = imageCapture ?: return

		// Create time-stamped output file to hold the image
		val photoFile = File(
			outputDirectory,
			SimpleDateFormat(
				FILENAME_FORMAT, Locale.KOREA
			).format(System.currentTimeMillis()) + ".jpg"
		)

		// Set up image capture listener, which is triggered after photo has
		// been taken
		imageCapture.takePicture(
			ContextCompat.getMainExecutor(this),
			object : ImageCapture.OnImageCapturedCallback() {
				override fun onError(exc: ImageCaptureException) {
					Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
				}

				@SuppressLint("UnsafeOptInUsageError")
				override fun onCaptureSuccess(image: ImageProxy) {
					val imageView: ImageView = findViewById(R.id.imageView)

					val bitmapImage = decodeBitmap(image)
					val drawableImage = bitmapImage?.toDrawable(resources)
					imageView.setImageDrawable(drawableImage)

					Log.d("bitmap Image", bitmapImage.toString())

					image.close()
				}

				fun decodeBitmap(image: ImageProxy): Bitmap? {
					val buffer = image.planes[0].buffer
					val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
					return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
				}
			}
		)
	}

	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

		cameraProviderFuture.addListener({
			// Used to bind the lifecycle of cameras to the lifecycle owner
			val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

			// Preview
			val preview = Preview.Builder()
				.build()
				.also {
					it.setSurfaceProvider(viewFinder.surfaceProvider)
				}

			imageCapture = ImageCapture.Builder()
				.build()
			val imageAnalyzer = ImageAnalysis.Builder()
				.setTargetResolution(Size(1280, 720)) // Setting Resolution Size of Captured Image
				.build()
				.also {
					it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
						Log.d(TAG, "Average luminosity: $luma")
					})
				}

			// Select back camera as a default
			val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

			try {
				// Unbind use cases before rebinding
				cameraProvider.unbindAll()

				// Bind use cases to camera
				cameraProvider.bindToLifecycle(
					this, cameraSelector, preview, imageCapture, imageAnalyzer
				)

			} catch (exc: Exception) {
				Log.e(TAG, "Use case binding failed", exc)
			}

		}, ContextCompat.getMainExecutor(this))
	}

	private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
		ContextCompat.checkSelfPermission(
			baseContext, it
		) == PackageManager.PERMISSION_GRANTED
	}

	private fun getOutputDirectory(): File {
		val mediaDir = externalMediaDirs.firstOrNull()?.let {
			File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
		}
		return if (mediaDir != null && mediaDir.exists())
			mediaDir else filesDir
	}

	override fun onDestroy() {
		super.onDestroy()
		cameraExecutor.shutdown()
	}

	companion object {
		private const val TAG = "CameraXBasic"
		private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
		private const val REQUEST_CODE_PERMISSIONS = 10
		private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
	}

	override fun onRequestPermissionsResult(
		requestCode: Int, permissions: Array<String>, grantResults:
		IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		// Check if the request code is correct; ignore it otherwise.
		if (requestCode == REQUEST_CODE_PERMISSIONS) {
			// If the permissions are granted, call startCamera().
			if (allPermissionsGranted()) {
				startCamera()
			}
			// If permissions are not granted, present a toast to notify the user that the permissions were not granted.
			else {
				Toast.makeText(
					this,
					"Permissions not granted by the user.",
					Toast.LENGTH_SHORT
				).show()
				finish()
			}
		}
	}

	private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

		private fun ByteBuffer.toByteArray(): ByteArray {
			rewind()    // Rewind the buffer to zero
			val data = ByteArray(remaining())
			get(data)   // Copy the buffer into a byte array
			return data // Return the byte array
		}

		override fun analyze(image: ImageProxy) {

			val buffer = image.planes[0].buffer
			val data = buffer.toByteArray()
			val pixels = data.map { it.toInt() and 0xFF }
			val luma = pixels.average()

			listener(luma)

			image.close()
		}
	}
}