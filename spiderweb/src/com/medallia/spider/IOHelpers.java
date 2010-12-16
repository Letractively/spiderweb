package com.medallia.spider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Static utility functions for IO related tasks.
 *
 */
public class IOHelpers {
	
	private static final int COPY_BUFFER_SIZE = 1024 * 4;
	
	/** Copy input to output; neither stream is closed.
	 * 
	 * @return the number of bytes copied
	 */
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	/** Copy input to output; neither is closed.
	 * 
	 * @return the number of bytes copied
	 */
	public static int copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[COPY_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Read the bytes from the given stream into a byte array.
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

}
