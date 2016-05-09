package hallelujah.cal;

public class Point {

	private long lTimeStamp = 0;
	private float fXCoordinate = 0.0f;
	private float fYCoordinate = 0.0f;
	private float fPressure = 0.0f;
	private float fSize = 0.0f;
	private float fTouchMajor = 0.0f;
	private float fTouchMinor = 0.0f;
	private float fToolMajor = 0.0f;
	private float fToolMinor = 0.0f;
	private float fOrientation = 0.0f;
	private byte btToolType = 0x00;
	
	public Point(){
	}
	public long getTimeStamp() {
		return lTimeStamp;
	}
	public void setTimeStamp(long lTimeStamp) {
		this.lTimeStamp = lTimeStamp;
	}
	public float getXCoordinate() {
		
		return fXCoordinate;
	}
	public void setXCoordinate(float fXCoordinate) {
		this.fXCoordinate = fXCoordinate;
	}
	public float getYCoordinate() {
		return fYCoordinate;
	}
	public void setYCoordinate(float fYCoordinate) {
		this.fYCoordinate = fYCoordinate;
	}
	public float getPressure() {
		return fPressure;
	}
	public void setPressure(float fPressure) {
		this.fPressure = fPressure;
	}
	public float getSize() {
		return fSize;
	}
	public void setSize(float fSize) {
		this.fSize = fSize;
	}
	public float getTouchMajor() {
		return fTouchMajor;
	}
	public void setTouchMajor(float fTouchMajor) {
		this.fTouchMajor = fTouchMajor;
	}
	
	public float getTouchMinor() {
		return fTouchMinor;
	}
	public void setTouchMinor(float fTouchMinor) {
		this.fTouchMinor = fTouchMinor;
	}
	public float getToolMajor() {
		return fToolMajor;
	}
	public void setToolMajor(float fToolMajro) {
		this.fToolMajor = fToolMajro;
	}
	public float getToolMinor() {
		return fToolMinor;
	}
	public void setToolMinor(float fToolMinor) {
		this.fToolMinor = fToolMinor;
	}
	public float getOrientation() {
		return fOrientation;
	}
	public void setOrientation(float fOrientation) {
		this.fOrientation = fOrientation;
	}
	public byte getToolType() {
		return btToolType;
	}
	public void setToolType(byte btToolType) {
		this.btToolType = btToolType;
	}
	

}
