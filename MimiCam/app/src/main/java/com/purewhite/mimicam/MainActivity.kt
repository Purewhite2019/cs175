package com.purewhite.mimicam

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.*
import android.hardware.camera2.params.SessionConfiguration
import android.media.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_PERMISSION = 1

        const val MEDIA_TYPE_IMAGE = 2
        const val MEDIA_TYPE_VIDEO = 3
    }
    private val lockPermissionRequest = Object()

    private lateinit var editTextFileName : EditText
    private lateinit var textureView : TextureView
    private lateinit var textViewInfo : TextView
    private lateinit var imgBtnChangeCam : ImageButton
    private lateinit var imgBtnShutter : ImageButton
    private lateinit var imgViewLast : ImageView
    private lateinit var bytesHistory : ByteArray
    private lateinit var bitmapHistory : Bitmap

    private lateinit var cameraManager : CameraManager
    private lateinit var camera : CameraDevice
    private var cameraCaptureSession : CameraCaptureSession? = null
    private var textureSurface : Surface? = null

    private lateinit var imageReader : ImageReader
    private lateinit var imageReaderSurface : Surface

    private val mediaRecorder = MediaRecorder()
    private var isRecording = false

    private var outputFile : File? = null

    private var cameraId = 0


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        editTextFileName = findViewById(R.id.edittext_filename)
        textureView = findViewById(R.id.texture_view)
        textViewInfo = findViewById(R.id.textview_info)
        imgBtnChangeCam = findViewById(R.id.imgbtn_changecam)
        imgBtnShutter = findViewById(R.id.imgbtn_shutter)
        imgViewLast = findViewById(R.id.imgview_last)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                textureSurface = Surface(textureView.surfaceTexture)
                startCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }

        imgBtnChangeCam.setOnClickListener {
            cameraCaptureSession?.close()
            camera.close()
            imageReader.close()

            cameraId = 1 - cameraId
            startCamera()
        }
    }

    private fun startCamera() {
        val camStateCallback = object : CameraDevice.StateCallback() {
            @SuppressLint("ClickableViewAccessibility")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onOpened(camera: CameraDevice) {
                this@MainActivity.camera = camera

                val list = cameraManager.cameraIdList
                for (cam in list) {
                    val cameraCharacteristics = cameraManager.getCameraCharacteristics(cam)
                    Log.d("cam", "$cam : ${cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)}")
                }

                val cameraCharacteristics = cameraManager.getCameraCharacteristics(camera.id)
                val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val sizes = streamConfigurationMap!!.getOutputSizes(ImageFormat.JPEG)
                Log.d("startCamera()", "${sizes.size}, [0]: ${sizes[0].width}, ${sizes[0].height}")
                imageReader = ImageReader.newInstance(sizes[0].width, sizes[0].height, ImageFormat.JPEG, 2)
                imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
                    override fun onImageAvailable(reader: ImageReader?) {
                        val image = reader!!.acquireLatestImage()

                        val photoW = image.width
                        val photoH = image.height
                        val targetW = imgViewLast.width
                        val targetH = imgViewLast.width
                        var inSampleSize = 1
                        while (photoW > targetW * inSampleSize || photoH > targetH * inSampleSize)
                            inSampleSize *= 2

                        Log.d("Input img size", "$photoW x $photoH")
                        Log.d("Target img size", "$targetW x $targetH")
                        Log.d("Output img size", "${photoW / (1 shl inSampleSize)} x ${targetH / (1 shl inSampleSize)}")

                        val buffer = image.planes[0].buffer
                        bytesHistory = ByteArray(buffer.remaining())
                        buffer.get(bytesHistory)
                        image.close()

                        outputFile = createOutputFile(MEDIA_TYPE_IMAGE)
                        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
                        textViewInfo.text = "Last Shoot:\n"+
                                            "Image, $photoW × $photoH\n" +
                                            "${outputFile!!.name}\n" +
                                            "${currentTime}\n"

                        val fos = FileOutputStream(outputFile)
                        fos.write(bytesHistory)
                        fos.close()

                        val bmOptions = BitmapFactory.Options()
                        bmOptions.inSampleSize = inSampleSize
                        bitmapHistory = BitmapFactory.decodeByteArray(bytesHistory, 0, bytesHistory.size, bmOptions)
                        imgViewLast.setImageBitmap(bitmapHistory)

                    }
                }, null)

                imageReaderSurface = imageReader.surface

                try {
                    val requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    requestBuilder.addTarget(textureSurface!!)
                    val request = requestBuilder.build()

                    val camCaptureSessionStateCallback = object :
                        CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            cameraCaptureSession = session
                            try {
                                session.setRepeatingRequest(request, null, null)
                            } catch (e : CameraAccessException) {
                                e.printStackTrace()
                            }
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }

                    camera.createCaptureSession(listOf(textureSurface, imageReaderSurface), camCaptureSessionStateCallback, null)
                } catch (e : CameraAccessException) {
                    e.printStackTrace()
                }

                imgBtnShutter.setOnClickListener{
                    lateinit var requestBuilderImageReader : CaptureRequest.Builder
                    try {
                        requestBuilderImageReader = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                    } catch (e : CameraAccessException) {
                        e.printStackTrace()
                    }
                    requestBuilderImageReader.set(CaptureRequest.JPEG_ORIENTATION, 90)
                    requestBuilderImageReader.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
                    requestBuilderImageReader.addTarget(imageReaderSurface)

                    try {
                        cameraCaptureSession!!.capture(requestBuilderImageReader.build(), null, null)
                    } catch (e : CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                imgBtnShutter.setOnLongClickListener{
                    isRecording = true
                    startRecording()
                    true
                }

                imgBtnShutter.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP && isRecording) {
                        isRecording = false
                        stopRecording()
                        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
                        val mediaMetadataRetriever = MediaMetadataRetriever()
                        mediaMetadataRetriever.setDataSource(outputFile!!.path)
                        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000.toDouble()
                        textViewInfo.text = "Last Shoot:\n"+
                                "Video, ${textureView.width} × ${textureView.height}, $duration s\n" +
                                "${outputFile!!.name}\n" +
                                "$currentTime\n"
                        bitmapHistory = ThumbnailUtils.createVideoThumbnail(outputFile!!.path, MediaStore.Video.Thumbnails.MICRO_KIND)!!.scale(imgViewLast.width, imgViewLast.height)
                        imgViewLast.setImageBitmap(bitmapHistory)
                        return@setOnTouchListener true
                    }
                    return@setOnTouchListener false
                }
            }
            override fun onDisconnected(camera: CameraDevice) {}
            override fun onError(camera: CameraDevice, error: Int) {}
        }

        checkPermission()

        cameraManager.openCamera(cameraManager.cameraIdList[cameraId], camStateCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareVideoRecorder() : Boolean {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        if (camera.id.toInt() == 0)
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
        else
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW))
        mediaRecorder.setPreviewDisplay(textureSurface)
        outputFile = createOutputFile(MEDIA_TYPE_VIDEO)
        mediaRecorder.setOutputFile(outputFile)

        mediaRecorder.setOrientationHint(
            when(windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            })
        try {
            mediaRecorder.prepare()
        } catch (e: IllegalStateException) {
            mediaRecorder.reset()
            mediaRecorder.release()
            return false
        } catch (e: IOException) {
            mediaRecorder.reset()
            mediaRecorder.release()
            return false
        }
        return true
    }

    private fun createOutputFile(type : Int) : File {
        val mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!mediaStorageDir!!.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                throw RuntimeException("mediaStorageDir.mkdirs() failed")
            }
        }
        val fileName : String =
            if (editTextFileName.text.isNotEmpty()) editTextFileName.text.toString()
            else SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())

        val mediaFile = when (type) {
            MEDIA_TYPE_IMAGE -> File(mediaStorageDir.path + File.separator + fileName + ".jpg")
            MEDIA_TYPE_VIDEO -> File(mediaStorageDir.path + File.separator + fileName + ".mp4")
            else -> throw RuntimeException("Invalid mediaFile type: $type")
        }

        return mediaFile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {
        try {
            cameraCaptureSession?.close()
            prepareVideoRecorder()

//            val texture = textureView.surfaceTexture
//            texture!!.setDefaultBufferSize(textureView.width, textureView.height)
            val previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)

            previewBuilder.addTarget(textureSurface!!)

            val recorderSurface = mediaRecorder.surface
            previewBuilder.addTarget(recorderSurface)

            camera.createCaptureSession(listOf(textureSurface, recorderSurface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    try {
                        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                        cameraCaptureSession!!.setRepeatingRequest(previewBuilder.build(), null, null)
                    } catch (e : CameraAccessException) {
                        e.printStackTrace()
                    }
                    runOnUiThread{
                        mediaRecorder.start()
                    }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {}

            }, null)

        } catch (e : Exception) {
            e.printStackTrace()
        }
    }
    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        startCamera()
    }

    override fun onPause() {
        cameraCaptureSession?.close()
        camera.close()
        imageReader.close()

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (textureSurface != null)
            startCamera()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
            lockPermissionRequest.wait()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            lockPermissionRequest.notifyAll()
    }
}