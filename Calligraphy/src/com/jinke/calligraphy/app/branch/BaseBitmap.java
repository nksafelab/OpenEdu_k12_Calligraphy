package com.jinke.calligraphy.app.branch;

import java.util.ArrayList;
import java.util.List;

import com.jinke.calligraphy.app.branch.FreeDrawBitmap.FreeBitmapInfo;
import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.template.WolfTemplate;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.single.BitmapCount;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class BaseBitmap {
	
	private static final String TAG = "BaseBitmap";
	public static final int TITLE_HEIGHT = 103;
	public	static Bitmap 	mBitmap;  	//保持对全局mBitmap的引用
	protected Bitmap 	bitmap;
	public	Paint	paint;		//每种画布风格(涂鸦/光标)都可以用不同的paint，此paint在具体的笔(硬笔/毛笔)构造时赋值。
    public 	MyView	bView;
    public 	CurInfo bCurInfo;	//属于画布，涂鸦时pos=0，光标时pos=-300
    private Paint line_paint = new Paint();
    private List<Available> listAvailables;
    private WolfTemplate template;
    
    private int line_bottom = 0;
	private int line_top = 0;
	public static int line_space = 0;
	private Available available;

	public static Bitmap addBitmap;
	public static List<Bitmap> addBitmapList;
	private float[] matrixValue = new float[9];
	
	public static boolean TransparentChanged = false;
	
	public static Bitmap cleanBitmap1;
	public static Bitmap cleanBitmap2;
	

    public float	mStartX,mStartY,mEndX,mEndY;
    public float	sStartX,sStartY,sEndX,sEndY;
    
	//mouse
    public Bitmap transparentBitmap;
	
	static {
			//cleanBitmap1 = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
			cleanBitmap1 = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);

			BitmapCount.getInstance().createBitmap("BaseBitmap create cleanBitmap1");
			//cleanBitmap2 = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
			cleanBitmap2 = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap("BaseBitmap create cleanBitmap2");
			cleanBitmap1.eraseColor(Color.WHITE);
			cleanBitmap2.eraseColor(Color.WHITE);
	}
	
	public void resetAddBitmapList(){
		for(int i=0;i<addBitmapList.size();i++){
			addBitmapList.get(i).eraseColor(Color.WHITE);
		}
	}
	
	public BaseBitmap(Bitmap bm, MyView v){
		mBitmap = bm;
		/*
		 * 移入子类中，因为每个子类中的画布大小可能不一样
		 */ 

		if(transparentBitmap == null) {
			//transparentBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
			transparentBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap("BaseBitmap create transparentBitmap");
		}
		transparentBitmap.eraseColor(Color.TRANSPARENT);

		resetBound();
		
		//ly
		//注释掉看效果
		if(WolfTemplateUtil.getCurrentTemplate().getFormat() != 0)
			
//			try{
//				addBitmap = BitmapFactory.decodeFile(WolfTemplateUtil.TEMPLATE_PATH+WolfTemplateUtil.getCurrentTemplate().getName()+"/"+WolfTemplateUtil.getCurrentTemplate().getName()+"_add_bg.png").copy(Config.ARGB_8888, true);
//				//String str=WolfTemplateUtil.TEMPLATE_PATH+WolfTemplateUtil.getCurrentTemplate().getName()+"/"+WolfTemplateUtil.getCurrentTemplate().getName()+"_add_bg.png";
//				BitmapCount.getInstance().createBitmap("BaseBitmap decode addBitmap");
//				
//			}catch (OutOfMemoryError e) {
//				// TODO: handle exception
//				Log.e("AndroidRuntime", "BaseBitmap BaseBitmap() OOM!!!");
//			}
		//end
		
		addBitmapList = new ArrayList<Bitmap>();
		addBitmapList.add(addBitmap);
		
		bView = v;
		template = WolfTemplateUtil.getCurrentTemplate();
		listAvailables = template.getAvailables();
	}
	public void updateAvailables(){
		template = WolfTemplateUtil.getCurrentTemplate();
		listAvailables = template.getAvailables();
		for(int i=0;i<listAvailables.size();i++){
			Log.e("template", "new start:"+listAvailables.get(i).getStartY());
		}
	}
	public static Bitmap getAddedBitmap(int i){
		
		if(i == 1 && (addBitmapList.size() < 2)){
				addPage();//可能添加不上，因为OOM
		}
		if(i >= 2){
			return null;
		}
		return addBitmapList.get(i);
	}
	private static void addPage(){

		try{
			//addBitmapList.add(BitmapFactory.decodeFile(WolfTemplateUtil.TEMPLATE_PATH+WolfTemplateUtil.getCurrentTemplate().getName()+"/notebook_add_bg.png").copy(Config.ARGB_8888, true));
			addBitmapList.add(BitmapFactory.decodeFile(WolfTemplateUtil.TEMPLATE_PATH+WolfTemplateUtil.getCurrentTemplate().getName()+"/notebook_add_bg.png").copy(Config.ARGB_4444, true));
			String str=WolfTemplateUtil.TEMPLATE_PATH+WolfTemplateUtil.getCurrentTemplate().getName()+"/notebook_add_bg.png";
			BitmapCount.getInstance().createBitmap("BaseBitmap decode addBitmapList add");
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
			Log.e("AndroidRuntime", "BaseBitmap addPage() OOM!!!");
		}
	}
	
	public void setBitmap(Bitmap b){
		bitmap = b;
		
	}
	
	public Bitmap getBitmap(){
		return bitmap;
	}
	
	/*
	 * 获取开始滑动时的上层透明bitmap，涂鸦态即为唯一的那个bitmap
	 * 而光标态为透明的bitmap
	 */
	public Bitmap getTopBitmap(){
		return null;
	}
	
	/*
	 * 获取在onDraw中需要画在之上的bitmap，
	 * 涂鸦态为惟一的那个bitmap，
	 * 光标态为与涂鸦态相对应的那张底层bitmap。
	 */
	public Bitmap getBaseBitmap(){
		return bitmap;
	}
	
	/*
	 * 获取全局的bitmap(900*Start.SCREEN_HEIGHT)
	 */
	public Bitmap getMainBitmap(){
		return mBitmap;
	}
	
	public void clearDataBitmap(){
		Log.i(TAG, "clearDataBitmap");	
	}
	
	public void setBgBitmap(){
		Canvas canvas = new Canvas();
			canvas.setBitmap(mBitmap);

			//整合时将下面两句放开
//			ScreenLayer.fetchScreenLayer(Calligraph.mScreenLayerBitmap);
//			canvas.drawBitmap(Calligraph.mScreenLayerBitmap, new Rect(0, MyView.statusBarHeight, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
//					new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT-MyView.statusBarHeight),new Paint());
			
//			Bitmap bgBitmap = BitmapFactory.decodeFile("/data/data/safemode_popbg.png").copy(Bitmap.Config.ARGB_8888, true);
//			canvas.drawBitmap(bgBitmap, 0, 0, paint);
//			bgBitmap.recycle();

			canvas.drawBitmap(bView.cursorBitmap.bitmap, new Rect(300, 0, 900, Start.SCREEN_HEIGHT),new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Paint());
			
/*
 * mouse
			Bitmap mHandBgBitmap = BitmapFactory.decodeFile("/data/data/handwriting_bg.png").copy(Bitmap.Config.ARGB_8888, true);
			canvas.drawBitmap(mHandBgBitmap, Start.SCREEN_WIDTH, 0, paint);
			mHandBgBitmap.recycle();
*/
	        
			MyView.drawStatus = 0;
//			resetCursor();
	}
	
	public void doDraw(Canvas canvas, Paint paint, Matrix matrix){
		
		
		if(bView.getTouchMode() == bView.getFreeDragMode() || bView.getTouchMode() == bView.getFreeScaleMode()
				|| bView.getTouchMode() == bView.getFreeNullMode())
			canvas.drawBitmap(Calligraph.mScaleBitmap, matrix, paint);
		
//		else if(bView.getTouchMode() == bView.getCursorScaleMode() || 
//				bView.getTouchMode() == bView.getCursorNullMode()){
//			
////			System.out.println("MyView.MODE_CURSOR_SCALE"+MyView.MODE_CURSOR_SCALE);
////			System.out.println("MyView.drawStatus"+MyView.STATUS_DRAW_CURSOR);
//			
//			Matrix mm = new Matrix();
//			mm.postTranslate(-Start.SCREEN_WIDTH,  -EditableCalligraphy.flip_dst);
//			
//			canvas.drawBitmap(bCurInfo.mBitmap, mm, paint);
//			
//			if(EditableCalligraphy.flip_dst > 0) {
//				mm.postTranslate(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT);
//				canvas.drawBitmap(addBitmap, mm, paint);
//				
//				Log.i("DST", "dst:"+EditableCalligraphy.flip_dst);
//			}
//			
////			Log.e("base", "else if!!!!!!!!!!!!");
//			drawBgLine(canvas, matrix);
//			
//		}
		
		else {
			//光标态书写的时候，此处会疯狂的刷... 待提升性能
			
//			Log.e("base", "else !!!!!!!!!!!!");
//			Matrix m = new Matrix(matrix);
//			m.postTranslate(bCurInfo.mPosLeft, 0);
//			canvas.drawBitmap(bCurInfo.mBitmap, m, paint);
//			canvas.drawBitmap(bCurInfo.mBitmap, bCurInfo.mPosLeft, -EditableCalligraphy.flip_dst, paint);
//			Log.i(TAG, "draw");
			
			

			canvas.drawColor(Color.WHITE);
			
			
//			drawFreeBitmap();
			if(EditableCalligraphy.flip_dst > TITLE_HEIGHT){
				canvas.drawBitmap(mBitmap, bCurInfo.mPosLeft, -TITLE_HEIGHT, paint);
			}else
				canvas.drawBitmap(mBitmap, bCurInfo.mPosLeft, -EditableCalligraphy.flip_dst, paint);
//			canvas.drawBitmap(bView.cursorBitmap.getTopBitmap(), 0, 0, paint);
			/*
			if(EditableCalligraphy.flip_dst > 0){
				int count = EditableCalligraphy.flip_dst/Start.SCREEN_HEIGHT;
				if(EditableCalligraphy.flip_dst > Start.SCREEN_HEIGHT && addBitmapList.size() < 2){
					addPage();
				}
				for(int i=0;i<= count;i++){
					Log.e("DST", "addpage "+ EditableCalligraphy.flip_dst);
					if(i >= 2)
						break;
					canvas.drawBitmap(addBitmapList.get(i), 0, Start.SCREEN_HEIGHT-(EditableCalligraphy.flip_dst - (i*Start.SCREEN_HEIGHT)), paint);
				}
			}
			*/
			
//			if(EditableCalligraphy.flip_dst > 0){
//				int count = EditableCalligraphy.flip_dst/Start.SCREEN_HEIGHT;
//				if(EditableCalligraphy.flip_dst > Start.SCREEN_HEIGHT && addBitmapList.size() < 2){
//					addPage();
//				}
//				for(int i=0;i<= count;i++){
//					Log.e("DST", "addpage "+ EditableCalligraphy.flip_dst);
//					if(i >= 2)
//						break;
//					canvas.drawBitmap(addBitmapList.get(i), 0, Start.SCREEN_HEIGHT-(EditableCalligraphy.flip_dst - (i*Start.SCREEN_HEIGHT)), paint);
//				}
//			}
			
			if(MyView.drawStatus == MyView.STATUS_DRAW_CURSOR) {
//				if(matrix == null)
//					drawBgLine(canvas,new Matrix());
//				else
					drawBgLine(canvas, bView.getMMMatrix());
			}
			
//			System.out.println("!!!!!!!!!!!@@@@@@@@@@@@@@@@ draw else");
		}
//		canvas.drawBitmap(bCurInfo.mBitmap, bCurInfo.mPosLeft, 0, paint);
	}
	public void drawFreeBitmap(){
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		List<FreeBitmapInfo> list = bView.freeBitmap.getFreeBitmapInfoList();
		float x,y;
		Bitmap infoBitmap;
		Log.e("free","list size:" + list.size());
		for(FreeBitmapInfo info : list){
			
			infoBitmap = info.bitmap;
			x = info.rect.left + Start.SCREEN_WIDTH;
			if(EditableCalligraphy.flip_dst > TITLE_HEIGHT)
				y = info.rect.top - EditableCalligraphy.flip_dst + TITLE_HEIGHT;
			else
				y = info.rect.top - EditableCalligraphy.flip_dst;
			canvas.drawBitmap(infoBitmap, x, y, new Paint());
//	         cv.save(Canvas.ALL_SAVE_FLAG);//保存   
//	         //store   
//	         cv.restore();//存储   
			
		}
	}
	
