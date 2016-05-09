package com.jinke.calligraphy.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;

import android.graphics.Bitmap;
import android.util.Log;



public class CalliParse extends DefaultHandler{

	private String szTagName = "";
	private String flagStr = "";
//	public static final String FILE_PATH_HEADER = "/extsd";
	public static final String FILE_PATH_HEADER = Start.getStoragePath();
	private List<CalligraphyItem> cList;
	private CalligraphyItem cItem;
	CDBPersistent db;
	public CalliParse(){
		cList = new ArrayList<CalligraphyItem>();
		db = new CDBPersistent(Start.context);
		db.open();
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (localName.equals("Item")) {
			cItem = new CalligraphyItem();
		} else {
			szTagName = localName;
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		//System.out.println("EndElement");
		flagStr ="";
		szTagName = "";
		
		szTagName = "";
		if (localName.equals("Item")) {
			if(cList.size() > 100){
				db.insert(cList);
				db.pageBackup(cList);
				cList.clear();
			}
			
			cList.add(cItem);
			
			cItem = new CalligraphyItem();
		}else if(localName.equals("CalligraphyStore")){
			db.close();
		}
	}

	public void characters(char[] ch, int start, int length) {
		//System.out.println("characters");
		
		if (szTagName.equals("templateID")) {
			flagStr += new String(ch, start, length);
			cItem.setTemplateID(StrToInt(flagStr));
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("pageNum")){
			flagStr += new String(ch, start, length);
			cItem.setPageNum(StrToInt(flagStr));
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("availableID")){
			flagStr += new String(ch, start, length);
			cItem.setAvailableID(StrToInt(flagStr));
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("itemID")){
			flagStr += new String(ch, start, length);
			cItem.setItemID(StrToInt(flagStr));
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("flipBottom")){
			flagStr += new String(ch, start, length);
			cItem.setFlipBottom(StrToInt(flagStr));
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("flipDst")){
			flagStr += new String(ch, start, length);
			cItem.setFlipDst(StrToInt(flagStr));
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("charType")){
			flagStr += new String(ch, start, length);
			cItem.setCharType(flagStr);
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("matrix")){
			flagStr += new String(ch, start, length);
			cItem.setMatrix(flagStr);
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("created")){
			flagStr += new String(ch, start, length);
			cItem.setCreated(flagStr);
			Log.e("start", "templateID"+flagStr);
		}else if(szTagName.equals("byteBitmap")){
			flagStr += new String(ch, start, length);
			cItem.setByteBitmap(flagStr);
//			Bitmap b = BitmapFactory.decodeByteArray(Base64.decode(flagStr.getBytes(), Base64.DEFAULT),0,Base64.decode(flagStr.getBytes(), Base64.DEFAULT).length);
//			saveFile(b, "parser.png");
			Log.e("bitmap", "bitmap--------------------------");
			Log.e("bitmap", "bitmap:"+flagStr);
		}else if(szTagName.equals("picturePath")){
			flagStr += new String(ch, start, length);
			cItem.setUri(flagStr);
			Log.e("start", "uri"+flagStr);
		}
		
	}

	public List<CalligraphyItem> doParse(InputStream oIS) throws Exception {
		try
		{
			FileOutputStream out = new FileOutputStream(new File(FILE_PATH_HEADER + "/temp"));
			byte[] b = new byte[4096];
			int c = 0;
			while( (c = oIS.read(b)) != -1){
				out.write(b, 0, c);
			}
			
			
			
			Log.e("start", "doparse!!!!!!!!!!!!!!!!!!!!");
			//System.out.println("Parse!!!!!!!!!");
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser parser = saxFactory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(this);
		
		
		//added by zhong
//		BufferedInputStream bis = new BufferedInputStream(oIS);
//		
//		ByteArrayBuffer bab = new ByteArrayBuffer(512);
//		int count = 0;
//		int current = 0;
//		while ((current = bis.read()) != -1) {
//			bab.append((byte) current);
//			count ++;
//		}
//		Log.e("request", "count:"+count);
//		Log.e("request", "bab.size:"+bab.length());
//		String szData = new String(bab.toByteArray(), "utf-8");
//		bis.close();
//		
//		Log.e("request", "result:"+szData);
		
		
//		reader.parse(new InputSource(new StringReader(szData.trim())));
//		reader.parse(new InputSource(new StringReader(zz)));
		File temp = new File(FILE_PATH_HEADER + "/temp");
//		reader.parse(new InputSource(new FileInputStream(new File(FILE_PATH_HEADER + "/temp"))));
			reader.parse(new InputSource(new FileInputStream(temp)));
		
		if(temp.exists())
			temp.delete();
		//org.apache.harmony.xml.ExpatParser$ParseException: 
//		At line 2, column 0: XML or text declaration not at start of entity

		
		}
		catch (IOException e) {
			Log.e("start", "doParseException!!!!!!!!!!!!!!!!!!!!");
			System.out.println("doParseException:" + e.getMessage());
		}
		if(oIS != null)
			oIS.close();
		return cList;
	}
	
	public int StrToInt(String str){
		return Integer.parseInt(str);
	}
	
	public void saveFile(Bitmap b, String filename) {
		try {

			Log.d("start", "saveBitmapFile >>>>>>>>>>>>>>>>>>>>>>>");
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(new File("/sdcard/" + filename)));
			// mBaseImpl.syncFromPartToMain();
			// view.pickBitmap();
			b.compress(Bitmap.CompressFormat.PNG, 80, bos);
			bos.flush();

			bos.close();

		} catch (Exception e) {

		}
	}
	
	public static String Get(String szUrl) throws Exception {
        URL oUrl = new URL(szUrl);

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(szUrl);
        String result = "";
        String s = "";
        try {

        	HttpResponse response = client.execute(get);
        	BufferedReader reader = new BufferedReader(new InputStreamReader(
            response.getEntity().getContent()));
        	for (s = reader.readLine(); s != null; s = reader.readLine()) {
        		Log.v("response", s);
        		result += s;
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return result;
	}

	
}
