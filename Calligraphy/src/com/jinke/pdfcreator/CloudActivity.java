package com.jinke.pdfcreator;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import com.itextpdf.text.DocumentException;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class CloudActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String url="10.0.0.123";
	private static final String port="21";
	private static final String username="anonymous";
	private static final String password="";
	private static final String remotePath="/pdf";
	private static final String fileNamePath="/extsd/pdfdir/";
	private static final String fileName="cloudnote.pdf";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
		
        List<EditableCalligraphy> editList = Start.c.view.cursorBitmap.listEditableCalligraphy;
        
        EditableCalligraphy editable = null;
        LinkedList<EditableCalligraphyItem> charList = null;
        EditableCalligraphyItem item = null;
        for(int i=0;i<editList.size();i++){
        	editable = editList.get(i);
        	charList = editable.getCharsList();
        	Log.e("bound", "charList.size:" + charList.size());
        	for(int j=0;j<charList.size();j++){
        		
        		item = charList.get(j);
        		Log.v("pdf", "availableid:" + editable.getAvailable().getAid() 
        				+ " itemid:" + item.getItemID() + " bitmap isRecycle:" + item.getCharBitmap().isRecycled());
        	}
        }
//         
        CloudNote note = new CloudNote();
////        note.initPictureList("/extsd/pictures/");
//        
        try {
        	

            String pdfPath = Environment.getExternalStorageDirectory()
    		.getAbsolutePath()+ "/cloudnote.pdf";
        	
//			note.createPDF("/extsd/cloudnote.pdf",editList);
            note.createPDF(pdfPath,editList);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
   
}