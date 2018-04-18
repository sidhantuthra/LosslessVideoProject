package codec;

import java.io.*;
import java.util.*;
import io.BitSink;
import models.SourceModel;
import models.Symbol;

public class NewEncoder implements SymbolEncoder{
	
	public HashMap<String, Integer> table = new HashMap<String, Integer>();
	private String[] charArray;
	private int count;
	private boolean _closed;
	private SourceModel _model;
	
	public SourceModel model() {
		return _model;
	}
	
	public void model(SourceModel m) {
		_model = m;
	}
	
	public NewEncoder(SourceModel m) {
		_model = m;
	}

	@Override
	public void encode(Symbol s, BitSink out) throws IOException {
		charArray = new String[4096];
		for (int i = 0; i < 256; i++) {
			table.put(Character.toString((char) i), i);
			charArray[i] = Character.toString((char) i);
		}
		count = 256;

		byte input_byte = 0;
		String temp = "";
		byte[] buffer = new byte[3];
		boolean left = true;

		// Read first character from file
		int i = new Byte(input_byte).intValue();
		if (i < 0) {
			i += 256;
		}
		char c = (char) i;
		temp = "" + c;

		// Read each character
		while (true) {
			i = new Byte (input_byte).intValue();

			if (i < 0) {
				i += 256;
			}
			c = (char) i;

			if (table.containsKey(temp + c)) {
				temp = temp + c;
			} else {
				String s1 = to12bit(table.get(temp));
				// Store 12 bits into array
				if (left) {
					buffer[0] = (byte) Integer.parseInt(
							s1.substring(0, 8), 2);
					buffer[1] = (byte) Integer.parseInt(
							s1.substring(8, 12) + "0000", 2);
				} else {
					buffer[1] += (byte) Integer.parseInt(
							s1.substring(0, 4), 2);
					buffer[2] = (byte) Integer.parseInt(
							s1.substring(4, 12), 2);
					for (int b = 0; b < buffer.length; b++) {

						buffer[b] = 0;
					}
				}
				left = !left;
				if (count < 4096) {
					table.put(temp + c, count++);
				}
				temp = "" + c;
			}
		}

	}

	// Converting 8bit chars to 12bit
	public String to12bit(int i) {
		String temp = Integer.toBinaryString(i);
		while (temp.length() < 12) {
			temp = "0" + temp;
		}
		return temp;
	}

	@Override
	public void close(BitSink out) throws IOException {
		return;
		
	}

	@Override
	public Map<Symbol, String> getCodeMap() {
		return null;
	}
}
