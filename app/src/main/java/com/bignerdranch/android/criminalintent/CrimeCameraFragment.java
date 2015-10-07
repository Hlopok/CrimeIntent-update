package com.bignerdranch.android.criminalintent;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;


/**
 * Created by User on 28.09.2015.
 */
public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;
//    public static final String EXTRA_PHOTO_FILENAME = "com.sur.android.crimeintent.photo_filename";


    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);
        Button takePictureButton = (Button)v.findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener (new View.OnClickListener(){
            public void onClick(View v) {
               if (mCamera!=null) {
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
               }

            }
        });

        mProgressContainer = (View)v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);


        Button take_PictureButton = (Button)v.findViewById(R.id.crime_camera_takePictureButton);
        take_PictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        PackageManager pm = getActivity().getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) && (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))) {
            take_PictureButton.setEnabled(false);
        }

        mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
        mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) // surfaceDestroyed method start
            {
                if (mCamera != null)
                {
                    mCamera.stopPreview();
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (mCamera != null) {return;}

                Camera.Parameters parameters = mCamera.getParameters();
                Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), w, h);
                parameters.setPreviewSize(s.width, s.height);
                mCamera.setParameters(parameters);

                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview ", e);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
        return v;
    }

    @TargetApi(9)
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Size getBestSupportedSize(List<Size> sizes, int width, int height) // getBestSupportedSize method start
    {
        Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Size s : sizes)
        {
            int area = s.width * s.height;
            if (area > largestArea)
            {
                bestSize = s;
                largestArea = area;
            } // end if
        } // end for

        return bestSize;
    } // getBestSupportedSize method en

     private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };


    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            String filename = UUID.randomUUID().toString()+".jpg";
            FileOutputStream os = null;
            boolean success = true;
            try {
                os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                os.write(data);

            } catch (Exception e) {
                Log.e(TAG, "Error write to file" + filename,e);
                success = false;
            } finally {
                try {
                    if (os!=null) {
                        os.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error close file" + filename,e);
                    success=false;
                }
            }
            if (success) {
                Log.i(TAG, "JPEG saved at " +filename);
            }
            getActivity().finish();
         }
    };
}
