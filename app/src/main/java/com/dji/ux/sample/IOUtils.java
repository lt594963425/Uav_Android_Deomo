package com.dji.ux.sample;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	/**
	 * 关闭流
	 */
	public static boolean close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				LogUtil.e("close", e.getMessage());
			}
		}
		return true;
	}

	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		write(input, output);
		output.close();
		return output.toByteArray();
	}

	public static void write(InputStream inputStream, OutputStream outputStream) throws IOException {
		int len;
		byte[] buffer = new byte[4096];
		while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
	}

}
