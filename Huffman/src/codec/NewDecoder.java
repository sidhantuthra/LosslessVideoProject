package codec;

import java.io.IOException;
import java.util.HashMap;
import io.BitSource;
import io.InsufficientBitsLeftException;
import models.Symbol;
import models.SourceModel;

public class NewDecoder implements SymbolDecoder{

	public HashMap<String, Integer> table = new HashMap<String, Integer>();
	private String[] charArray;
	private int count;
	private SourceModel _model;
	
	public NewDecoder(SourceModel m) {
		_model = m;
	}
	
	public SourceModel model() {
		return _model;
	}

	public void model(SourceModel m) {
		_model = m;
	}

	@Override
	public Symbol decode(BitSource bit_source) throws InsufficientBitsLeftException, IOException {

		charArray = new String[4096];
		for (int i = 0; i < 256; i++) {
			table.put(Character.toString((char) i), i);
			charArray[i] = Character.toString((char) i);
		}
		count = 256;

		int currentword, priorword;
		byte[] buffer = new byte[3];
		boolean left = true;
		
		// Get first word and output its  character
		priorword = getvalue(buffer[0], buffer[1], left);
		left = !left;

		// Read every 3 bytes and generate characters
		while (true) {
			if (left) {
				currentword = getvalue(buffer[0], buffer[1], left);
			} else {
				currentword = getvalue(buffer[1], buffer[2], left);
			}
			left = !left;
			if (currentword >= count) {

				if (count < 4096)
					charArray[count] = charArray[priorword]
					+ charArray[priorword].charAt(0);
					count++;
				} else {

					if (count < 4096)
					charArray[count] = charArray[priorword]
					+ charArray[currentword].charAt(0);
					count++;
				}
				priorword = currentword;

		} 

	}
	
	public int getvalue(byte b1, byte b2, boolean onleft) {
		String temp1 = Integer.toBinaryString(b1);
		String temp2 = Integer.toBinaryString(b2);
		while (temp1.length() < 8) {
			temp1 = "0" + temp1;
		}
		if (temp1.length() == 32) {
			temp1 = temp1.substring(24, 32);
		}
		while (temp2.length() < 8) {
			temp2 = "0" + temp2;
		}
		if (temp2.length() == 32) {
			temp2 = temp2.substring(24, 32);
		}

		// If left is true
		if (onleft) {
			return Integer.parseInt(temp1 + temp2.substring(0, 4), 2);
		} else {
			return Integer.parseInt(temp1.substring(4, 8) + temp2, 2);
		}

	}

}
