package com.jinke.calligraphy.font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.util.Log;

public class MFontDomXMLParser {

	private String xmlPath = "";
	private List<MFont> fontList = null;
	private MFont font = null;
	public MFontDomXMLParser(String xmlPath){
		this.xmlPath = xmlPath;
	}
	
	public List<MFont> parseXML(){
		fontList = new ArrayList<MFont>();
		InputStream in = null;
        try {
			in = new FileInputStream(new File(xmlPath));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			Element root = document.getDocumentElement();
			parse(root);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for(MFont font:fontList){
        	Log.i("通知：", "font name:" + font.getFontName() + " path:" + font.getFontPath());  
        }
		return fontList;
	}
	private void parse(Element element){
    	NodeList nodelist = element.getChildNodes();  
        int size = nodelist.getLength();  
        for (int i = 0; i < size; i++) {  
            // 获取特定位置的node  
            Node element2 = (Node) nodelist.item(i);  
            /* getNodeName获取tagName，例如<book>thinking in android</book>这个Element的getNodeName返回book 
             * getNodeType返回当前节点的确切类型，如Element、Attr、Text等 
             * getNodeValue 返回节点内容，如果当前为Text节点，则返回文本内容；否则会返回null 
             * getTextContent 返回当前节点以及其子代节点的文本字符串，这些字符串会拼成一个字符串给用户返回。例如 
             * 对<book><name>thinking in android</name><price>12.23</price></book>调用此方法，则会返回“thinking in android12.23” 
             */  
            String tagName = element2.getNodeName();  
            if (tagName.equals("fontnames")  
                    && element2.getNodeType() == Document.ELEMENT_NODE) {  
            	font = new MFont();  
            	Log.i("通知：", "创建beauty");  
                if (element2.getNodeType() == Document.ELEMENT_NODE) {  
                    parse((Element) element2);  
                }  
                fontList.add(font);  
                Log.i("通知：", "添加beauty");  
            }  
  
            if (tagName.equals("name")) {  
                String name = element2.getTextContent();  
                Log.i("通知：", "name" + name);  
                font.setFontName(name);
            }  
            if (tagName.equals("path")) {  
                String path = element2.getTextContent();  
                Log.i("通知：", "path" + path);  
                font.setFontPath(path);
            }  
        }  
    }
	
}
