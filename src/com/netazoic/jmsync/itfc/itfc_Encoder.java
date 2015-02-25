package com.netazoic.jmsync.itfc;

import java.io.File;

import com.netazoic.jmsync.FLAC_Encoder;
import com.netazoic.jmsync.MPG_Encoder;

public interface itfc_Encoder {
	public static Integer CD_SAMPLE_RATE =  44100;
	public static Integer DVD_SAMPLE_RATE = 48000;

	public enum ENC_Format{
		mp3(MPG_Encoder.class),
		flac(FLAC_Encoder.class),
		none(null);
		
		public Class<?> encClass;

		ENC_Format(Class<?> c){
			encClass = c;
		}
	}
	
	public abstract void encodeFile(File src, File tgt) throws Exception;
	
	public String getExtension();
}