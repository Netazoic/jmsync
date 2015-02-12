package com.netazoic.jmsync;

public abstract class Encoder<T extends Encoder> implements itfc_Encoder {
	public ENC_Format encFormat = null;

	public String getExtension(){
		 return encFormat.name();
	}
}
