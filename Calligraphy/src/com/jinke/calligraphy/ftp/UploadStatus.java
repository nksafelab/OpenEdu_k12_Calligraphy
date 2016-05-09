package com.jinke.calligraphy.ftp;

public enum UploadStatus {
	Create_Directory_Fail, 
	Create_Directory_Success, 
	Upload_New_File_Success, 
	Upload_New_File_Failed, 
	File_Exits, 
	Remote_Bigger_Local, 
	Upload_From_Break_Success,
	Upload_From_Break_Failed, 
	Delete_Remote_Faild; 
}
