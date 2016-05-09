package hallelujah.cal.ctrl;

import java.io.IOException;

import hallelujah.cal.producer.CalligraphyProducer;

public final class ProducerFactory {
	private static ProducerFactory sFactory = null;

	public static ProducerFactory instance() {
		if (null == sFactory) {
			sFactory = new ProducerFactory();
		}

		return sFactory;
	}

	public CalligraphyProducer newProducer(String szType, String szFilePath, int nPos) throws IOException {
		if (szType.equals("single")) {
			return ParserController.newProducer(szFilePath, nPos);
		} else {
			throw new IOException("not support other prducer type");
		}
	}
}
