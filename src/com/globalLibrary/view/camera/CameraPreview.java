package com.globalLibrary.view.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import com.globalLibrary.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class CameraPreview extends FrameLayout implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceView preview;
    private ViewfinderView frame;

    private int rotation;// 旋转角度

    public CameraPreview(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        preview = new SurfaceView(context);
        preview.setLayoutParams(layoutParams);
        preview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        addView(preview, layoutParams);

        frame = new ViewfinderView(context);
        frame.setLayoutParams(layoutParams);
        addView(frame, layoutParams);

        SurfaceHolder holder = preview.getHolder();
        holder.addCallback(this);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        if (camera == null) {
            camera = Camera.open();
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        Camera.Parameters params = camera.getParameters();
                        params.setPictureFormat(ImageFormat.JPEG);
                        params.setPreviewSize(preview.getMeasuredWidth(), preview.getMeasuredHeight());
                        camera.setParameters(params);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        camera.stopPreview();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        switch (rotation) {
            case Surface.ROTATION_0:
                parameters.setPreviewSize(height, width);
                camera.setDisplayOrientation(90);
                break;
            case Surface.ROTATION_90:
                parameters.setPreviewSize(width, height);
                camera.setDisplayOrientation(0);
                break;
            case Surface.ROTATION_180:
                parameters.setPreviewSize(height, width);
                camera.setDisplayOrientation(270);
                break;
            case Surface.ROTATION_270:
                parameters.setPreviewSize(width, height);
                camera.setDisplayOrientation(180);
                break;
        }
        camera.setParameters(parameters);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public Rect getFrameSize() {
        return frame.getFrameRect();
    }

    public SurfaceHolder getSurfaceholder() {
        return preview.getHolder();
    }

    public void getBitmapInFrame(PreviewBitmapHandler previewBitmapHandler) {
        getBitmap(previewBitmapHandler, true);
    }

    private void getBitmap(final PreviewBitmapHandler previewBitmapHandler, final boolean isFrame) {
        if (camera != null) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        camera.setOneShotPreviewCallback(new PreviewCallback(previewBitmapHandler, isFrame));
                    } else if (previewBitmapHandler != null) {
                        previewBitmapHandler.onError();
                    }
                }
            });
        } else if (previewBitmapHandler != null) {
            previewBitmapHandler.onError();
        }
    }

    public void getBitmapInPreview(PreviewBitmapHandler previewBitmapHandler) {
        getBitmap(previewBitmapHandler, false);
    }

    public void setFrameSize(Rect rect) {
        frame.setFrameRect(rect);
    }

    public void setFrameSize(final int width, final int height) {
        frame.setFrameWidth(width).setFrameHeight(height);
    }

    public void setRotation(Activity activity) {
        if (activity != null) {
            rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        }
    }

    private class PreviewCallback implements Camera.PreviewCallback {
        private PreviewBitmapHandler previewBitmapHandler;
        private boolean isFrameBitmap = false;

        private PreviewCallback(PreviewBitmapHandler previewBitmapHandler, boolean frameBitmap) {
            this.previewBitmapHandler = previewBitmapHandler;
            this.isFrameBitmap = frameBitmap;
        }

        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size previewSize = parameters.getPreviewSize();
            Rect rect;
            if (isFrameBitmap) {
                rect = getFrameSize();
            } else {
                rect = new Rect(0, 0, previewSize.width, previewSize.height);
            }
            if (Arrays.asList(Surface.ROTATION_0, Surface.ROTATION_180).contains(rotation)) {
                int centerX = rect.centerY();
                int centerY = rect.centerX();
                int width = rect.height();
                int height = rect.width();
                rect.left = centerX - width / 2;
                rect.top = centerY - height / 2;
                rect.right = rect.left + width;
                rect.bottom = rect.top + height;
            }

            YuvImage image = new YuvImage(bytes, parameters.getPreviewFormat(), previewSize.width, previewSize.height, null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
            if (image.compressToJpeg(rect, 100, outputStream)) {
                byte[] data = outputStream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                data = null;
                IOUtil.close(outputStream);
                if (bitmap == null) {
                    if (previewBitmapHandler != null) {
                        previewBitmapHandler.onError();
                    }
                } else {
                    float degress = 0F;
                    switch (rotation) {
                        case Surface.ROTATION_0:
                            degress = 90F;
                            break;
                        case Surface.ROTATION_90:
                            degress = 0F;
                            break;
                        case Surface.ROTATION_180:
                            degress = 270;
                            break;
                        case Surface.ROTATION_270:
                            degress = 180;
                            break;
                    }
                    Bitmap dstBitmap;
                    if (degress == 0F) {
                        dstBitmap = bitmap.copy(bitmap.getConfig(), false);
                    } else {
                        Matrix matrix = new Matrix();
                        matrix.reset();
                        matrix.postRotate(degress);
                        dstBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }
                    bitmap.recycle();
                    if (previewBitmapHandler != null) {
                        if (dstBitmap == null) {
                            previewBitmapHandler.onError();
                        } else {
                            previewBitmapHandler.onHandler(dstBitmap);
                        }
                    }
                }
            } else if (previewBitmapHandler != null) {
                previewBitmapHandler.onError();
            }
        }
    }

    public interface PreviewBitmapHandler {
        void onError();

        void onHandler(Bitmap bitmap);
    }
}
