package com.netazoic.netamtp;

import java.io.File;
import javaFlacEncoder.FLAC_FileEncoder;

public class FLAC_Encoder {

	public void encodeFile(File src, File tgt){
		System.out.println("Start of enc.encodeFile");
		FLAC_FileEncoder flac = new FLAC_FileEncoder();
		flac.encode(src, tgt);
		flac = null;
	}
	
	
    public static void main(String[] args) {
        FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
        File inputFile = new File("hello.wav");
        File outputFile = new File("hello.flac");

        flacEncoder.encode(inputFile, outputFile);
        System.out.println("Done");
    }
}
