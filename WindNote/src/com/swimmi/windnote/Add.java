package com.swimmi.windnote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swimmi.camera.CameraActivity;
import com.swimmi.common.DBHelper;
import com.swimmi.common.HeartWayConstant;
import com.swimmi.common.LocationHelper;
import com.swimmi.sound.RecordButton;
import com.swimmi.sound.RecordButton.OnFinishedRecordListener;

public class Add extends Activity {

	private LinearLayout add; // 布局容器
	private EditText noteTxt; // 内容框
	private EditText titleTxt; // 标题框
	private ImageButton backBtn; // 返回
	private ImageButton lockBtn; // 加锁
	private ImageButton goneBtn; // 放飞
	private ImageButton saveBtn; // 保存
	private TextView lengthTxt; // 长度标签
	private Button clearBtn; // 清空标签
	private Dialog goneDialog; // 放飞对话框

	private boolean lock = false; // 是否加锁
	private int n_time = -1; // 限制保存天数
	private int n_count = -1; // 限制浏览次数
	private int color; // 当前皮肤颜色

	// add by zoe 2014-06-29
	private RecordButton audioBtn; // 录音按钮
	private ImageButton picBtn;// 拍照按钮
	
	//add by zoe 2014-07-02
	private LinearLayout hsv;//录音列表
	//保存拍下来的照片
	private LinearLayout picList = null;

	private SharedPreferences sp; // 数据存储
	private SQLiteDatabase wn; // 数据库
	// 保存录音文件
	private ArrayList<String> audioRecordFiles = new ArrayList<String>();
	private ArrayList<String> picRecordFiles = new ArrayList<String>();
	
