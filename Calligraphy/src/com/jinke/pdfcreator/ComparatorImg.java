package com.jinke.pdfcreator;
import java.util.Comparator;

//list 排序, 重写compare 方法
public class ComparatorImg implements Comparator{
	public int compare(Object arg0,Object arg1){
		Item item1=(Item)arg0;
		Item item2=(Item)arg1;
		int index1=item1.getIndex();
		int index2=item2.getIndex();
		int flag=index1-index2;
		return flag;
	}


}
