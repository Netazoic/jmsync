package com.netazoic.jmsync;

import java.io.File;
import java.io.IOException;
import javaFlacEncoder.FLAC_FileEncoder;
import javaFlacEncoder.StreamConfiguration;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
/*
 * Convert a PCM file (e.g., Wav file) into FLAC format.
 * The generic use of this is for transfer to an MTP player, and in my case, and Android phone.
 */
public class FLAC_Encoder {
	public static Float CD_SAMPLE_RATE = (float) 44100.0;
	public static Float DVD_SAMPLE_RATE = (float) 48000.0;

	public void encodeFile(File src, File tgt) throws UnsupportedAudioFileException, IOException{
		System.out.println("Starting encode of  " + src.getName());
		Float targetSampleRate = CD_SAMPLE_RATE;
		FLAC_FileEncoder flac = new FLAC_FileEncoder();
        AudioFile f = new AudioFile(src.getAbsolutePath());
        if(f.getSampleRate() > DVD_SAMPLE_RATE){
        	//Have to downsample the input for use on most Android players
        	/*
        	 * FLAC files take on the sample rate of the source they are created from, and
        	 * it is not possible (or at least not easy) to convert the sample rate
        	 * of the FLAC once it has been generated. So we are going to create a 
        	 * temporary copy of the source material that is in the desired sample rate
        	 * before converting.
        	 */
        	boolean flgDebug = true;
        	File temp = null;
        	try{
        		temp = f.convertSampleRate(targetSampleRate, flgDebug);
            	flac.encode(temp, tgt);
        	}catch(IOException ex){
        		throw ex;
        	}finally{
        		if(temp != null) temp.delete();
        	}
        }
        else{
    		flac.encode(src, tgt);
        }
		flac = null;
	}
	
	
    public static void main(String[] args) {
        FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
        File inputFile = new File("hello.wav");
        File outputFile = new File("hello.flac");
        //EncodingConfiguration ec = new EncodingConfiguration();
        StreamConfiguration sc = new StreamConfiguration();
        sc.setSampleRate(41000);
        flacEncoder.setStreamConfig(sc);

        flacEncoder.encode(inputFile, outputFile);
        System.out.println("Done");
    }
}
