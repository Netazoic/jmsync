package com.netazoic.jmsync;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
/*
 * Convert a PCM file (e.g., Wav file) into MPG format.
 * The generic use of this is for transfer to an MTP player, and in my case, and Android phone.
 */
public class MPG_Encoder extends com.netazoic.jmsync.Encoder<MPG_Encoder> implements itfc_Encoder {
	boolean flgDebug = false;

	public void encodeFile(File src, File tgt) throws Exception{
		if(flgDebug)System.out.println("Starting encode of  " + src.getName());
		Integer targetSampleRate = CD_SAMPLE_RATE;
		Encoder mpg = new it.sauronsoftware.jave.Encoder();
		AudioFile f = new AudioFile(src.getAbsolutePath());
		File temp = null;
		try{
			if(f.getSampleRate() > DVD_SAMPLE_RATE){
				//Have to downsample the input for use on most Android players
				boolean flgDebug = true;
				try{
					temp = f.convertSampleRate(targetSampleRate.floatValue(), flgDebug);
					src = temp;
				}catch(IOException ex){
					throw new Exception(ex);
				}catch (UnsupportedAudioFileException e) {
					throw new Exception(e);
				}
			}
			AudioAttributes audio = new AudioAttributes();
			audio.setCodec("libmp3lame");
			audio.setBitRate(new Integer(128000));
			audio.setChannels(new Integer(2));
			audio.setSamplingRate(CD_SAMPLE_RATE);
			EncodingAttributes attrs = new EncodingAttributes();
			attrs.setFormat("mp3");
			attrs.setAudioAttributes(audio);
			Encoder encoder = new Encoder();
			encoder.encode(src, tgt, attrs);
			encoder = null;
		}catch(IllegalArgumentException ex){
			throw new Exception(ex);
		}finally{
			if(temp != null) temp.delete();
		}
	}


	public static void main(String[] args) throws Exception {
		MPG_Encoder mpgEncoder = new MPG_Encoder();
		File inputFile = new File("hello.wav");
		File outputFile = new File("hello.flac");

		try {
			mpgEncoder.encodeFile(inputFile, outputFile);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
}
