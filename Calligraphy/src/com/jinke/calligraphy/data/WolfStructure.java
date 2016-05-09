package com.jinke.calligraphy.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ObjectInputStream.GetField;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class WolfStructure {
	private static final String TAG = "WolfStructure";
	private File wolfFile;
	private RandomAccessFile access;
	private FileWriter writer;

	private static final int HEAD_OFFSET = Storage.WOL_FILE_LEN;
	private File TMP_NODE_FILE ;
	private String TMP_NODE_FILE_PATH = Storage.FILE_PATH_HEAD + "nodefile_tmp_apk";

	private static int	curFileOffset = HEAD_OFFSET;
	private XmlSerializer serializer;
	
	public WolfStructure(File f){
		this.wolfFile = f;
		serializer = Xml.newSerializer();
	}
	
	public int getAddressInfo(String filepath, int page){
//		AddressInfo info = new AddressInfo();
		try {
			access = new RandomAccessFile(wolfFile, "rw");
			TMP_NODE_FILE = new File(TMP_NODE_FILE_PATH);
			writer = new FileWriter(TMP_NODE_FILE);
			serializer.setOutput(writer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "curOffset1:" + curFileOffset);
		//读取手写文件头获取第一个文件节点的位置
		HandWriteHeadInfo headInfo = readHandWriteHeadInfo();
		
		
		Log.i(TAG, "curOffset2:" + curFileOffset);
		//如果地址为空，表示还没有文件结点，则更新手写文件头，新建一个文件结点。
		Log.i(TAG, (headInfo == null) ? "null" : "not null");
		Log.i(TAG, "@@First file node address:" + Integer.toHexString(headInfo.firstFileNodeAddress));
		
		if(headInfo.firstFileNodeAddress == 0) {
			Log.i(TAG, "the first file node");
			headInfo.firstFileNodeAddress = Storage.WOL_FILE_LEN;
			headInfo.offset = Storage.WOL_FILE_LEN;
			//更新手写文件头
			updateHandWriteHead(headInfo);
		} else {
			Log.i(TAG, "more than one file node");
			//通过headInfo中的信息，更新最后一个fileNode的next指针
			updateLastFileNode(headInfo, filepath);
			//更新手写文件头
			headInfo.offset = (int) wolfFile.length();
			updateHandWriteHead(headInfo);
		}

		curFileOffset = (int) wolfFile.length();
		Log.i(TAG, "curFileOffset3:" + curFileOffset);
		makeAndWriteFileNode(filepath);
		makeAndWritePageNode(page);
		makeAndWriteHandNode();

		Log.i(TAG, "write before: length:" + wolfFile.length());
		
		int len;
		byte[] buffer = new byte[512];
		
		try {
			serializer.flush();
			access.seek(wolfFile.length());
			FileInputStream fis = new FileInputStream(TMP_NODE_FILE);
			while((len = fis.read(buffer)) != -1) {
				access.write(buffer, 0, len);
			}
			fis.close();
			writer.close();
			access.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "write after: length:" + wolfFile.length());
		
		return (int) wolfFile.length();
		//从第一个文件结点开始遍历，找到对应的文件结点
		//取得文件结点的最后一个页结点，添加新的页结点
		//生成手写结点，并添加进入文件，记录手写信息的位置为当前文件尾
		//根据上述的信息，将生成的XML写入此位置
	}
	
	public void updateLastFileNode(HandWriteHeadInfo headInfo, String filePath){
		FileNode fn = new FileNode(filePath, "0", "0","0");
		
		try {
			Log.i(TAG, "====write next===" + wolfFile.length() + " at " + 
					Integer.toHexString(headInfo.offset + fn.getNextPos()));
			access.seek(headInfo.offset + fn.getNextPos());
			access.write(String.format("0x%08x", wolfFile.length()).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void makeAndWriteFileNode(String filePath){
		//新建文件结点
		FileNode fn = new FileNode(filePath, "0", "0", "0");
		curFileOffset += fn.getLength();
		Log.i(TAG, "curOffset4:" + curFileOffset);
		fn.setFirstPageNodeAddress(String.format("0x%08x", curFileOffset));
		fn.setNextFileNodeAddress(String.format("0x%08x", 0));
		fn.setLastPageNodeAddress(fn.getFirstPageNodeAddress());
		//将文件结点写入文件尾
		writeFileNodeToWol(fn);
	}
	
	public void writeFileNodeToWol(FileNode fn){
		try {
			serializer.startTag(null, fn.FILENODE);

			serializer.startTag(null, fn.ADDRESS);
			serializer.text(fn.getFilePath());
			serializer.endTag(null, fn.ADDRESS);
			
			serializer.startTag(null, fn.SUBNODE);
			serializer.text(fn.getFirstPageNodeAddress());
			serializer.endTag(null, fn.SUBNODE);
			

			serializer.startTag(null, fn.NEXT);
			serializer.text(fn.getNextFileNodeAddress());
			serializer.endTag(null, fn.NEXT);
			
			serializer.startTag(null, fn.LAST);
			serializer.text(fn.getLastPageNodeAddress());
			serializer.endTag(null, fn.LAST);
			
			serializer.endTag(null, fn.FILENODE);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void makeAndWritePageNode(int page){
		//新建文件结点
		PageNode pn = new PageNode();
		pn.setPageNum(String.format("0x%08x", page));
		curFileOffset += pn.getLength();
		Log.i(TAG, "curOffset5:" + curFileOffset);
		pn.setFirstHandNodeAddress(String.format("0x%08x", curFileOffset));
		pn.setNextPageNodeAddress(String.format("0x%08x", 0));
		pn.setAvailable("1");
		//将文件结点写入文件尾
		writePageNodeToWol(pn);
	}
	
	public void writePageNodeToWol(PageNode pn){
		try {
			serializer.startTag(null, pn.PAGENODE);
			
			serializer.startTag(null, pn.ADDRESS);
			serializer.text(pn.getPageNum());
			serializer.endTag(null, pn.ADDRESS);
			
			serializer.startTag(null, pn.SUBNODE);
			serializer.text(pn.getFirstHandNodeAddress());
			serializer.endTag(null, pn.SUBNODE);
			
			serializer.startTag(null, pn.NEXT);
			serializer.text(pn.getNextPageNodeAddress());
			serializer.endTag(null, pn.NEXT);
			
			serializer.startTag(null, pn.AVAILABLE);
			serializer.text(pn.getAvailable());
			serializer.endTag(null, pn.AVAILABLE);
			
			serializer.endTag(null, pn.PAGENODE);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void makeAndWriteHandNode(){
		//新建手写结点
		HandNode hn = new HandNode();
		hn.setIndex(String.format("0x%08x", 0));
		curFileOffset += hn.getLength();
		Log.i(TAG, "curOffset6:" + curFileOffset);
		hn.setHanditemAddress(String.format("0x%08x", curFileOffset));
		hn.setNextHandNodeAddress(String.format("0x%08x", 0));
		hn.setAvailable("1");
		//将文件结点写入文件尾
		writeHandNodeToWol(hn);
	}
	
	public void writeHandNodeToWol(HandNode hn){
		try {
			serializer.startTag(null, hn.HANDNODE);
			
			serializer.startTag(null, hn.INDEX);
			serializer.text(hn.getIndex());
			serializer.endTag(null, hn.INDEX);
			
			serializer.startTag(null, hn.ADDRESS);
			serializer.text(hn.getHanditemAddress());
			serializer.endTag(null, hn.ADDRESS);
			
			serializer.startTag(null, hn.NEXT);
			serializer.text(hn.getNextHandNodeAddress());
			serializer.endTag(null, hn.NEXT);
			
			serializer.startTag(null, hn.AVAILABLE);
			serializer.text(hn.getAvailable());
			serializer.endTag(null, hn.AVAILABLE);
			
			serializer.endTag(null, hn.HANDNODE);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateHandWriteHead(HandWriteHeadInfo info) {
		Log.i(TAG, "update hand write head to --- 0x" + Integer.toHexString(info.offset));
		try {
			access.seek(Storage.HW_HEAD_INFO_ADDRESS);
			access.writeInt(info.firstFileNodeAddress);
			access.writeInt(info.offset);
			access.writeInt(info.count);
			curFileOffset = HEAD_OFFSET + HandWriteHeadInfo.HANDWRITE_HEADINFO_LENGTH;
			access.seek(curFileOffset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 第一次执行时，还没有手写文件头，返回全是0
	 */
	public HandWriteHeadInfo readHandWriteHeadInfo(){
		Log.i(TAG, "read hw Head Info @@@@@ begin");
		HandWriteHeadInfo info = new HandWriteHeadInfo();
		try{
			access.seek(Storage.HW_HEAD_INFO_ADDRESS);
			Log.i(TAG, "curFilePos:" + access.getFilePointer());
			Log.i(TAG, "wolfFile length:" + wolfFile.length());
			info.firstFileNodeAddress = access.readInt();
			info.offset = access.readInt();
			info.count = access.readInt();

			Log.i(TAG, "read hw Head Info @@@@@ end");
			return info;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
//	public void ok(){
//		
//		String path;
//		FileNode fn;
//		try {
//			access = new RandomAccessFile(wolfFile, "rw");
//			access.seek(curFileOffset);
//			fn = readFileNode();
//			path = new String(fn.filePath, 0, filepath.length());
//			while(! path.equals(filepath)) {
//				curFileOffset += FileNode.getBytesNum();
//				access.seek(curFileOffset);
//				fn = readFileNode();
//				path = new String(fn.filePath, 0, filepath.length());
//			}
//			return info;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}
	
//	public FileNode readFileNode(){
//		FileNode fileNode = new FileNode();
//		byte[] buffer = new byte[1024];
//		try {
//			if( access.read(buffer)!= -1)
//				fileNode.filePath = buffer;
//			fileNode.pageNodeAddress = access.readLong();
//			fileNode.nextFileNodeAddress = access.readLong();
//			return fileNode;
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}
	
//	public PageNode readPageNode(){
//		PageNode pageNode = new PageNode();
//		try {
//			pageNode.pageNum = access.readInt();
//			pageNode.handNodeAddress = access.readLong();
//			pageNode.nextPageNodeAddress = access.readLong();
//			
//			return pageNode;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}
//	
//	public HandNode readHandNode(){
//		HandNode handNode = new HandNode();
//		try{
//			handNode.curpos = access.readInt();
//			handNode.handitemAddress = access.readLong();
//			access.skipBytes(16);
//			handNode.nextHandNodeAddress = access.readLong();
//			return handNode;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}
	

	
	public void writeFileNode(FileNode fn, long address){
		
	}
	
	public void writePageNode(PageNode pn, long address){
		
	}
	
	public void writeHandNode(HandNode hn, long address){
		
	}
	
	public void writeHandItem(HandItem hi, long address){
		
	}
	
	class AddressInfo {
		public long fileNodeAddress;
		public long pageNodeAddress;
		public long handNodeAddress;
		public long handItemAddress;
	}
	
	
}
