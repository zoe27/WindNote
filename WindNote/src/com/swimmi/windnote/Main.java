package com.swimmi.windnote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swimmi.common.DBHelper;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

	private Dialog menuDialog;		//�˵��Ի���
	private Dialog delDialog;		//ɾ���Ի���
	private GridView menuGrid;		//�˵�ѡ��
	private View menuView;			//�˵�ѡ����ͼ
	
	private ImageButton addBtn;		//���
	private ImageButton menuBtn;	//�����˵�
	private ImageButton searchBtn;	//����
	private ImageButton modeBtn;	//��ʾģʽ
	private ImageButton sortBtn;	//����
	
	private ListView notesLis;		//�����б�
	private GridView notesGrd;		//��������
	private TextView titleTxt;		//����
	private LinearLayout main;		//����
	private EditText keyTxt;		//�����
	private EditText againTxt;		//����ȷ�Ͽ�
	private EditText newTxt;		//�������
	private EditText searchTxt;		//������
	private TextView refreshTxt;	//ˢ�±�ǩ
	
	private Integer s_id;			//����ID
	private boolean sort_desc;		//�����ʶ
	private boolean mode_list;		//ģʽ��ʶ
	private long exitTime;			//�˳�ʱ��
	private int color;				//��ǰƤ����ɫ
	private String q_content;		//��������
	private String q_author;		//��������
	private String q_type;			//��������
	private HashMap<Integer,Integer> idMap;		//IDMap
	
	final int ACTION_SKIN=0;	//�˵�ѡ��
	final int ACTION_KEY=1;
	final int ACTION_SAY=2;
	final int ACTION_HELP=3;
	final int ACTION_ABOUT=4;
	final int ACTION_EXIT=5;
	private float mx;		//��Ļ��������
	private float my;
	
	private ColorPickerDialog cpDialog;		//��ɫѡ��Ի���
	private SharedPreferences sp;			//���ݴ洢
	private Dialog keyDialog;				//����Ի���
	private SQLiteDatabase wn;				//���ݿ�����
	@SuppressLint("UseSparseArrays")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		wn=Database(R.raw.windnote_zb);
		sp = getSharedPreferences("setting", 0);
		idMap=new HashMap<Integer, Integer>();		//��ȡ����ID�б�
        color=sp.getInt("color", getResources().getColor(R.color.blue));
		main=(LinearLayout)findViewById(R.id.main);
		main.setBackgroundColor(color);
		
		titleTxt=(TextView)findViewById(R.id.title_main);
		addBtn=(ImageButton)findViewById(R.id.add_btn);
		menuBtn=(ImageButton)findViewById(R.id.menu_btn);
		searchBtn=(ImageButton)findViewById(R.id.search_btn);
		modeBtn=(ImageButton)findViewById(R.id.mode_btn);
		sortBtn=(ImageButton)findViewById(R.id.sort_btn);
		notesLis=(ListView)findViewById(R.id.notes_lis);
		notesLis.setVerticalScrollBarEnabled(true);
		notesGrd=(GridView)findViewById(R.id.notes_grd);
		notesGrd.setVerticalScrollBarEnabled(true);
		@SuppressWarnings("deprecation")
		int width=getWindowManager().getDefaultDisplay().getWidth();	//��ȡ��Ļ���
		notesGrd.setNumColumns(width/120);			//�������񲼾�����
		
		q_content=sp.getString("q_content", "");
		q_author=sp.getString("q_author", "");
		q_type=sp.getString("q_type", "");
		
		ImageButton[] btns={addBtn,menuBtn,searchBtn,modeBtn,sortBtn};
		for(ImageButton btn:btns)
			btn.setOnClickListener(click);
		
		sort_desc=sp.getBoolean("sort", true);		//��ȡ����ʽ
		mode_list=sp.getBoolean("mode", true);		//��ȡ��ʾģʽ
		
		menuDialog = new Dialog(this,R.style.dialog);		//�Զ���˵�
		menuView = View.inflate(this, R.layout.gridmenu, null);
		menuDialog.setContentView(menuView);
		menuDialog.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)
				dialog.dismiss();
				return false;
			}
		});
		menuGrid=(GridView)menuView.findViewById(R.id.grid);
		menuGrid.setAdapter(getMenuAdapter());		//���ò˵���
		menuGrid.setOnItemClickListener(itemClick);
		searchTxt=(EditText)findViewById(R.id.search_txt);
		searchTxt.setBackgroundColor(color);
		searchTxt.addTextChangedListener(search);
		searchTxt.setText(sp.getString("word", ""));
		titleTxt.setOnClickListener(click);
		refreshTxt=(TextView)findViewById(R.id.refresh_txt);
		
		Long lastdate=sp.getLong("lastdate", new Date().getTime());		//���¼��±���ʱ��
		long passday=(int)(new Date().getTime()-lastdate)/(3600000*24);
		wn.execSQL("update notes set n_time=n_time-"+passday+" where n_time>0");
		sp.edit().putLong("lastdate",new Date().getTime()).commit();
		showItem(sort_desc,mode_list);
	}
	public OnTouchListener touch = new OnTouchListener(){		//�����¼���������ʾ���ڴ�����
		@Override
		public boolean onTouch(View view, MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			switch(e.getAction()){
			case MotionEvent.ACTION_DOWN:
				mx=x;
				my=y;
				break;
			case MotionEvent.ACTION_UP:
				float dx = x-mx;
				float dy = y-my;
				if(dy>30&&dx<30){			//����ˢ��
					refreshTxt.setVisibility(View.VISIBLE);
					showItem(sort_desc,mode_list);
					Handler refreshHand = new Handler();
					Runnable refreshShow=new Runnable()		
				    {
				        @Override
				        public void run()
				        {  
				        	refreshTxt.setVisibility(View.GONE);
				        }
				    };
					refreshHand.postDelayed(refreshShow, 500);
				}
			}
			return false;
		}
	};
	@Override
	public boolean onTouchEvent(MotionEvent e){			//�����¼���������ʾ���ⴥ����
		float x = e.getX();
		float y = e.getY();
		switch(e.getAction()){
		case MotionEvent.ACTION_DOWN:
			mx=x;
			my=y;
			break;
		case MotionEvent.ACTION_UP:
			float dx = x-mx;
			float dy = y-my;
			if(dy>30&&dx<30){
				refreshTxt.setVisibility(View.VISIBLE);
				showItem(sort_desc,mode_list);
				Handler refreshHand = new Handler();
				Runnable refreshShow=new Runnable()
			    {
			        @Override
			        public void run()
			        {  
			        	refreshTxt.setVisibility(View.GONE);
			        }
			    };
				refreshHand.postDelayed(refreshShow, 500);
			}
		}
		return true;
	}

	//�������
	public TextWatcher change = new TextWatcher() {		//���������¼�
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String key=keyTxt.getText().toString();
			String again=againTxt.getText().toString();
			if(key.length()>=6&&key.equals(again))
			{
				sp.edit().putString("key", key).commit();
				Toast.makeText(Main.this, getResources().getString(R.string.key_success)+key,Toast.LENGTH_LONG).show();
				keyDialog.dismiss();
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	public TextWatcher change2 = new TextWatcher() {		//�����޸��¼�
		@Override	
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String old=keyTxt.getText().toString();
			String key=newTxt.getText().toString();
			String keyAgain=againTxt.getText().toString();
			String rkey=sp.getString("key", "");
			if(old.equals(rkey)&&key.length()>=6&&key.equals(keyAgain))
			{
				sp.edit().putString("key", key).commit();
				Toast.makeText(Main.this, getResources().getString(R.string.key_success)+key,Toast.LENGTH_LONG).show();
				keyDialog.dismiss();
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	public TextWatcher change3 = new TextWatcher() {		//ȡ�������¼�
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String old=keyTxt.getText().toString();
			String rkey=sp.getString("key", "");
			if(old.equals(rkey))
			{
				sp.edit().remove("key").commit();
				Toast.makeText(Main.this, R.string.key_canceled,Toast.LENGTH_SHORT).show();
				keyDialog.dismiss();
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	private void setKey(){			//��������
		keyDialog=new Dialog(this,R.style.dialog);
		View keyView = View.inflate(this, R.layout.setkey, null);
		keyDialog.setContentView(keyView);
		keyTxt=(EditText)keyView.findViewById(R.id.key_txt);
		againTxt=(EditText)keyView.findViewById(R.id.again_txt);
		keyTxt.addTextChangedListener(change);
		againTxt.addTextChangedListener(change);
		keyDialog.show();
	}
	private void editKey(){			//�޸�����
		View keyView = View.inflate(this, R.layout.editkey, null);
		final Dialog dialog=new Dialog(this,R.style.dialog);
		dialog.setContentView(keyView);
		Button resetBtn=(Button)keyView.findViewById(R.id.reset_key);
		Button cancelBtn=(Button)keyView.findViewById(R.id.cancel_key);
		resetBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				resetKey();
				dialog.dismiss();
			}
		});
		cancelBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				cancelKey();
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	private void resetKey(){		//��������
		keyDialog=new Dialog(this,R.style.dialog);
		View keyView = View.inflate(this, R.layout.resetkey, null);
		keyDialog.setContentView(keyView);
		keyTxt=(EditText)keyView.findViewById(R.id.key_old);
		newTxt=(EditText)keyView.findViewById(R.id.key_new);
		againTxt=(EditText)keyView.findViewById(R.id.key_new_again);
		keyTxt.addTextChangedListener(change2);
		keyTxt.setOnFocusChangeListener(focusChange);
		newTxt.addTextChangedListener(change2);		
		againTxt.addTextChangedListener(change2);
		keyDialog.show();
	}
	private void cancelKey()			//ȡ������
	{
		keyDialog=new Dialog(this,R.style.dialog);
		View keyView = View.inflate(this, R.layout.cancelkey, null);
		keyDialog.setContentView(keyView);
		keyTxt=(EditText)keyView.findViewById(R.id.key_old);
		keyTxt.addTextChangedListener(change3);
		keyDialog.show();
	}	
	private void showItem(Boolean desc,Boolean list){		//��ʾ����
		String word=searchTxt.getText().toString().trim();
		SimpleAdapter adapter = new SimpleAdapter(Main.this,getData(desc,word),list?R.layout.listitem:R.layout.griditem,
				new String[]{"id","title","content","time","count","lock","postdate"},
				new int[]{R.id.id,R.id.title,R.id.content,R.id.time,R.id.count,R.id.lock,R.id.postdate});
		sortBtn.setImageResource(desc?R.drawable.asc:R.drawable.desc);
		modeBtn.setImageResource(list?R.drawable.grid:R.drawable.list);
		if(list)
		{	
			notesLis.setVisibility(View.VISIBLE);
			notesGrd.setVisibility(View.GONE);
			notesLis.setAdapter(adapter);		//���ɼ����б�
			notesLis.setOnItemClickListener(new OnItemClickListener(){		//��������¼�
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					ListView listView =(ListView)parent;		
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);  
					wn.execSQL("update notes set n_count=n_count-1 where n_count>0 and id="+idMap.get(position));	//���¿��������
					Intent intent=new Intent(Main.this,Note.class);
					intent.putExtra("data", map);
					startActivity(intent);
					finish();
				}
			});
			notesLis.setOnTouchListener(touch);
			notesLis.setOnItemLongClickListener(longClick);			//���³����¼�
			titleTxt.setText(getResources().getString(R.string.app_name)+"\t["+notesLis.getCount()+"]");
		}
		else{
			notesGrd.setVisibility(View.VISIBLE);
			notesLis.setVisibility(View.GONE);
			notesGrd.setAdapter(adapter);		//���ɼ�������
			notesGrd.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					GridView gridView =(GridView)parent;		
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(position);  
					wn.execSQL("update notes set n_count=n_count-1 where n_count>0 and id="+idMap.get(position));
					Intent intent=new Intent(Main.this,Note.class);
					intent.putExtra("data", map);
					startActivity(intent);
					finish();
				}
			});
			notesGrd.setOnTouchListener(touch);
			notesGrd.setOnItemLongClickListener(longClick);			//���³����¼�
			titleTxt.setText(getResources().getString(R.string.app_name)+"\t["+notesGrd.getCount()+"]");
		}
	}
	public OnFocusChangeListener focusChange=new OnFocusChangeListener(){		//����ı��¼�
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText txt=(EditText)v;
			String rkey=sp.getString("key", "");
			if(!v.hasFocus()&&!txt.getText().toString().equals(rkey)&&!txt.getText().toString().equals(""))
				Toast.makeText(Main.this, R.string.wrong_key, Toast.LENGTH_SHORT).show();		//��ʾԭ�������
		}
	};
	private void chooseColor(){			//ѡ��Ƥ��
		Dialog dialog=new Dialog(this,R.style.dialog);
		View colorView = View.inflate(this, R.layout.gridmenu, null);
		dialog.setContentView(colorView);
		GridView colorGrid=(GridView)colorView.findViewById(R.id.grid);
		colorGrid.setNumColumns(2);
		colorGrid.setAdapter(getColorAdapter());		//����Ƥ��ѡ��
		colorGrid.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
			{
				if(getResources().getColor(My.colors[position])!=color)
				{
					if(position<My.colors.length-1)			//ѡ���˵�ǰƤ��
					{						
						sp.edit().putInt("color", getResources().getColor(My.colors[position])).commit();
						Intent intent=new Intent(Main.this,Welcome.class);
						intent.putExtra("needKey", false);
						startActivity(intent);
						finish();
					}
					else if(position==My.colors.length-1)		//ѡ�����µ�Ƥ��
					{
						cpDialog = new ColorPickerDialog(Main.this, color,   
		                        getResources().getString(R.string.word_confirm),   
		                        new ColorPickerDialog.OnColorChangedListener() { 
		                    @Override  
		                    public void colorChanged(int c) 
		                    {  
								sp.edit().putInt("color", c).commit();
								Intent intent=new Intent(Main.this,Welcome.class);
								intent.putExtra("needKey", false);
								startActivity(intent);
								finish();
		                    }
		                });
						cpDialog.getWindow().setBackgroundDrawableResource(R.drawable.list_focused);
		                cpDialog.show();  
					}
				}
				else
				{
					Toast.makeText(Main.this, R.string.now_skin, Toast.LENGTH_SHORT).show();
				}
			}
		});
		dialog.show();
	}
	private SimpleAdapter getColorAdapter()			//��ȡƤ���б�
	{
		SimpleAdapter adapter = new SimpleAdapter(this,getColor(),R.layout.menuitem,
				new String[]{"txt"},
				new int[]{R.id.item_txt});
		return adapter;
	}
	private SimpleAdapter getMenuAdapter()			//��ȡ�˵��б�
	{
		SimpleAdapter adapter = new SimpleAdapter(this,getMenu(),R.layout.menuitem,
				new String[]{"img","txt"},
				new int[]{R.id.item_img,R.id.item_txt});
		return adapter;
	}
	private List<Map<String, Object>> getColor() {			//��ȡ��ɫ�б�
		String[] txts=My.cs;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<txts.length;i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("txt", txts[i]);
			list.add(map);
		}
		return list;
	}
	private List<Map<String, Object>> getMenu() {			//��ȡ�˵�
		int[] imgs={R.drawable.skin,R.drawable.key,R.drawable.say,R.drawable.help,R.drawable.about,R.drawable.exit};
		int[] txts={R.string.action_skin,R.string.action_key,R.string.action_say,R.string.action_help,R.string.action_about,R.string.action_exit};
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<imgs.length;i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("img", imgs[i]);
			map.put("txt", getResources().getString(txts[i]));
			list.add(map);
		}
		return list;
	}
	private List<Map<String, Object>> getData(Boolean desc, String word) {		//��ȡ��������
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Cursor cursor=wn.rawQuery("select id,n_title,n_content,n_time,n_count,n_lock,lat, lng,pic_list, audio_list,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes where n_time!=0 and n_count!=0 order by n_postdate "+(desc!=true?"":"desc"), null);
		if(word.length()>0)
			cursor=wn.rawQuery("select id,n_title,n_content,n_time,n_count,n_lock,lat, lng,pic_list, audio_list,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes where n_time!=0 and n_count!=0 and (n_title||'`'||n_content||'`'||n_postdate||'`'||n_postday) like '%"+word+"%' order by n_postdate "+(desc!=true?"":"desc"), null);
		if(word.equals("#all"))
			cursor=wn.rawQuery("select id,n_title,n_content,n_time,n_count,n_lock,lat, lng,pic_list, audio_list,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes order by n_postdate "+(desc!=true?"":"desc"), null);
		sp.edit().putString("word", word).commit();
		int pos=0;
		while(cursor.moveToNext())
		{
			int n_id=cursor.getInt(cursor.getColumnIndex("id"));
			idMap.put(pos, n_id);
			pos+=1;
			String n_title=cursor.getString(cursor.getColumnIndex("n_title"));
			String n_content=cursor.getString(cursor.getColumnIndex("n_content"));
			Integer n_time=cursor.getInt(cursor.getColumnIndex("n_time"));
			Integer n_count=cursor.getInt(cursor.getColumnIndex("n_count"));
			Boolean n_lock=cursor.getInt(cursor.getColumnIndex("n_lock"))>0;
			Integer n_postdate=cursor.getInt(cursor.getColumnIndex("n_postday"));
			
			//��γ��
			double lat = cursor.getDouble(cursor.getColumnIndex("lat"));
			double lng = cursor.getDouble(cursor.getColumnIndex("lng"));
			//��Ƭ��¼��
			String picList = cursor.getString(cursor.getColumnIndex("pic_list"));
			String audioList = cursor.getString(cursor.getColumnIndex("audio_list"));
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", n_id);
			map.put("title", n_title);
			map.put("content", n_content);
			map.put("time", n_time);
			map.put("count", n_count);
			map.put("lock", n_lock);
			map.put("postdate", n_postdate==0?getResources().getString(R.string.word_today):n_postdate+getResources().getString(R.string.word_ago));
			
			//��γ��
			map.put("lat", lat);
			map.put("lng", lng);
			
			map.put("picList", picList);
			map.put("audioList", audioList);
			
			list.add(map);
		}
		cursor.close();
		return list;
	}
	public SQLiteDatabase Database(int raw_id) {		//���ݿ�����
        try {
        	int BUFFER_SIZE = 100000;
        	String DB_NAME = DBHelper.DATA_BASE;//"windnote.db"; 
        	String PACKAGE_NAME = "com.swimmi.windnote";
        	String DB_PATH = "/data"
                + Environment.getDataDirectory().getAbsolutePath() + "/"
                + PACKAGE_NAME+"/databases/";
        	File destDir = new File(DB_PATH);
        	  if (!destDir.exists()) {
        	   destDir.mkdirs();
        	  }
        	String file=DB_PATH+DB_NAME;
        	if (!(new File(file).exists())) {
                InputStream is = this.getResources().openRawResource(
                        raw_id);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file,null);
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
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if((System.currentTimeMillis()-exitTime)>2000){
				Toast.makeText(Main.this, R.string.exit_hint, Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis(); 
				return true;
			}
			else 
			{
	            finish();
	            System.exit(0);
	        }
		}
		return false;
	}
	private OnItemClickListener itemClick=new OnItemClickListener(){			//�˵�����¼�
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch(position){
			case ACTION_SKIN:
				chooseColor();
				break;
			case ACTION_KEY:
				if(!sp.contains("key"))
					setKey();
				else
					editKey();
				break;
			case ACTION_SAY:
				say();
				break;
			case ACTION_HELP:
				help();
				break;
			case ACTION_ABOUT:
				about();
				break;
			case ACTION_EXIT:
				finish();
				System.exit(0);
				break;
			}
		}
	};
	private void help(){		//����
		wn.execSQL("update notes set n_count=1,n_postdate=datetime('now','localtime') where id=1");
		showItem(sort_desc,mode_list);		//��ʾʹ��˵��
		menuDialog.dismiss();
	}
	private void about(){		//����
		Dialog aboutDialog=new Dialog(this,R.style.dialog);
		View aboutView = View.inflate(this, R.layout.aboutme, null);
		aboutDialog.setContentView(aboutView);
		aboutDialog.show();
	}
	@SuppressLint("SimpleDateFormat")
	private void say(){			//����
		Intent intent= new Intent(Main.this,Add.class);
		Bundle data = new Bundle();
		data.putString("title",getResources().getString(R.string.word_my)+q_type+getResources().getString(R.string.action_say)+"\t\t"+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		data.putString("content","        "+q_author+getResources().getString(R.string.word_said)+q_content+"\r\n");
		intent.putExtras(data);
		startActivity(intent);
		finish();
	}
	private void delete(){		//ɾ������
		View deleteView = View.inflate(this, R.layout.deletenote, null);
		delDialog=new Dialog(this,R.style.dialog);
		delDialog.setContentView(deleteView);
		Button yesBtn=(Button)deleteView.findViewById(R.id.delete_yes);
		Button noBtn=(Button)deleteView.findViewById(R.id.delete_no);
		TextView goneTimeTxt=(TextView)deleteView.findViewById(R.id.gone_time);
		TextView goneCountTxt=(TextView)deleteView.findViewById(R.id.gone_count);
		Cursor cursor=wn.rawQuery("select n_time,n_count from notes where id="+s_id,null);
		while(cursor.moveToNext()){
			int time=cursor.getInt(cursor.getColumnIndex("n_time"));
			int count=cursor.getInt(cursor.getColumnIndex("n_count"));
			String time_txt=time>0?String.valueOf(time):"n";
			String count_txt=count>0?String.valueOf(count):"n";
			goneTimeTxt.setText(R.string.left_txt);
			goneCountTxt.setText(time_txt+getResources().getString(R.string.word_time)+count_txt+getResources().getString(R.string.word_count));
		}
		yesBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				wn.execSQL("delete from notes where id="+s_id);
				delDialog.dismiss();
				showItem(sort_desc,mode_list);
			}
		});
		noBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				delDialog.dismiss();
			}
		});
		delDialog.show();
	}
	private OnItemLongClickListener longClick= new OnItemLongClickListener()		//����ɾ��
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			s_id=idMap.get(position);
			delete();
			return false;
		}
	};
	private OnClickListener click=new OnClickListener(){			//����¼�����

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.add_btn:		//�½�����
				Intent intent= new Intent(Main.this,Add.class);
				if(getIntent().hasExtra("title"))
					intent.putExtras(getIntent().getExtras());
				startActivity(intent);
				finish();
				break;
			case R.id.menu_btn:		//�˵�
				if (menuDialog == null) 
				{
					menuDialog = new Dialog(Main.this,R.style.dialog);
					menuDialog.show();
				}
				else
				{
					menuDialog.show();
				}
				break;
			case R.id.search_btn:		//����
				showHide(searchTxt);
				Add.focus(searchTxt,true);
				break;
			case R.id.mode_btn:			//ģʽ
				mode_list=!mode_list;
				sp.edit().putBoolean("mode", mode_list).commit();
				showItem(sort_desc,mode_list);
				break;
			case R.id.sort_btn:			//����
				/*sort_desc=!sort_desc;
				sp.edit().putBoolean("sort", sort_desc).commit();
				showItem(sort_desc,mode_list);*/
				//���Ϊ��ͼ�¼�
				Intent intentFoot = new Intent(Main.this,FootPrint.class);
				startActivity(intentFoot);
				break;
			case R.id.title_main:		//���������
				searchTxt.setText("");
				sp.edit().remove("word").commit();
				showItem(sort_desc, mode_list);
			}
		}
	};
	private TextWatcher search=new TextWatcher(){		//�����¼�
		@Override
		public void afterTextChanged(Editable arg0) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			showItem(sort_desc, mode_list);
		}
	};
	private void showHide(View view){		//����Ԫ��
		if(view.getVisibility()==View.VISIBLE)
			view.setVisibility(View.INVISIBLE);
		else
			view.setVisibility(View.VISIBLE);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {		//���ò˵�
		menu.removeItem(0);
		if (menuDialog == null) 
		{
			menuDialog = new Dialog(Main.this,R.style.dialog);
			menuDialog.show();
		}
		else 
		{
			menuDialog.show();
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem mi){
		return super.onOptionsItemSelected(mi);
	}
}
