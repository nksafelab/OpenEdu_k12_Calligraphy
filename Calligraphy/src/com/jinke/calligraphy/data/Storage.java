package com.jinke.calligraphy.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.template.WolfTemplate;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.single.BitmapCount;

public class Storage {
	
	private MyView view;
	
	private static final String TAG = "Storage";
	
//	public static final String FILE_PATH_HEAD = "/mnt/extsd/";
	public static final String FILE_PATH_HEAD = Start.getStoragePath() + "/";
	
	private static final String WOLFPATH = FILE_PATH_HEAD + "notepad.wol";
	private static final String XMLPATH = FILE_PATH_HEAD + "test_apk.xml";
	
	public static final int CURSOR = 1;
	public static final int FREE = 0;
	
	private int status;
	private File   wolfFile;
	
	private static final int	FILE_LENGTH_POS = 19;
	private static final int 	HAND_INFO_LENGTH_POS = 113;
	private static final int	HAND_INFO_ADDRESS = 117;
	
	public static  int 	HW_HEAD_INFO_ADDRESS;
	
	public static int WOL_FILE_LEN;
	private static final int 	HEAD_OFFSET = 256;

	private static int	curFileOffset = HEAD_OFFSET;
	private static int 	handimgID;
	
	public Bitmap b = null;
	public Bitmap indexB = null;
	
//	private FileOutputStream fos = null;
	private RandomAccessFile raf = null;
	private DataOutputStream dop = null;
	private XMLCreater  creater;
	
