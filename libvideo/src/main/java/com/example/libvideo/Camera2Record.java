package com.example.libvideo;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.print.PrinterId;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/14
 */
public class Camera2Record {

    public static final int TYPE_TEXTURE = 1;
    public static final int TYPE_SURFACEVIEW = 2;

    private static String TAG = "Camera2Record";
    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    private Context mContext;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * TextureView
     *
     * @param mContext
     */
    private TextureView mTextureView;

    private CaptureRequest.Builder mPreviewBuilder;

    private SurfaceHolder mSurfaceHolder;

    private int type;

    public Camera2Record(Context mContext, TextureView mTextureView, int type) {
        this.mContext = mContext;
        this.mTextureView = mTextureView;
        this.type = type;
    }

    public Camera2Record(Context mContext, SurfaceHolder mSurfaceHolder, int type) {
        this.mContext = mContext;
        this.mSurfaceHolder = mSurfaceHolder;
        this.type = type;
    }


    public void openCamera2(int width, int height) {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        closePreviewSession();
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(streamConfigurationMap.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), width, height, mVideoSize);

            mMediaRecorder = new MediaRecorder();
            cameraManager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened");
            mCameraDevice = cameraDevice;
            stratPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onDisconnected");
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d(TAG, "onError");
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    public void startRecordVideo(String videoFilePath) {
        if (mCameraDevice == null) {
            return;
        }
        closePreviewSession();
        try {
            setUpMediaRecorder(videoFilePath);

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<Surface> surfaces = new ArrayList<>();
            // Set up Surface for the camera preview
            Surface previewSurface = null;

            if (type == TYPE_TEXTURE) {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                if (texture == null) {
                    return;
                }
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                previewSurface = new Surface(texture);
            }else {
                previewSurface = mSurfaceHolder.getSurface();
            }

            surfaces.add(previewSurface);
            //  ImageReader imageReader = ImageReader.newInstance(mVideoSize.getWidth(), mVideoSize.getHeight(), ImageFormat.JPEG, 10);
            //  imageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
            //  mPreviewBuilder.addTarget(imageReader.getSurface());
            mPreviewBuilder.addTarget(previewSurface);
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            // Set up Surface for the MediaRecorder
            mPreviewBuilder.addTarget(recorderSurface);
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopVideoRecord() {
        Log.d(TAG, "stopVideoRecord");
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
        stratPreview();

    }


    private void setUpMediaRecorder(String videoFile) {
        if (mMediaRecorder == null) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(videoFile);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "MediaRecorder prepare:" + e.getMessage());
        }
    }

    private void stratPreview() {
        if (mTextureView == null && type == TYPE_TEXTURE || mSurfaceHolder == null && type == TYPE_SURFACEVIEW) {
            return;
        }
        try {

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface previewSurface = null;

            if (type == TYPE_TEXTURE) {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                if (texture == null) {
                    return;
                }
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                previewSurface = new Surface(texture);
            }else {
                previewSurface = mSurfaceHolder.getSurface();
            }

            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "onConfigureFailed");
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    public void closeCamera() {
        closePreviewSession();
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void updatePreview() {
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            Log.d(TAG, "bytes.length:" + bytes.length);
            image.close();
        }
    };
}
