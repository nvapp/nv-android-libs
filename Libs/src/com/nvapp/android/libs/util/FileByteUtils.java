package com.nvapp.android.libs.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileByteUtils {
	public static byte[] file2byte(File f) throws Exception {
		return file2byte(f.getPath());
	}

	public static byte[] file2byte(String f) {
		InputStream in = null;
		try {
			in = new FileInputStream(f);
			byte[] tmp = new byte[512];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int bytesRead = in.read(tmp);
			while (bytesRead != -1) {
				out.write(tmp, 0, bytesRead);
				bytesRead = in.read(tmp);
			}
			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}

			}
		}
		return null;
	}

	// writes byte [] to a file
	public static void byte2file(byte[] data, String fn) throws Exception {
		try {
			@SuppressWarnings("resource")
			OutputStream out = new FileOutputStream(fn);
			out.write(data);
			out.flush();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
}
