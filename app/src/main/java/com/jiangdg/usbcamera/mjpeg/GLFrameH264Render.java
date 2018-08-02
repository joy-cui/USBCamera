package com.jiangdg.usbcamera.mjpeg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.suirui.srpaas.base.util.log.SRLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by cui on 2018/3/13.
 */
//
public class GLFrameH264Render   implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener{
    private int FRAME_RATE=24;
    private  Surface mDecoderSurface;
    private boolean isInitCodec;
    private String TAG = "VideoRender";
    private SRLog log = new SRLog(TAG, 1);
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private  float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };




    private FloatBuffer mTriangleVertices=null;

    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private SurfaceTexture mSurface;
    private boolean updateSurface = false;

    private  int GL_TEXTURE_EXTERNAL_OES = 0x8D65;//

    //    private MediaPlayer mMediaPlayer;
//    private int mScreenWidth=0;
//    private int mScreenHeight=0;
    private int mViewW=0;
    private int mViewH=0;
    private int dataWidth=0;
    private int dataHeight=0;
    //    private  Surface surface;
    private int updateWidth=0;//需要更新的视频宽度
    private int updateHeight=0;


    ByteBuffer[] inputBuffers;
    ByteBuffer inputBuffer=null;
    MediaCodec.BufferInfo bufferInfo;

    GLH264FrameSurface mTargetSurface;
    boolean isRender=true;

    private int inputWidth=0;
    private int inputHight=0;
    private  int  m_OutFpsBaseTimeMs=0;
    private Thread  outBufferThread =null;
    private Thread  renderThread =null;
    private int mCodecW=640;
    private int mCodecH=360;


    public DisplayMetrics getRelDM(Context context) {
        @SuppressLint("WrongConstant") WindowManager windowManager = (WindowManager)context.getSystemService("window");
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getMetrics(realDisplayMetrics);
        return realDisplayMetrics;
    }

    public GLFrameH264Render(Context context,GLH264FrameSurface glh264FrameSurface) {
        log.E("GLFrameH264Render....new....LargeVideoSceneonRender");
        this.mTargetSurface=glh264FrameSurface;
        createBuffers();
        Matrix.setIdentityM(mSTMatrix, 0);
        DisplayMetrics dm = this.getRelDM(context);
        m_OutFpsBaseTimeMs = 1000 / FRAME_RATE;
    }


    public void createBuffers() {
        if(mTriangleVertices == null) {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        if(mTriangleVertices!=null && mTriangleVerticesData!=null && mTriangleVerticesData.length>=20){
            mTriangleVertices.clear();
//               mTriangleVertices.order();
            mTriangleVertices.put(mTriangleVerticesData);
            mTriangleVertices.position(0);

        }

    }

    synchronized public void onFrameAvailable(SurfaceTexture surface) {
//        log.E("onFrameAvailable............: ======:"+this.dataWidth+"*"+this.dataHeight);
        if(updateHeight!=0 && updateWidth!=0){
            setDataSize(updateWidth,updateHeight);
            updateHeight=0;
            updateWidth=0;
        }
        updateSurface = true;
        if (mTargetSurface != null) {
            mTargetSurface.requestRender();
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        synchronized(this) {
            if (mSurface!=null && updateSurface) {
//                log.E("GLH264FrameSurface......onDrawFrame:==================== "+updateSurface);
                mSurface.updateTexImage();
                mSurface.getTransformMatrix(mSTMatrix);
                updateSurface = false;
            }
        }

//        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

//        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
//        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();

//        GLES20.glDrawArrays(5, 0, 4);
//        GLES20.glFinish();
//        GLES20.glDisableVertexAttribArray(this.maPositionHandle);
//        GLES20.glDisableVertexAttribArray(this.maTextureHandle);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mViewW = width;
        this.mViewH = height;
        Log.e("","sdk_log。。setViewSize。GLH264FrameSurface。onSurfaceChanged..setDataSize...width:" + width + "  height:" + height + " dataWidth:" + this.dataWidth + " dataHeight:" + this.dataHeight);
        gl.glViewport(0, 0, width, height);
        setViewSize();
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        mProgram = createProgram(mVertexShader, mFragmentShader);
        Log.e("","rendercreate......sdk_log");
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }


        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

            /*
             * Create the SurfaceTexture that will feed this textureID,
             * and pass it to the MediaPlayer
             */
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        mDecoderSurface = new Surface(mSurface);
        synchronized(this) {
            updateSurface = false;
        }
        isInitCodec = initDecoder(mDecoderSurface);
        mDecoderSurface.release();
    }






    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    //更新视频的宽高
    public void setDataSize(int width,int height){
//            Log.e(TAG, "setDataSize setViewSize 更新视频的宽高 sdk_log....width:   " + this.dataWidth + " height: " + this.dataHeight);
        if(this.dataWidth!=width || this.dataHeight!=height) {
            Log.e(TAG, "setDataSize setViewSize  sdk_log....width:   " + width + " height: " + height);
            this.dataWidth = width;
            this.dataHeight = height;
            this.setViewSize();


        }
    }
    //更新view的宽高
    public void updateScreenData(int width,int height){
//            Log.e("","updateScreenData....setViewSize...width:"+width+" height: "+height);
//            if(this.mViewW!=width || this.mViewH!=height){
//                this.mViewW=width;
//                this.mViewH=height;
//                this.setViewSize();
//            }
    }
    private void setViewSize() {
        Log.e("","GLH264FrameSurface...setViewSize.outputBufferIndex....sdk_log...dataWidth:"+this.dataWidth+"  dataHeight: "+this.dataHeight+" mviewwidth: "+ this.mViewW+" viewHeight:"+this.mViewH);
        synchronized (this) {
            if(dataWidth > 0 && dataHeight > 0) {
                if(this.mViewW > 0 && this.mViewH > 0) {
                    float e = 1.0F * (float)this.mViewH / (float)this.mViewW;
                    float uvarraySize = 1.0F * (float)dataHeight / (float)dataWidth;
                    Log.e("","setViewSize....uvarraySize:"+uvarraySize+" e: "+e+" this.mViewW:"+this.mViewW+" this.mViewH:"+this.mViewH);
                    if(e == uvarraySize) {

                        Log.e("","mTriangleVerticesData2....1");
                        float[]  mTriangleVerticesData2 = {
                                -1.0f, -1.0f, 0, 0.f, 0.f,
                                1.0f, -1.0f, 0, 1.f, 0.f,
                                -1.0f,  1.0f, 0, 0.f, 1.f,
                                1.0f,  1.0f, 0, 1.f, 1.f,
                        };
                        mTriangleVerticesData=mTriangleVerticesData2;

                    } else {
//                     Log.e("","mTriangleVerticesData2....2");
                        float heightScale;
                        if(e < uvarraySize) {

                            heightScale = e / uvarraySize;
                            Log.e("","mTriangleVerticesData2....3...heightScale:"+heightScale);
                            float[] mTriangleVerticesData2= {
                                    // X, Y, Z, U, V
                                    -heightScale, -1.0f, 0, 0.f, 0.f,
                                    heightScale, -1.0f, 0, 1.f, 0.f,
                                    -heightScale,  1.0f, 0, 0.f, 1.f,
                                    heightScale,  1.0f, 0, 1.f, 1.f,

                            };

                            mTriangleVerticesData=mTriangleVerticesData2;

                        } else {
                            Log.e("","mTriangleVerticesData2....4");
                            heightScale = uvarraySize / e;
                            float[] mTriangleVerticesData2= {
                                    // X, Y, Z, U, V
                                    -1.0f, -heightScale, 0, 0.f, 0.f,
                                    1.0f, -heightScale, 0, 1.f, 0.f,
                                    -1.0f,  heightScale, 0, 0.f, 1.f,
                                    1.0f,  heightScale, 0, 1.f, 1.f, };
                            mTriangleVerticesData=mTriangleVerticesData2;


                        }
                    }
                }



            }
            createBuffers();

        }
    }
    //硬解
    int mCount = 0;
    private MediaCodec mCodec=null;
//    private final static String MIME_TYPE = "video/avc";
    private final static String MIME_TYPE="video/mjpeg";// H.264 Advanced Video
    boolean initDecoder(Surface surface) {
        try {
            log.E("GLH264FrameSurface...WatchShareVIdeoSence....initDecoder...start...");
            mCodec = MediaCodec.createDecoderByType(MIME_TYPE);

            MediaFormat mediaFormat= mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                    mCodecW, mCodecH);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
//        mCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mCodec.configure(mediaFormat, surface,
                    null, 0);

            mCodec.start();
            log.E("GLH264FrameSurface...WatchShareVIdeoSence....initDecoder...start...end");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }





    public void unInit() {
        try {
            log.E("GLH264FrameSurface..WatchShareVIdeoSence.clearData....close...surfaceVIew");
            isRender=false;
            if(mCodec!=null) {
                mCodec.stop();
                mCodec.release();
                mCodec=null;
            }

            if(this.mDecoderSurface!=null){
                this.mDecoderSurface=null;
            }
            if(outBufferThread!=null){
                outBufferThread.interrupt();
                outBufferThread=null;
            }
            if(renderThread!=null){
                renderThread.interrupt();
                renderThread=null;
            }

            mTargetSurface = null;
            bufferInfo=null;
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onFrame(byte[] buf, int offset, int length, int width, int height) {
        try {
//            log.E("onFrame....."+width+" height: "+height+" GLFrameH264Render:"+this.hashCode());
             if(mCodec==null || !isInitCodec){
                return false;
            }

            int inputBufferIndex = mCodec.dequeueInputBuffer(-1);//获取输入缓冲区的索引(不等待)//-1
            if (inputBufferIndex >= 0) {
                inputBuffer=mCodec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(buf, offset, length);//length是传进来的原始长度
//                mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / FRAME_RATE, 0);
                mCodec.queueInputBuffer(inputBufferIndex, 0, length, System.currentTimeMillis(), 0);
                mCount++;
            } else {
                mCount=0;
            }

            this.inputWidth=width;
            this.inputHight=height;


            if (mCodec != null) {
                if (bufferInfo == null) {
                    bufferInfo = new MediaCodec.BufferInfo();
                }

                int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);//取出一帧数据；
                while (outputBufferIndex >= 0) {
                    mCodec.releaseOutputBuffer(outputBufferIndex, true);//释放缓冲区,归还输出 buffer
//                    log.E("WatchShareVIdeoSence...onFrame....解码成功 ");
                    outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);

                }
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED || outputBufferIndex == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    updateWidth = inputWidth;
                    updateHeight = inputHight;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }


        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onFrame(ByteBuffer buf,  int width, int height) {
        try {
            log.E("onFrame....."+width+" height: "+height+" GLFrameH264Render:capacity   "+buf.capacity());
            if(mCodec==null || !isInitCodec){
                return false;
            }
            int length=buf.capacity();
            byte[] bytes = new byte[length];
            buf.get(bytes);

            int inputBufferIndex = mCodec.dequeueInputBuffer(-1);//获取输入缓冲区的索引(不等待)//-1
            if (inputBufferIndex >= 0) {
                inputBuffer=mCodec.getInputBuffer(inputBufferIndex);
                log.E("ByteBuffer===onFrame    :"+inputBuffer.capacity());
                inputBuffer.clear();
                inputBuffer.put(bytes,0, length);//length是传进来的原始长度
//                mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / FRAME_RATE, 0);
                mCodec.queueInputBuffer(inputBufferIndex, 0, length, System.currentTimeMillis(), 0);
                mCount++;
            } else {
                mCount=0;
            }

            this.inputWidth=width;
            this.inputHight=height;


            if (mCodec != null) {
                if (bufferInfo == null) {
                    bufferInfo = new MediaCodec.BufferInfo();
                }

                int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);//取出一帧数据；
                while (outputBufferIndex >= 0) {
                    mCodec.releaseOutputBuffer(outputBufferIndex, true);//释放缓冲区,归还输出 buffer
//                    log.E("WatchShareVIdeoSence...onFrame....解码成功 ");
                    outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);

                }
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED || outputBufferIndex == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    updateWidth = inputWidth;
                    updateHeight = inputHight;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }


        return true;
    }


}
