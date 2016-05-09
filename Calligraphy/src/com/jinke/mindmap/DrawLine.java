package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface DrawLine {
   public void operate(LocateDot from, LocateDot to, Canvas c, Paint mp,float arrawWidth);
}
