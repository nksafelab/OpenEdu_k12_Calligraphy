package com.jinke.calligraphy.app.branch;

import android.graphics.Bitmap;

public interface Command {
	public void execute();
	public void undo(Bitmap b);
}