//	private void drawBgLine(Canvas canvas){
//		int lineNum = 0;
//		line_paint.setColor(Color.GRAY); 
//		
//		for(int j=0;j<listAvailables.size();j++){
//			available = listAvailables.get(j);
//			line_top = available.getStartY();
//			line_bottom = available.getEndY();
//			line_space = template.getLinespace();
//			
////			System.out.println("drawline line_space"+ line_space);
////			System.out.println("drawline top"+ line_top);
////			System.out.println("drawline bottom" + line_bottom);
//			if(available.getLinenumber() == 1){
//				
//			}else{
//				lineNum = (line_bottom - line_top)/ line_space;
//				lineNum += EditableCalligraphy.flip_dst / template.getLinespace();
//			}
//			
//			for(int i=0;i<lineNum;i++){
//				canvas.drawLine( 20 , line_top+line_space*(i+1) - EditableCalligraphy.flip_dst, 560, line_top+line_space*(i+1)- EditableCalligraphy.flip_dst, line_paint);
//			}
//		} 
//	}
	public void drawBgLine(Canvas canvas,Matrix matrix){
		int lineNum = 0;
		int miniNum = 0;
		line_paint.setColor(Color.GRAY);
		
		matrix.getValues(matrixValue);
		
		int line_startx = 0;
		int line_endx = 0;
		for(int j=0;j<listAvailables.size();j++){
			available = listAvailables.get(j);
			if(available.getDirect() != 1){
				//横向
				line_top = available.getStartY();
				line_bottom = available.getEndY();
				
				line_startx = available.getStartX();
				line_endx = available.getEndX();
				
//				line_space = template.getLinespace();
				if(available.getAlinespace() != 0)
					line_space = available.getAlinespace();
				else
					line_space = template.getLinespace();
				
				if(available.getLinenumber() == 0)
					return;
				
				line_space = (int) (line_space * matrixValue[4]);
				
				lineNum = (line_bottom - line_top)/ line_space;
				miniNum = lineNum;
				
				if(available.getLinenumber() == 1){
					
				}else{
					
//					Log.e("BaseBitmap","linespace:"+ line_space);
					
					if(lineNum + EditableCalligraphy.flip_dst / template.getLinespace() > miniNum)
						lineNum += EditableCalligraphy.flip_dst / template.getLinespace();
				
					lineNum += 10;
					
					for(int i=0;i<lineNum;i++){
//						Log.e("line", "direct:"+available.getDirect()+" lineNumber:"+available.getLinenumber());
//						canvas.drawLine( 20 , line_top+line_space*(i+1) - EditableCalligraphy.flip_dst, 560, line_top+line_space*(i+1)- EditableCalligraphy.flip_dst, line_paint);
						canvas.drawLine( line_startx , 
								line_top+line_space*(i+1) - EditableCalligraphy.flip_dst, 
								line_endx, 
								line_top+line_space*(i+1)- EditableCalligraphy.flip_dst, 
								line_paint);

					}
				}
			}else{
				//竖向
//				line_top = available.getStartX();
//				line_bottom = available.getEndX();
				line_top = available.getEndX();
				line_bottom = available.getStartX();	
				line_space = template.getLinespace();
				if(available.getLinenumber() == 0)
					return;
				
				line_bottom = (int) (Start.SCREEN_WIDTH - (Start.SCREEN_WIDTH - line_bottom) * matrixValue[0]);
				
				line_space = (int) (line_space * matrixValue[4]);
				Log.e("mindmapY", "line_space:" + line_space);
				
				lineNum = (line_bottom - line_top)/ line_space;
				miniNum = lineNum;
				
				if(available.getLinenumber() == 1){
					
				}else{
					if(lineNum + EditableCalligraphy.flip_Horizonal_dst / template.getLinespace() > miniNum)
						lineNum += EditableCalligraphy.flip_Horizonal_dst / template.getLinespace();
				
					for(int i=0;i<=lineNum+1;i++){
//						canvas.drawLine( 20 , line_top+line_space*(i+1) - EditableCalligraphy.flip_dst, 560, line_top+line_space*(i+1)- EditableCalligraphy.flip_dst, line_paint);
//						canvas.drawLine( line_top+line_space*(i+1) - EditableCalligraphy.flip_dst
//								, available.getStartY() 
//								, line_top+line_space*(i+1)- EditableCalligraphy.flip_dst
//								, available.getEndY() - 20
//								, line_paint
//								);
						
						
						canvas.drawLine( line_bottom-line_space*(i) + EditableCalligraphy.flip_Horizonal_dst
								, available.getStartY() 
								, line_bottom-line_space*(i) + EditableCalligraphy.flip_Horizonal_dst
								, available.getEndY() - 20
								, line_paint
								);
					}
				}
			}
		}//for
	}
	

	public void updateState()
	{
//		if(bitmap != null){
//			Canvas canvas = new Canvas();
//			canvas.setBitmap(bitmap);
//			//FIXME: implement it in a graceful way
//			canvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Rect(0, 0, 300, Start.SCREEN_HEIGHT), new Paint());
//		}
	}
	public boolean forceUpdateTopBitmap()
	{
		return false;
	}
	public void start(float x, float y){
//		Log.i(TAG, "start");
		TransparentChanged = true;
		if(bView.getTouchMode() == bView.freeDragMode)
			Start.status.modified("tuya modify");

		extendBound(x,y);
		extendBoundSingle(x, y);
	}
	
	public void move(float x, float y){

        extendBound(x,y);
        extendBoundSingle(x, y);
	}
	
	public void after(){

		resetBoundSingle();
	}
	
	public void finish(){
		
	}
	
	public void extendBound(float x , float y) {
		if(x < mStartX)
        		mStartX = x;
        	if(y < mStartY)
        		mStartY = y;
		if(x > mEndX)
			mEndX = x;
		if(y> mEndY)
			mEndY = y;
	}
	
	public void resetBound() {
		mStartX = Float.MAX_VALUE;
		mStartY = Float.MAX_VALUE;
		mEndX = 0;
		mEndY = 0;
	}
	
	public void extendBoundSingle(float x , float y) {
        if(x < sStartX)
                sStartX = x;
        if(y < sStartY)
                sStartY = y;
        if(x > sEndX)
                sEndX = x;
        if(y> sEndY)
                sEndY = y;
	}

	public void resetBoundSingle() {
		sStartX = Float.MAX_VALUE;
		sStartY = Float.MAX_VALUE;
		sEndX = 0;
		sEndY = 0;
	}

}
