package hallelujah.cal.ctrl;

import hallelujah.cal.parser.CalligraphyParser;

import java.io.IOException;

public class ParserFactory {
	private static ParserFactory sFactory = null;

	public static ParserFactory instance() {
		if (null == sFactory) {
			sFactory = new ParserFactory();
		}

		return sFactory;
	}

	public CalligraphyParser newParser(String szType, String szFilePath, int nPos) throws IOException {
		if (szType.equals("single")) {
			return ParserController.newParser(szFilePath, nPos);
		} else {
			throw new IOException("At present, not support other parser type");
		}
	}
}
