package com.netazoic.jmsync;

import java.io.File;
import java.io.IOException;

import javaFlacEncoder.FLAC_FileEncoder;
import javaFlacEncoder.StreamConfiguration;

import javax.sound.sampled.UnsupportedAudioFileException;
/*
 * Convert a PCM file (e.g., Wav file) into FLAC format.
 * The generic use of this is for transfer to an MTP player, and in my case, and Android phone.
 */
public class FLAC_Encoder extends Encoder<FLAC_Encoder> implements itfc_Encoder {


	public void encodeFile(File src, File tgt) throws UnsupportedAudioFileException, IOException{
		System.out.println("Starting encode of  " + src.getName());
		Float targetSampleRate = CD_SAMPLE_RATE.floatValue();
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
        FLAC_Encoder flacEncoder = new FLAC_Encoder();
        File inputFile = new File("hello.wav");
        File outputFile = new File("hello.flac");

        try {
			flacEncoder.encodeFile(inputFile, outputFile);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Done");
    }
}
