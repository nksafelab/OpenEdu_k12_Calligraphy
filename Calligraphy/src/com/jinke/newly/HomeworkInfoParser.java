package com.jinke.newly;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


public class HomeworkInfoParser extends DefaultHandler {
	
	private List<HomeworkBean> result;
	private HomeworkBean current;
	private String tagName;
	
	public List<HomeworkBean> getResult()
	{
		return result;
	}
	
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		result=new ArrayList<HomeworkBean>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		if(localName.equals("work"))
		{
			current=new HomeworkBean();
		}
		this.tagName=localName;
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		if(localName.equals("work"))
		{
			result.add(current);
			current = null;
		}
		this.tagName = null;
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		if(tagName!=null)
		{
			String data=new String(ch,start,length);
			if(tagName.equals("title"))
			{
				current.setTitle(data);
			}
			else if(tagName.equals("workpic"))
			{
				current.setPic(data);
			}
			else if(tagName.equals("stuname"))
			{
				current.setStuname(data);
			}
			else if(tagName.equals("posttime"))
			{
				current.setUptime(data);
			}
			
		}
	}
	
	public boolean doParse(String xml) throws Exception
	{
		try
		{
			SAXParserFactory saxFactory=SAXParserFactory.newInstance();
			SAXParser parser=saxFactory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(this);
			
			reader.parse(new InputSource(new StringReader(xml.trim())));
		}
		catch(Exception e)
		{
			Log.e("!!!!!!!!!!!!", "in loginParser error!!");
		}
		
		return true;
	}
}
