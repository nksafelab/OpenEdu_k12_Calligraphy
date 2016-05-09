package com.jinke.rloginservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UserInfo {
	
	boolean result[] = {false};
	String username = null;
	String password = null;
	String phone = null;
	String city = null;
	String email = null;
	
	public static final Parcelable.Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
		public UserInfo createFromParcel(Parcel source) {
			
			return new UserInfo(source);
		}
		public UserInfo[] newArray(int size) {
			return new UserInfo[size];
		}
	};
	
	public UserInfo(){
		
	}
	
	public UserInfo(Parcel parcel){
		parcel.readBooleanArray(result);
		username = parcel.readString();
		password = parcel.readString();
		phone = parcel.readString();
		city = parcel.readString();
		email = parcel.readString();
	}
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeBooleanArray(result);
		parcel.writeString(username);
		parcel.writeString(password);
		parcel.writeString(phone);
		parcel.writeString(city);
		parcel.writeString(email);
		
	}
	
	public void setResult(boolean r[])
	{
		result = r;
	}
	
	public boolean[] getResult()
	{
		return result;
	}
	
	public void setUsername(String u)
	{
		username = u;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setPhone(String p)
	{
		phone = p;
	}
	
	public String getPhone()
	{
		return phone;
	}
	
	public void setCity(String c)
	{
		city = c;
	}
	
	public String getCity()
	{
		return city;
	}
	
	public void setEmail(String e)
	{
		email = e;
	}
	
	public String getEmail()
	{
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
}