	private String picListStr = "";//图片文件
	private String audioListStr = "";//录音文件

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);

		wn = Database(R.raw.windnote_zb); // 连接数据库
		sp = getSharedPreferences("setting", 0); // 获取设置数据
		color = sp.getInt("color", getResources().getColor(R.color.blue)); // 获取皮肤颜色

		add = (LinearLayout) findViewById(R.id.add);
		add.setBackgroundColor(color);
		noteTxt = (EditText) findViewById(R.id.note_txt);
		titleTxt = (EditText) findViewById(R.id.title_txt);
		lengthTxt = (TextView) findViewById(R.id.length_txt);
		clearBtn = (Button) findViewById(R.id.clear_btn);
		noteTxt.addTextChangedListener(change);

		audioBtn = (RecordButton) findViewById(R.id.note_audio);
		audioBtn.setOnFinishedRecordListener(finishLister);
		picBtn = (ImageButton) findViewById(R.id.note_pic);
		hsv = (LinearLayout) findViewById(R.id.audioList);
		picList = (LinearLayout) findViewById(R.id.note_picList);

		if (getIntent().hasExtra("title")) // 还原未保存的数据
		{
			Bundle data = getIntent().getExtras();
			if (data.containsKey("title"))
				titleTxt.setText(data.getString("title"));
			if (data.containsKey("content"))
				noteTxt.setText(data.getString("content"));
			if (data.containsKey("lock"))
				lock = data.getBoolean("lock");
		}

		backBtn = (ImageButton) findViewById(R.id.back_btn);
		lockBtn = (ImageButton) findViewById(R.id.lock_btn);
		goneBtn = (ImageButton) findViewById(R.id.gone_btn);
		saveBtn = (ImageButton) findViewById(R.id.save_btn);

		clearBtn.setOnClickListener(new OnClickListener() { // 清空内容事件
			@Override
			public void onClick(View view) {
				View deleteView = View.inflate(Add.this, R.layout.deletenote,
						null);
				final Dialog clearDialog = new Dialog(Add.this, R.style.dialog);
				clearDialog.setContentView(deleteView);
				Button yesBtn = (Button) deleteView
						.findViewById(R.id.delete_yes);
				yesBtn.setText(R.string.clear_note);
				Button noBtn = (Button) deleteView.findViewById(R.id.delete_no);
				noBtn.setText(R.string.clear_cancel);
				yesBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						titleTxt.setText("");
						noteTxt.setText("");
						clearDialog.dismiss();
					}
				});
				noBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						clearDialog.dismiss();
					}
				});
				clearDialog.show();
			}
		});
		// 初始化按钮监听事件
		addClickListener();
		setLock(lock);

	}

	// 添加按钮的监听事件
	private void addClickListener() {
		ImageButton[] btns = { backBtn, lockBtn, goneBtn, saveBtn, picBtn };
		for (ImageButton btn : btns)
			// 添加按钮事件监听
			btn.setOnClickListener(click);
	}

	public void setLock(Boolean b) { // 加锁（解锁）
		lockBtn.setImageResource(b == true ? R.drawable.unlock
				: R.drawable.lock);
		EditText[] txts = { titleTxt, noteTxt };
		for (EditText txt : txts) {
			focus(txt, !b);
			txt.setTextColor(b == true ? getResources().getColor(
					R.color.darkgray) : color);
			txt.setBackgroundResource(b == true ? R.color.gray : R.color.white);
		}
		lengthTxt.setTextColor(b == true ? getResources().getColor(
				R.color.darkgray) : color);
		lengthTxt.setBackgroundResource(b == true ? R.color.gray
				: R.color.white);
		clearBtn.setTextColor(b == true ? getResources().getColor(
				R.color.darkgray) : color);
		clearBtn.setEnabled(!b);
		clearBtn.setBackgroundResource(b == true ? R.color.gray : R.color.white);
	}

	public static void focus(EditText view, Boolean b) { // 失去（得到）焦点
		view.setCursorVisible(b);
		view.setFocusable(b);
		view.setFocusableInTouchMode(b);
		if (b == true)
			view.requestFocus();
		else
			view.clearFocus();
		Spannable text = (Spannable) view.getText();
		Selection.setSelection(text, text.length());
	}

	private void save() // 保存记事
	{
		String n_title = titleTxt.getText().toString().trim();
		if (n_title.length() == 0)
			n_title = "无标题";
		String n_content = noteTxt.getText().toString().trim();
		Boolean n_lock = lock;
		
		//经纬度
		double lat = LocationHelper.location.getLatitude();
		double lng = LocationHelper.location.getLongitude();
		
		//照片
		for (String pic : picRecordFiles) {
			picListStr += pic.concat(";");
		}
		//录音
		for (String audio : audioRecordFiles) {
			audioListStr += audio.concat(";");
		}
		
		if (n_content.trim().length() > 0) {
			wn.execSQL(
					"insert into notes(n_title,n_content,n_time,n_count,n_lock,lat,lng,pic_list,audio_list) values(?,?,?,?,?,?,?,?,?)",
					new Object[] { n_title, n_content, n_time, n_count, n_lock, lat, lng, picListStr,audioListStr });
			Toast.makeText(Add.this, R.string.note_saved, Toast.LENGTH_SHORT)
					.show();
			sp.edit().remove("time").commit();
			sp.edit().remove("count").commit();
			Intent intent = new Intent(Add.this, Main.class);
			startActivity(intent);
			finish();
		} else
			Toast.makeText(Add.this, R.string.note_null, Toast.LENGTH_SHORT)
					.show();
	}

	private void back() { // 返回主界面
		Intent intent = new Intent(Add.this, Main.class);
		String title = titleTxt.getText().toString().trim();
		String content = noteTxt.getText().toString().trim();
		if (title.length() > 0 || content.length() > 0) // 传递未保存的数据
		{
			Bundle data = new Bundle();
			data.putString("title", title);
			data.putString("content", content);
			data.putBoolean("lock", lock);
			intent.putExtras(data);
		}
		startActivity(intent);
		finish();
	}

	private void gone() { // 放飞
		goneDialog = new Dialog(this, R.style.dialog);
		View goneView = View.inflate(this, R.layout.gone, null);
		goneDialog.setContentView(goneView);
		final EditText timeTxt = (EditText) goneView
				.findViewById(R.id.time_txt);
		final EditText countTxt = (EditText) goneView
				.findViewById(R.id.count_txt);
		if (!sp.getString("time", "").equals("-1"))
			timeTxt.setText(sp.getString("time", ""));
		if (!sp.getString("count", "").equals("-1"))
			countTxt.setText(sp.getString("count", ""));
		Button confirmBtn = (Button) goneView.findViewById(R.id.gone_confirm);
		Button cancelBtn = (Button) goneView.findViewById(R.id.gone_cancel);
		confirmBtn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线效果
		cancelBtn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		confirmBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (timeTxt.getText().length() > 0)
					n_time = Integer.parseInt(timeTxt.getText().toString());
				if (countTxt.getText().length() > 0)
					n_count = Integer.parseInt(countTxt.getText().toString());
				sp.edit().putString("time", String.valueOf(n_time)).commit();
				sp.edit().putString("count", String.valueOf(n_count)).commit();
				goneDialog.dismiss();
			}
		});
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goneDialog.dismiss();
			}
		});
		goneDialog.show();
	}
	
	//拍照
	public void startCamera(){
		Intent intent = new Intent(Add.this, CameraActivity.class);
		startActivityForResult(intent, HeartWayConstant.RESULT_CAPTURE_PICTURE);
		//startActivity(intent);
	}

	public SQLiteDatabase Database(int raw_id) { // 数据库连接，从raw中读取文件
		try {
			int BUFFER_SIZE = 100000;
			String DB_NAME = DBHelper.DATA_BASE;//"windnote.db";
			String PACKAGE_NAME = "com.swimmi.windnote";
			String DB_PATH = "/data"
					+ Environment.getDataDirectory().getAbsolutePath() + "/"
					+ PACKAGE_NAME + "/databases/";
			File destDir = new File(DB_PATH);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			String file = DB_PATH + DB_NAME;
			if (!(new File(file).exists())) {
				InputStream is = this.getResources().openRawResource(raw_id);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file, null);
			return db;
		} catch (FileNotFoundException e) {
			Log.e("Database", "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Database", "IO exception");
			e.printStackTrace();
		}
		return null;
	}

	// 录音按钮停止事件
	private OnFinishedRecordListener finishLister = new OnFinishedRecordListener() {

		@Override
		public void onFinishedRecord(String audioPath) {
			//Toast.makeText(Add.this, audioPath, Toast.LENGTH_LONG).show();
			// 以分号隔开
			/*ImageView imageView = new ImageView(getApplicationContext());
			imageView.setBackgroundResource(R.id.note_audio);
			imageView.setContentDescription(audioPath);*/
			ImageButton audio = new ImageButton(getApplicationContext());
			audio.setContentDescription(audioPath);
			audio.setImageResource(R.id.note_audio);
			//audio.getBackground().setAlpha(R.color.transparent);
			//audio.setBackgroundColor(R.color.transparent);
			//hsv.setVisibility(0);
			hsv.addView(audio);
			audioRecordFiles.add(audioPath);
		}
	};

	private TextWatcher change = new TextWatcher() { // 文本改变监听
		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() > 0) {
				lengthTxt.setVisibility(View.VISIBLE);
				clearBtn.setVisibility(View.VISIBLE);
				lengthTxt.setText(String.valueOf(s.length()));
			} else {
				lengthTxt.setVisibility(View.GONE);
				clearBtn.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) // 返回事件重写
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		}
		return false;
	}

	private OnClickListener click = new OnClickListener() { // 点击事件监听

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				back();
				break;
			case R.id.lock_btn:
				lock = !lock;
				setLock(lock);
				break;
			case R.id.gone_btn:
				gone();
				break;
			case R.id.save_btn:
				save();
				break;
			case R.id.note_pic:
				startCamera();
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	/**
	 * 获取返回值
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case HeartWayConstant.RESULT_CAPTURE_RECORDER_SOUND:
			if(resultCode == RESULT_OK){
				Uri uri = data.getData();
				Cursor cursor=this.getContentResolver().query(uri, null, null, null, null);
				if(cursor.moveToNext()){
					String audioRecordPath = cursor.getString(cursor.getColumnIndex("_data"));
					Toast.makeText(this, audioRecordPath, Toast.LENGTH_LONG).show();
				}
			}
			break;
		//拍照返回
		case HeartWayConstant.RESULT_CAPTURE_PICTURE:
			if (resultCode == HeartWayConstant.SUCCESS
					|| data.getSerializableExtra("picPath") != null) {
				String[] picPath = (String[]) data
						.getSerializableExtra("picPath");
				
				//给照片放到一个列表中
				setPicToList(picPath, picList, getApplicationContext());
			}
			break;
		case HeartWayConstant.VIDEO_RECORD_RESPONSE:
			if (resultCode == HeartWayConstant.SUCCESS
					|| data.getSerializableExtra("videoName") != null) {
				String videoName = (String) data.getSerializableExtra("videoName");

				// 给照片放到一个列表中
				Toast.makeText(this, videoName, Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;

		}
	}
	
	// 将照片放到一个列表中
	private void setPicToList(String[] plist, LinearLayout layout,
			Context context) {
		StringBuffer picSb = new StringBuffer();
		for (String string : plist) {
			ImageView imageView = new ImageView(context);
			FileInputStream fis;
			try {
				fis = new FileInputStream(string);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);
				imageView.setImageBitmap(bitmap);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				layout.addView(imageView);
				
				picRecordFiles.add(string);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

}
