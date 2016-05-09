package com.jinke.calligraphy.app.branch;

public class WordLimit {
	
	private static final String TAG = "WordLimit";
	public static final int WORDLIMIT = 10000000;
	private static WordLimit wordLimit = null;
	private int wordCount = 0;
	
	public static WordLimit getInstance(){
		if(wordLimit == null)
			wordLimit = new WordLimit();
		return wordLimit;
	}
	
	public void addWordCount(){
		wordCount ++;
	}
	
	public boolean canInsertWord(){
		if(wordCount < WORDLIMIT){
			wordCount ++;
			return true;
		}
		return false;
	}
	
	public void resetWordCount(){
		wordCount = 0;
	}
	

}
