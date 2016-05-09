package com.jinke.mindmap;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.single.LogUtil;

public class MindMapItem {
	private MindMapItem parent;//父节点
	private List<MindMapItem> childList;//子节点链表
	private List<EditableCalligraphyItem> charList;//包含的文字
	private int level;//当前节点所在的层数
	private int buttom = -1;
	private int pos;//当前节点在本层上的位置，即是父节点的第几个儿子节点
	private static int minditemcount = 0;//记录全局的节点个数，用以设置节点id
	private int mindid = 0;//节点id
	private int parentid = -1;//父节点id，父节点为空时mindid=-1
	private boolean isComposed;//记录当前节点是否已排版
    public LocateDot indot;//当前节点划线时的入口点坐标
	public LocateDot outdot;//当前节点划线时的出口点坐标
	public int flip_dstX = 0;
	public void setFlipDstX(int fdtX){
		this.flip_dstX = fdtX;
	}
	public int getFlipDstX(){
		if(parent == null)
			return flip_dstX;
		else
			return parent.getFlipDstX();
	}
	public void deleteWord(EditableCalligraphyItem item){
		boolean flag = this.charList.remove(item);
		LogUtil.getInstance().e("delete", "delete result:" + flag);
	}
	/**
	 * 功能：不允许没有文字的节点出现；（不能光标定位）
	 */
	public void isDestory(){
		LogUtil.getInstance().e("isDestory", "charList:" + (this.charList == null));
		if(charList != null)
			LogUtil.getInstance().e("isDestory", "charList size:" + this.charList.size());
		if(this.charList!=null && this.charList.size()!=0)
			return;
		if(this.parent == null)
			return;
		LogUtil.getInstance().e("isDestory", "destory mindmapitem " + this.getMindID());
		this.parent.childList.remove(this);
	}
	//设置当前节点已经排版
	public void setComposed(){
		this.isComposed = true;
	}
	public void setNotComposed(){
		this.isComposed = false;
	}
	public boolean isComposed(){
		return this.isComposed;
	}
	
	public LocateDot getFromDot(float vop){//设置接入点和发出点位置//vop(0-1)=0.5是居中，越大越往下
        LocateDot from = new LocateDot(outdot.x,indot.y+Math.abs(indot.y-outdot.y)*vop);
        return from;
	}
	public LocateDot getToDot(float vop){
	        LocateDot to = new LocateDot(indot.x,indot.y+Math.abs(indot.y-outdot.y)*vop);
	        return to;
	}
	public boolean hasParent(){
		return this.parent != null;
	}
	public boolean isFirst(EditableCalligraphyItem item){
		
		int index = charList.indexOf(item);
		LogUtil.getInstance().e("drawmindmap", "isFirst:" + index);
		return index == 0;
	}
	public boolean hasChild(){
		if(childList == null)
			return false;
		if(childList.size() == 0)
			return false;
		return true;
	}
	public int getMindID(){
		return mindid;
	}
	public int getParentID(){
		return parentid;
	}
	public MindMapItem(){
		parent = null;
		level = 0;
		
//		minditemcount = 0;//新建导图，计数归0
		mindid = minditemcount++;
		parentid = -1;//新建导图，没有父节点
		LogUtil.getInstance().e("mindid", "new mindmap mindid:" + mindid + " parentid:" + parentid);
		
		charList = new ArrayList<EditableCalligraphyItem>();
		indot = new LocateDot();
		outdot = new LocateDot();
	}
	public MindMapItem(MindMapItem parent){
		this.parent = parent;
		this.level = parent.getLevel() + 1;
		
		mindid = minditemcount++;
		parentid = parent.getMindID();
		LogUtil.getInstance().e("mindid", "new minditem mindid:" + mindid + " parentid:" + parentid);
		
		charList = new ArrayList<EditableCalligraphyItem>();
		indot = new LocateDot();
		outdot = new LocateDot();
	}
	public static void resetMindMapCount(){
		minditemcount = 0;
	}
	public int getLevel(){
		return this.level;
	}
	
	public void addNewWord(EditableCalligraphyItem item){
		item.setMindMapItem(this);
		item.setSpecial();
		charList.add(item);
	}
	
	public List<MindMapItem> getChildList(){
		return childList;
	}
	public void setChildID(int id){
		this.pos = id;
	}
	public int getbrotherButtom(){
		if(this.parent == null){
			return -1;
		}
		
		if(pos >= 1 && pos < parent.childList.size()){
			return this.parent.childList.get(pos-1).getButtom();
		}
		if(pos == 0){
			return this.parent.getbrotherButtom();
		}
		return -1;
	}
	public MindMapItem createNewChild(){
		MindMapItem childItem = new MindMapItem(this);
		if(this.childList == null){
			this.childList = new ArrayList<MindMapItem>();
		}
		this.childList.add(childItem);
		childItem.setChildID(childList.size() -1);
		return childItem;
	}
	public List<EditableCalligraphyItem> getCharList(){
		return this.charList;
	}
	/**
	 * 排版后，距上方的行数
	 * @return
	 */
	public int getMarginTop(){
//		if(posterityCount() % 2 == 0){
//			return posterityCount()/2;
//		}else{
//			return (posterityCount()+1)/2;
//		}
		return posterityCount()/2;
	}
	public void setButtom(int buttom){
		this.buttom = buttom;
		LogUtil.getInstance().e("mindmap", pos + " setButtom " + buttom);
	}
	public int getButtom(){
		if(childList!=null && childList.size() != 0){
			if(buttom < childList.get(childList.size() -1).getButtom())
				return childList.get(childList.size() -1).getButtom();
		}
		LogUtil.getInstance().e("mindmap", pos + " getButtom " + buttom);
		return buttom;
	}
	/**
	 * 该节点所有子孙的数量
	 * @return 存在子节点，返回所有子节点的posterityCount()之和； 没有子节点，返回1；
	 */
	public int posterityCount(){
		int count = 0;
		if(childList != null && childList.size() != 0){ 
			MindMapItem temp = null;
			for(int i=0;i<childList.size();i++){
				temp = childList.get(i);
//				count ++;
				count += temp.posterityCount();
			}
			
			return count;
		}else{
			return 1;
		}
	}
}
