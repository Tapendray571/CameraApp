package com.example.camerahdx

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camerahdx.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var imageCapture:ImageCapture?=null
private lateinit var outputDirectory: File
private var imageViewSmall :ImageView?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory=outputDirectory()

        imageViewSmall?.setOnClickListener(this)

        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS, Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnTakePhoto.setOnClickListener {
        takephoto()
    }

        resultRedirect()

    }

    private fun resultRedirect(){

        binding?.imageViewSmall?.setOnClickListener(View.OnClickListener{ val intent = Intent(this,Gallery::class.java)
            startActivity(intent)
            finish()})
    }

    private fun outputDirectory(): File{
        val mediaDir=externalMediaDirs.firstOrNull()?.let {
            mFile->File(mFile,resources.getString(R.string.app_name)).apply {
                mkdirs()
        }
        }
        return if (mediaDir!=null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takephoto(){
        val imageCapture=imageCapture?: return
        val photoFile =File(
                    outputDirectory,
            SimpleDateFormat(Constants.File_NAME_FORMAT,
            Locale.getDefault())
                .format(System
                    .currentTimeMillis())+".jpg")

        val outputOption=ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()


        imageCapture.takePicture(outputOption,
        ContextCompat.getMainExecutor(this),
        object :ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                val savedUri=Uri.fromFile(photoFile)
                val msg="photo saved"

                Toast.makeText(this@MainActivity,
                    "$msg $savedUri",
                    Toast.LENGTH_LONG)
                    .show()


            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(Constants.Tag,
                    "${exception.message}",
                    exception)
            }


        }
        )
    }



     private fun startCamera(){
         val cameraProviderFuture=ProcessCameraProvider
             .getInstance(this)
                 cameraProviderFuture.addListener({


                     val cameraProvider: ProcessCameraProvider=cameraProviderFuture.get()


                     val preview= Preview.Builder()
                         .build()
                         .also { mPreview->
                             mPreview.setSurfaceProvider(
                                 binding.viewFinder.surfaceProvider
                             )

                         }
                     imageCapture=ImageCapture.Builder()
                         .build()
                     val cameraSelector=CameraSelector.DEFAULT_BACK_CAMERA
                     try{

                         cameraProvider.unbindAll()
                         cameraProvider.bindToLifecycle(
                             this, cameraSelector,preview,
                             imageCapture
                         )


                     }catch (e:Exception){
                         Log.d(Constants.Tag,"start Camera Fail:", e)
                     }

                 }, ContextCompat.getMainExecutor(this))

     }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.REQUEST_CODE_PERMISSIONS){
             if(allPermissionGranted()){
                  startCamera()
             }else{
                 Toast.makeText(this,
                     "Permisssion not granted by the user",
                 Toast.LENGTH_SHORT).show()
                 finish()
             }
         }
    }





    private fun allPermissionGranted()=
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext,it
            )== PackageManager.PERMISSION_GRANTED
        }




}

private fun ImageView?.setOnClickListener(mainActivity: MainActivity) {

}




