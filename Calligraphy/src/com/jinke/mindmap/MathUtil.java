package com.jinke.mindmap;

public class MathUtil {

	public static enum DIRECTION {RIGHT,UP,DOWN,LEFT};
	private static double result;
	/**
	 * 判断移动到的位置与起始位置的相对方位
	 * @param x1 起始点
	 * @param y1 起始点
	 * @param x2 移动点
	 * @param y2 移动点
	 * @return
	 */
	public static DIRECTION getAngle(double x1,double y1,double x2,double y2){
		
		
		if(y2 < y1){
			//在起始点上方
			if(x1 == x2){
				return DIRECTION.UP;
			}
			result = - Math.atan((y2-y1)/(x2-x1)) *180/Math.PI;
			if(result < 0){
				result += 180;
			}
			if(result <=45){
				return DIRECTION.RIGHT;
			}
			if(result > 45 && result < 135){
				return DIRECTION.UP;
			}
			if(result >= 135){
				return DIRECTION.LEFT;
			}
		}else if(y2 == y1){
			if(x2 > x1){
				return DIRECTION.RIGHT;
			}else{
				return DIRECTION.LEFT;
			}
		}else{
			//在起始点下方
			if(x1 == x2){
				return DIRECTION.DOWN;
			}
			result = Math.atan((y2-y1)/(x2-x1)) *180/Math.PI;
			if(result < 0){
				result += 180;
			}
			if(result <= 45){
				return DIRECTION.RIGHT;
			}
			if(result > 45 && result <135){
				return DIRECTION.DOWN;
			}
			if(result >= 135){
				return DIRECTION.LEFT;
			}
			
		}
		
		return DIRECTION.UP;
	}
}
