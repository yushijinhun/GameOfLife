package com.github.yushijinhun.gameoflife.util;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

public final class ExceptionUtil {

	public static void showExceptionDialog(Throwable e, Thread t, String info) {
		StringBuilder sb = new StringBuilder();
		if (info != null) {
			sb.append(info);
		}

		sb.append("\nException in thread \"");
		sb.append(t.getName());
		sb.append("\" ");

		CharArrayWriter writer = new CharArrayWriter();
		e.printStackTrace(new PrintWriter(writer));
		sb.append(writer.toCharArray());
		writer.close();

		JOptionPane.showMessageDialog(null,
				sb.toString().replaceAll("\t", "    "), "Game of Life",
				JOptionPane.ERROR_MESSAGE);
	}

	private ExceptionUtil() {

	}
}
