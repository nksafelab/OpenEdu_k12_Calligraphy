package com.jinke.calligraphy.database;

import android.graphics.Matrix;

public class MatrixHelper {
	public static Matrix getMatrix(String stringMatrix) {
		Matrix m = new Matrix();
		String tmp = stringMatrix.substring(8, stringMatrix.length() - 2);
		tmp = tmp.replace("][", ",").replace(" ", "");

		String[] t = tmp.split(",");
		float[] mValues = new float[9];
		float f;
		for (int i = 0; i < t.length; i++) {

			f = Float.parseFloat(t[i]);
			mValues[i] = f;
		}
		m.setValues(mValues);
		return m;
	}
}
