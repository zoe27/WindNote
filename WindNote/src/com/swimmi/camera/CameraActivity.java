package com.swimmi.camera;

import static com.swimmi.common.BaseConfigure.APP_PATH_IMAGE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * �������
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.swimmi.common.BaseConfigure;
import com.swimmi.common.HeartWayConstant;
import com.swimmi.windnote.R;

public class CameraActivity extends Activity implements SurfaceHolder.Callback{
	private static final String tag="yan";  
    private boolean isPreview = false;  
    private SurfaceView mPreviewSV = null; //Ԥ��SurfaceView  
    private SurfaceHolder mySurfaceHolder = null;  
    private ImageButton mPhotoImgBtn = null;  
    private Camera myCamera = null;  
    private Bitmap mBitmap = null;  
    private AutoFocusCallback myAutoFocusCallback = null;  
    
    //���水ť
    private ImageButton saveBtn = null;
    
    //�����ͼƬ����
    private List<String> picPath = new ArrayList<String>();
  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        //����ȫ���ޱ���  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;  
        Window myWindow = this.getWindow();  
        myWindow.setFlags(flag, flag);  
  
        setContentView(R.layout.activity_rect_photo);  
        
        saveBtn = (ImageButton) findViewById(R.id.savePic);
  
        //��ʼ��SurfaceView  
        mPreviewSV = (SurfaceView)findViewById(R.id.previewSV);  
        mySurfaceHolder = mPreviewSV.getHolder();  
        mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);//translucent��͸�� transparent͸��  
        mySurfaceHolder.addCallback(this);  
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
  
        //�Զ��۽������ص�  
        myAutoFocusCallback = new AutoFocusCallback() {  
  
            public void onAutoFocus(boolean success, Camera camera) {  
                // TODO Auto-generated method stub  
                if(success)//success��ʾ�Խ��ɹ�  
                {  
                    Log.i(tag, "myAutoFocusCallback: success...");  
                    //myCamera.setOneShotPreviewCallback(null);  
  
                }  
                else  
                {  
                    //δ�Խ��ɹ�  
                    Log.i(tag, "myAutoFocusCallback: ʧ����...");  
  
                }  
  
  
            }  
        };  
  
        mPhotoImgBtn = (ImageButton)findViewById(R.id.photoImgBtn);  
        //�ֶ���������ImageButton�Ĵ�СΪ120��120,ԭͼƬ��С��64��64  
        LayoutParams lp = mPhotoImgBtn.getLayoutParams();  
        lp.width = 120;  
        lp.height = 120;          
        mPhotoImgBtn.setLayoutParams(lp);                 
        mPhotoImgBtn.setOnClickListener(new PhotoOnClickListener());  
     //   mPhotoImgBtn.setOnTouchListener(new MyOnTouchListener());  
  
        saveBtn.setOnClickListener(saveBtnClick);
  
    }  
  
  
    /*����������SurfaceHolder.Callback�����Ļص�����*/  
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)   
    // ��SurfaceView/Ԥ������ĸ�ʽ�ʹ�С�����ı�ʱ���÷���������  
    {  
        // TODO Auto-generated method stub        
        Log.i(tag, "SurfaceHolder.Callback:surfaceChanged!");  
        initCamera();  
  
    }  
  
  
    public void surfaceCreated(SurfaceHolder holder)   
    // SurfaceView����ʱ/����ʵ������Ԥ�����汻����ʱ���÷��������á�  
    {  
        // TODO Auto-generated method stub        
        myCamera = Camera.open();  
        try {  
            myCamera.setPreviewDisplay(mySurfaceHolder);  
            Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            if(null != myCamera){  
                myCamera.release();  
                myCamera = null;  
            }  
            e.printStackTrace();  
        }  
  
  
  
    }  
  
  
    public void surfaceDestroyed(SurfaceHolder holder)   
    //����ʱ������  
    {  
        // TODO Auto-generated method stub  
        Log.i(tag, "SurfaceHolder.Callback��Surface Destroyed");  
        if(null != myCamera)  
        {  
            myCamera.setPreviewCallback(null); /*������PreviewCallbackʱ���������ǰ��Ȼ�˳����� 
            ����ʵ����ע�͵�Ҳû��ϵ*/  
              
            myCamera.stopPreview();   
            isPreview = false;   
            myCamera.release();  
            myCamera = null;       
        }  
  
    }  
  
    //��ʼ�����  
    public void initCamera(){  
        if(isPreview){  
            myCamera.stopPreview();  
        }  
        if(null != myCamera){             
			Camera.Parameters myParam = myCamera.getParameters();
			// ��ѯ��Ļ�Ŀ�͸�
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Log.i(tag,
					"��Ļ��ȣ�" + display.getWidth() + " ��Ļ�߶�:"
							+ display.getHeight());

			myParam.setPictureFormat(PixelFormat.JPEG);// �������պ�洢��ͼƬ��ʽ

			Size picSize = null;
			Size preSize = null;

			// ��ѯcamera֧�ֵ�picturesize��previewsize
			List<Size> pictureSizes = myParam.getSupportedPictureSizes();
			List<Size> previewSizes = myParam.getSupportedPreviewSizes();
			for (int i = 0; i < pictureSizes.size(); i++) {
				picSize = pictureSizes.get(i);
				Log.i(tag, "initCamera:����ͷ֧�ֵ�pictureSizes: width = "
						+ picSize.width + "height = " + picSize.height);
			}
			for (int i = 0; i < previewSizes.size(); i++) {
				preSize = previewSizes.get(i);
				Log.i(tag, "initCamera:����ͷ֧�ֵ�previewSizes: width = "
						+ preSize.width + "height = " + preSize.height);

			}
  
            //���ô�С�ͷ���Ȳ���  
            myParam.setPictureSize(picSize.width, picSize.height);  
            myParam.setPreviewSize(preSize.width, preSize.height);
           // myParam.setRotation(0);
            // myParam.set("rotation", 90);                
            // myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); 
            myCamera.setDisplayOrientation(180);
            if(isVertical()){
            	myCamera.setDisplayOrientation(90); 
            	myParam.setRotation(90);
            }
            myCamera.setParameters(myParam);              
            myCamera.startPreview();  
            myCamera.autoFocus(myAutoFocusCallback);  
            isPreview = true;  
        }  
    }  
  
    /*Ϊ��ʵ�����յĿ������������ձ�����Ƭ��Ҫ���������ص�����*/  
    ShutterCallback myShutterCallback = new ShutterCallback()   
    //���Ű��µĻص������������ǿ����������Ʋ��š����ꡱ��֮��Ĳ�����Ĭ�ϵľ������ꡣ  
    {  
  
        public void onShutter() {  
            // TODO Auto-generated method stub  
            Log.i(tag, "myShutterCallback:onShutter...");  
  
        }  
    };  
    PictureCallback myRawCallback = new PictureCallback()   
    // �����δѹ��ԭ���ݵĻص�,����Ϊnull  
    {  
  
        public void onPictureTaken(byte[] data, Camera camera) {  
            // TODO Auto-generated method stub  
            Log.i(tag, "myRawCallback:onPictureTaken...");  
  
        }  
    };  
    PictureCallback myJpegCallback = new PictureCallback()   
    //��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�  
    {  
  
        public void onPictureTaken(byte[] data, Camera camera) {  
            // TODO Auto-generated method stub  
            Log.i(tag, "myJpegCallback:onPictureTaken...");  
            if(null != data){  
                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ  
                myCamera.stopPreview();  
                isPreview = false;  
            }  
            //����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��  
            Matrix matrix = new Matrix();  
            matrix.postRotate((float)90.0);  
            Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);  
            //����ͼƬ��sdcard  
            if(null != rotaBitmap)  
            {  
                saveJpeg(rotaBitmap);  
            }  
  
            //�ٴν���Ԥ��  
            myCamera.startPreview();  
            isPreview = true;  
        }  
    };  
    //���հ����ļ���  
    public class PhotoOnClickListener implements OnClickListener{  
  
        public void onClick(View v) {  
            // TODO Auto-generated method stub  
            if(isPreview && myCamera!=null){  
                //myCamera.takePicture(myShutterCallback, null, myJpegCallback);    
                myCamera.takePicture(null, null, myJpegCallback);  
            }  
  
        }  
  
    }  
    /*����һ��Bitmap�����б���*/  
    public void saveJpeg(Bitmap bm){  
    	String mFileName = "";
    	if (BaseConfigure.getSdCard()) {
    		mFileName = HeartWayConstant.PIC_PATH;
		}else {
			BaseConfigure.ImagePath = new StringBuilder(getApplicationContext().getCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
			mFileName = BaseConfigure.ImagePath;
		}
    	
        File folder = new File(mFileName);  
        if(!folder.exists()) //����ļ��в������򴴽�  
        {  
            folder.mkdir();  
        }  
        long dataTake = System.currentTimeMillis();  
        String jpegName = mFileName + dataTake +".jpg";  
        //��ͼƬ���ָ�ֵ
        Log.i(tag, "saveJpeg:jpegName--" + jpegName);  
        //File jpegFile = new File(jpegName);  
        try {  
            FileOutputStream fout = new FileOutputStream(jpegName);  
            BufferedOutputStream bos = new BufferedOutputStream(fout);  
  
            //          //�����Ҫ�ı��С(Ĭ�ϵ��ǿ�960����1280),��ĳɿ�600����800  
            //          Bitmap newBM = bm.createScaledBitmap(bm, 600, 800, false);  
  
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);  
            bos.flush();  
            bos.close();  
            
            picPath.add(jpegName);
            Log.i(tag, "saveJpeg���洢��ϣ�");  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            Log.i(tag, "saveJpeg:�洢ʧ�ܣ�");  
            e.printStackTrace();  
        }  
    }  
  
    /*Ϊ��ʹͼƬ��ť���º͵���״̬��ͬ�����ù�����ɫ�ķ���.���µ�ʱ����ͼƬ��ɫ�䵭*/  
    public class MyOnTouchListener implements OnTouchListener{  
  
        public final  float[] BT_SELECTED=new float[]  
                { 2, 0, 0, 0, 2,  
            0, 2, 0, 0, 2,  
            0, 0, 2, 0, 2,  
            0, 0, 0, 1, 0 };                  
  
        public final float[] BT_NOT_SELECTED=new float[]  
                { 1, 0, 0, 0, 0,  
            0, 1, 0, 0, 0,  
            0, 0, 1, 0, 0,  
            0, 0, 0, 1, 0 };  
        public boolean onTouch(View v, MotionEvent event) {  
            // TODO Auto-generated method stub  
            if(event.getAction() == MotionEvent.ACTION_DOWN){  
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));  
                v.setBackgroundDrawable(v.getBackground());  
            }  
            else if(event.getAction() == MotionEvent.ACTION_UP){  
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));  
                v.setBackgroundDrawable(v.getBackground());  
                  
            }  
            return false;  
        }  
  
    }  
      
    @Override  
    public void onBackPressed()  
    //�����а����ؼ�ʱҪ�ͷ��ڴ�  
    {  
        // TODO Auto-generated method stub  
        super.onBackPressed();  
        CameraActivity.this.finish();  
    } 
    
    //������Ƭ��ť�¼�
    public OnClickListener saveBtnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			/**
			 * ����ֵ����activity
			 */
			final Intent intent=new Intent();
			int length = picPath.size();
			String[] picPathArr = new String[length];
			int i = 0;
			for (String string : picPath) {
				picPathArr[i] = string;
				i++;
			}
			picPath.clear();
			intent.putExtra("picPath",picPathArr);  
			CameraActivity.this.setResult(HeartWayConstant.SUCCESS,intent);  
			 
	        CameraActivity.this.finish();  
		}
	};
    
    
    /**
     * �������ж�
     */
    public boolean isVertical(){
    	boolean isVertical = true;
    	if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
    		isVertical = false;
    	}
    	return isVertical;
    }
}
