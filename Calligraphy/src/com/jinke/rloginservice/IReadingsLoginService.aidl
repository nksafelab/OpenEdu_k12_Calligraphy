package com.jinke.rloginservice;
import com.jinke.rloginservice.UserInfo;
interface IReadingsLoginService{
	boolean isLogin();
	boolean isBinding();
	boolean login(String username,String password);
	boolean unBinding();
	String getSimID();
	UserInfo getUserInfo();
	boolean loginActivity();
}