	public Storage(MyView v){
		Log.i(TAG, "constructor");
		Log.e("storage", "Storage() init");
		view = v;
		try {
			Log.e("storage", "Storage() init try");
//			fos = new FileOutputStream(new File(WOLFPATH));
			wolfFile = new File(WOLFPATH);
			
			
			
			WOL_FILE_LEN = (int) wolfFile.length();
			Log.e("storage", "Storage() init1-");
			
			//-----------手机上到这里不运行了
			raf = new RandomAccessFile(wolfFile, "rw");
			
			Log.e("storage", "Storage() init0-");
			HW_HEAD_INFO_ADDRESS = readHwHeadInfoAddress();
//			try {
//				raf.seek(WOL_FILE_LEN);
//				byte[] buffer = new byte[128];
//				raf.write(buffer);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block 
//				e.printStackTrace();
//			}
			Log.e("storage", "Storage() init0");
			init();
			if(b == null) {
				//b = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
				//b = Bitmap.createBitmap(Start.SCREEN_WIDTH/2, Start.SCREEN_HEIGHT/2, Bitmap.Config.ARGB_4444);
				b = Bitmap.createBitmap( Start.SCREEN_HEIGHT-1000, Start.SCREEN_WIDTH-1000,Bitmap.Config.ARGB_4444);
				BitmapCount.getInstance().createBitmap("create b for draw shareImage");
			}
			if(indexB == null) {
				//ly
				//indexB = Bitmap.createBitmap(600, 160, Bitmap.Config.ARGB_4444);
				indexB = Bitmap.createBitmap(1600, 160, Bitmap.Config.ARGB_4444);
				BitmapCount.getInstance().createBitmap("create indexB for draw shareImage");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init(){
		try {
			dop = new DataOutputStream(new FileOutputStream(XMLPATH));
			creater = new XMLCreater("utf-8", dop);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void save(){
		init();
		Log.i(TAG, "makeHandItem");
		//构造手写结点
		HandWrite handwrite = makeHandWriteData();
		Log.i(TAG, "updateIndexTable");
//		updateIndexTable(item);
		//将手写结点写入XML文件
		writeToXML(handwrite);
		Log.i(TAG, "appendHandwrite");
		//将构造好的XML临时文件添加到Wol文件
		appendHandWrite();
		//更新128字节的头信息
//		updateHead();
	}
	public synchronized void save(int status){
		this.status = status;
		init();
		Log.i(TAG, "makeHandItem");
		//构造手写结点
		HandWrite handwrite = makeHandWriteData();
		Log.i(TAG, "updateIndexTable");
//		updateIndexTable(item);
		//将手写结点写入XML文件
		writeToXML(handwrite);
		Log.i(TAG, "appendHandwrite");
		//将构造好的XML临时文件添加到Wol文件
		appendHandWrite();
		//更新128字节的头信息
//		updateHead();
	}
	
	public int readHwHeadInfoAddress(){
		int addr;
		try {
			raf.seek(HAND_INFO_ADDRESS);
			addr = raf.readInt();
			Log.i(TAG, "addr:" + Integer.toHexString(addr) + " reverse:" + Integer.toHexString(Integer.reverseBytes(addr)));
			return Integer.reverseBytes(addr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public void updateHead(){
		try {
			raf.seek(FILE_LENGTH_POS);
			raf.writeInt((int)wolfFile.length());
			raf.seek(HAND_INFO_LENGTH_POS);
			raf.writeInt((int)(wolfFile.length() - WOL_FILE_LEN));
			raf.seek(HAND_INFO_ADDRESS);
			raf.writeInt(WOL_FILE_LEN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void updateIndexTable(HandItem item){
		try {
			raf.seek(curFileOffset);
//			//写入文件绝对路径
//			raf.write(getFilePath().getBytes());
//			curFileOffset += FILEPATH_MAX_LENGTH;
//			raf.seek(curFileOffset);
//			//写入当前页号
//			raf.write(getPageNum().getBytes());
//			curFileOffset += FILE_PAGENUM_LENGTH;
//			raf.seek(curFileOffset);
//			//写入HandWrite的地址
//			Log.i(TAG, "address:" + Integer.toHexString((int)wolfFile.length()));
//			raf.writeInt((int)wolfFile.length());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void appendHandWrite(){
		
		
		Log.i(TAG, "append");
		byte[] buffer = new byte[512];
		int len;
		try {
			WolfStructure ws = new WolfStructure(new File(WOLFPATH));
			raf.seek(ws.getAddressInfo(getFilePath(), getPageNum()));
			Log.i(TAG, "append:" + raf.getFilePointer());
			FileInputStream fis = new FileInputStream(XMLPATH);
			while((len = fis.read(buffer)) != -1) {
				raf.write(buffer, 0, len);
			}
			fis.close();
			Log.i(TAG, "append:" + wolfFile.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HandWrite makeHandWriteData() {
		
		Log.i(TAG, "makeHandWriteData");
		HandWrite handwrite = new HandWrite();
		handwrite.setFormat(1);
		
		List<HandItem> itemList = new ArrayList<HandItem>();
		//此处以后需要判断是否同一本书，目前只有一页，故只有一个HandItem
		makeAndAddHandItem(itemList);
		handwrite.setHandItemList(itemList);
		return handwrite;
	}
	
	public void makeAndAddHandItem(List<HandItem> itemList){
		
		//下面开始构造每一页
		Log.i(TAG, "makeHandItem");
		handimgID = 0;
		
		HandItem handitem = new HandItem();
		handitem.setUrl(getFilePath());
		handitem.setOffset(0);
		handitem.setPage(1);
		handitem.setUserid(1234);
		handitem.setTime(Calendar.getInstance().getTimeInMillis());
		List<HandImg> handImgList = new ArrayList<HandImg>();
		if(view.hasTouch == true)
			makeAndAddFreeDrawHandImg(handImgList);
		LinkedList<EditableCalligraphyItem> editCharList = view.cursorBitmap.getCharList();
		if(editCharList.size() > 0) {
			makeAndAddCursorDrawHandImg(handImgList, editCharList);
		}
		handitem.setHandImgList(handImgList);
		itemList.add(handitem);
		Log.i(TAG, "addHandItem");
	}
	
	public void makeAndAddFreeDrawHandImg(List<HandImg> handImgList){
		
		Log.i(TAG, "makeFreeDrawHandImg");
		HandImg handimg = new HandImg();
		handimg.setForm(0);
		handimg.setLayout(0);
		handimg.setHid(++handimgID);
		handimg.setImgtime(Calendar.getInstance().getTimeInMillis());
		handimg.setLevel(0);
		
		//以下四项，只有光标态时候会用到
		handimg.setArea(new Rect(0,0,0,0));
		handimg.setChartype(0);
		handimg.setSequence((short) 0);
		handimg.setImgvector(0);
		
		Img img = new Img();
		img.setType(1);
		img.setSize(1);
		img.setX(0);
		img.setY(0);
		img.setWidth(view.getMBitmap().getWidth());
		img.setHeight(view.getMBitmap().getHeight());
		img.setCompact(1);
		img.setBitcount(4);
		img.setLength(0);  //暂时为0，具体写xml时赋值
		img.setKind(1);
		img.setFormat(2);  //JPEG
		img.setPagecontrol(0);
		img.setImgpart(new Rect(0,0,0,0));
		img.setBitmap(getCurBitmapRef());
		handimg.setImg(img);

		Log.i(TAG, "addFreeDrawHandImg");
		handImgList.add(handimg);
	}
	/*
	 * 获得当前状态的图片
	 */
	public Bitmap getCurBitmapRef() {
        Canvas canvas = new Canvas();
        canvas.setBitmap(b);
        if(status == CURSOR){
        	Log.e("save", "cursor");
        	canvas.drawBitmap(view.getMBitmap(), new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT),
        			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
        } else{
        	Log.e("save", "cursor else");
        	canvas.drawBitmap(view.getMBitmap(), new Rect(0,0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
        			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
        }
        return b;
	}
	/*
	 * 指定要获得该状态的图片
	 */
	public Bitmap getCurBitmapRef(int status) {
        Canvas canvas = new Canvas();
        Log.e("storage", "null------------" + (b==null));
        canvas.setBitmap(b);
        if(status == CURSOR){
        	Log.e("save", "cursor");
        	canvas.drawBitmap(view.getMBitmap(), new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT),
        			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
        } else{
        	Log.e("save", "cursor else");
        	canvas.drawBitmap(view.getMBitmap(), new Rect(0,0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
        			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
        }
        return b;
	}
	/*
	 * 获取目录中缩略图所需要的图片
	 */
	public Bitmap getCurIndexBitmapRef(int status) {
        Canvas canvas = new Canvas();
        canvas.setBitmap(indexB);
        if(WolfTemplateUtil.getCurrentTemplate().getTdirect() == 1){
        	Log.e("save", "cursor");
        	canvas.drawBitmap(view.getMBitmap(), 
//        			new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT),
//        			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
        			
        			new Rect(1000,0,Start.SCREEN_WIDTH * 2,500),
					new Rect(0, 0, 200, 450),
					
        			new Paint());
        } else{
        	Log.e("save", "cursor else");
        	canvas.drawBitmap(view.getMBitmap(), 
//        			new Rect(0,0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
//        			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), 
        			
        			new Rect(Start.SCREEN_WIDTH,0,Start.SCREEN_WIDTH * 2,160),
        			//ly
					//new Rect(0, 0, 600, 160),
        			new Rect(0, 0, 1600, 160),
        			new Paint());
        }
        return indexB;
	}
	
	
	public void makeAndAddCursorDrawHandImg(List<HandImg> handImgList, 
			LinkedList<EditableCalligraphyItem> editCharList) {
		
		Log.i(TAG, "makeCursorHandImg");
		
		int size = editCharList.size();
		EditableCalligraphyItem charItem;
		for(int i=0;i<size;i++){

			Log.i(TAG, "character(handimg) num: " + i);
			charItem = editCharList.get(i);
			
			HandImg handimg = new HandImg();
			handimg.setForm(1);
			handimg.setLayout(1);
			handimg.setHid(++handimgID);
			handimg.setImgtime(charItem.getTime());
			handimg.setLevel(0);
			
			//以下四项，只有光标态时候会用到
			handimg.setArea(new Rect(Start.SCREEN_WIDTH, 899-(int)(view.cursorBitmap.bCurInfo.mPosLeft+300), 70, Start.SCREEN_HEIGHT - 1));
			handimg.setChartype(charItem.getCharType());
			handimg.setSequence(i);
			handimg.setImgvector(0);
			
			Img img = new Img();
			img.setType(1);
			img.setSize(1);
			img.setX(0);
			img.setY(0);
			Bitmap bm = charItem.getCharBitmap();
			if(bm != null) {
				img.setWidth(bm.getWidth());
				img.setHeight(bm.getHeight());
			} else {
				img.setWidth(0);
				img.setHeight(0);
			}
			img.setCompact(1);
			img.setBitcount(4);
			img.setLength(0);  //暂时为0，具体写xml时赋值
			img.setKind(1);
			img.setFormat(2);  //JPEG
			img.setPagecontrol(0);
			img.setImgpart(new Rect(0,0,0,0));
			img.setBitmap(bm);
			//charItem.getCharBitmap().compress(format, quality, stream)
			handimg.setImg(img);
			
			//only for debug
			/*
			DataOutputStream fos;
			File file = new File("/sdcard/tmp222");
			
			try {
				if(file.exists())
					file.createNewFile();
				fos = new DataOutputStream(new FileOutputStream(file));
				charItem.getCharBitmap().compress(CompressFormat.JPEG, 100, fos);
				creater.startTag("imglen");
//				creater.attribute("length", String.valueOf(file.length()));
				fos.flush();
				fos.close();
			} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			*/
			////
			Log.i(TAG, "add handimg:" + i);
			handImgList.add(handimg);
		}
		
	}
	
	public void writeToXML(HandWrite handwrite){
		
		Log.i(TAG, "start write HandWrite To XML");
		
//		creater.startDocument();
		creater.startTag("handwrite");
		creater.withoutAttribute();
		
		creater.startTag("format");
		creater.text(String.valueOf(handwrite.getFormat()));
		creater.endTag("format");
		
		//handitems
		HandItem item;
		for(int i=0;i<handwrite.getHandItemList().size();i++) {
			item = handwrite.getHandItemList().get(i);
			writeHandItemToXML(item);
		}
		
		Log.i(TAG, "end Write handwrite to XML");
		creater.endTag("handwrite");
		creater.endDocument();
	}
	
	public void writeHandItemToXML(HandItem item) {
		Log.i(TAG, "start write HandItem to XML");
		
		creater.startTag("handitem");
		creater.withoutAttribute();
		
		creater.startTag("url");
		creater.text(item.getUrl());
		creater.endTag("url");
		
		creater.startTag("offset");
		creater.text(String.valueOf(item.getOffset()));
		creater.endTag("offset");
		
		creater.startTag("page");
		creater.text(String.valueOf(item.getPage()));
		creater.endTag("page");
		
		creater.startTag("userid");
		creater.text(String.valueOf(item.getUserid()));
		creater.endTag("userid");
		
		creater.startTag("time");
		creater.text(String.valueOf(item.getTime()));
		creater.endTag("time");
		
		HandImg handImg;
		for(int i=0; i<item.getHandImgList().size(); i++){
			handImg = item.getHandImgList().get(i);
			//如果是换行符等无bitmap的情况，暂时不写入。
			if(handImg.getImg().getBitmap() != null) {
				writeHandImgToXML(handImg);
			}
		}

		Log.i(TAG, "end write HandItem to XML");
		creater.endTag("handitem");
	}
	
	public void writeHandImgToXML(HandImg handImg){

		Log.i(TAG, "start write HandiImg " + handImg.getHid() + " to XML");
		//handimg
		creater.startTag("handimg");
		creater.withoutAttribute();
		
		creater.startTag("form");
		creater.text(String.valueOf(handImg.getForm()));
		creater.endTag("form");
		
		creater.startTag("layout");
		creater.text(String.valueOf(handImg.getLayout()));
		creater.endTag("layout");

		creater.startTag("hid");
		creater.text(String.valueOf(handImg.getHid()));
		creater.endTag("hid");

		creater.startTag("imgtime");
		creater.text(String.valueOf(handImg.getImgtime()));
		creater.endTag("imgtime");
		
		creater.startTag("level");
		creater.text(String.valueOf(handImg.getLevel()));
		creater.endTag("level");
		
		creater.startTag("area");
//		creater.withoutAttribute();
//		
//		creater.startTag("startX");
		creater.text(String.valueOf(handImg.getArea().left) + "," + String.valueOf(handImg.getArea().top) + ","
				+ String.valueOf(handImg.getArea().right) + "," + String.valueOf(handImg.getArea().bottom));
//		creater.endTag("startX");
		
//		creater.startTag("startY");
//		creater.text(String.valueOf(handImg.getArea().top));
//		creater.endTag("startY");

//		creater.startTag("endX");
//		creater.text(String.valueOf(handImg.getArea().right));
//		creater.endTag("endX");

//		creater.startTag("endY");
//		creater.text(String.valueOf(handImg.getArea().bottom));
//		creater.endTag("endY");
		
		creater.endTag("area");
		
		creater.startTag("chartype");
		creater.text(String.valueOf(handImg.getChartype()));
		creater.endTag("chartype");

		creater.startTag("sequence");
		creater.text(String.valueOf(handImg.getSequence()));
		creater.endTag("sequence");
		
		creater.startTag("imgvector");
		creater.text(String.valueOf(handImg.getImgvector()));
		creater.endTag("imgvector");
		
		writeImgToXML(handImg.getImg());
		
		Log.i(TAG, "start write HandiImg " + handImg.getHid() + " to XML");
		creater.endTag("handimg");
	}
	
	public void writeImgToXML(Img img){
		
		Log.i(TAG, "start write img to XML");
		//img
		
//
//		creater.attribute("type", String.valueOf(img.getType()));
//
//		creater.attribute("size", String.valueOf(img.getSize()));
//
//		creater.attribute("id", String.valueOf(img.getId()));
//
//		creater.attribute("x", String.valueOf(img.getX()));
//
//		creater.attribute("y", String.valueOf(img.getY()));
//
//		creater.attribute("width", String.valueOf(img.getWidth()));
//
//		creater.attribute("height", String.valueOf(img.getHeight()));
//
//		creater.attribute("compact", String.valueOf(img.getCompact()));
//
//		creater.attribute("bitcount", String.valueOf(img.getBitcount()));

		Bitmap bitmap = img.getBitmap();
		DataOutputStream fos;
		File file = new File(FILE_PATH_HEAD + "tmp111_apk");
		
		try {
			if(file.exists())
				file.createNewFile();
			fos = new DataOutputStream(new FileOutputStream(file));
			// by gongxl . Temp changed to PNG
			//bitmap.compress(CompressFormat.JPEG, 80, fos);
			bitmap.compress(CompressFormat.PNG, 80, fos);
			creater.startTag("imglen");
			creater.text(String.valueOf(file.length()));
			creater.endTag("imglen");
//			creater.attribute("length", String.valueOf(file.length()));
			fos.flush();
			fos.close();
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}

//		creater.attribute("kind", String.valueOf(img.getKind()));
//
//		creater.attribute("format", String.valueOf(img.getFormat()));
//
//		creater.attribute("pagecontrol", String.valueOf(img.getPagecontrol()));
		creater.startTag("img");
//		creater.text(bitmap);
		creater.text(file);
		creater.endTag("img");

		Log.i(TAG, "end write img to XML");
//		creater.endTag("img");
	}
	
	public static String getFilePath(){
		return "/data/data/tmp";
	}
	
	public static int getPageNum(){
		return 1;
	}
}
