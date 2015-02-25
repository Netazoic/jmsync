package com.netazoic.jmsync;

import com.netazoic.jmsync.itfc.itfc_Encoder;

public abstract class Encoder<T extends Encoder> implements itfc_Encoder {
	public ENC_Format encFormat = null;

	public String getExtension(){
		 return encFormat.name();
	}
}
