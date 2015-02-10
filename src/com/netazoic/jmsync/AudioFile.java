package com.netazoic.jmsync;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFile extends File {
	
	public AudioFile(String filePath) {
		super(filePath);
	}
	
	public AudioFile(File parentDir, String fileName) {
		super(parentDir, fileName);
	}
	
	/*
	 * Guts of this borrowed from
	 * Original code by Matthias Pfisterer
	 *
	 *	SampleRateConverter.java
	 *
	 *	part of jsresources.org
	 */
	
	
	public AudioFormat getAudioFormat() throws UnsupportedAudioFileException, IOException{
        AudioInputStream stream = AudioSystem.getAudioInputStream(this);
        AudioFormat afmt = stream.getFormat();
        stream.close();
        return afmt;
	}
	
	public AudioFileFormat getFileFormat() throws UnsupportedAudioFileException, IOException{
		AudioFileFormat		fileFormat = AudioSystem.getAudioFileFormat(this);
		return fileFormat;
	}
	
	public Float getSampleRate() throws UnsupportedAudioFileException, IOException{
		return getAudioFormat().getSampleRate();
	}
	
	public AudioFileFormat.Type getType() throws UnsupportedAudioFileException, IOException{
		AudioFileFormat.Type fileType = this.getFileFormat().getType();
		return fileType;
	}

	public File convertSampleRate(Float	fTargetSampleRate, boolean DEBUG) throws UnsupportedAudioFileException, IOException{		
		AudioFileFormat		sourceFileFormat = AudioSystem.getAudioFileFormat(this);
		AudioFileFormat.Type	targetFileType = sourceFileFormat.getType();

		//File targetFile = new File(fTargetSampleRate.toString() + "_" + this.getName());
		File targetFile = File.createTempFile("rate_converter_", "." +targetFileType.getExtension());


		/* Here, we are reading the source file.
		 */
		AudioInputStream	sourceStream = null;
		sourceStream = AudioSystem.getAudioInputStream(this);
		if (sourceStream == null)
		{
			out("cannot open source audio file: " + this);
			System.exit(1);
		}
		AudioFormat	sourceFormat = sourceStream.getFormat();
		if (DEBUG)  { out("source format: " + sourceFormat); }

		/* Currently, the only known and working sample rate
		   converter for Java Sound requires that the encoding
		   of the source stream is PCM (signed or unsigned).
		   So as a measure of convenience, we check if this
		   holds here.
		*/
		AudioFormat.Encoding	encoding = sourceFormat.getEncoding();
		if (! org.jsresources.AudioCommon.isPcm(encoding))
		{
			out("encoding of source audio data is not PCM; conversion not possible");
			System.exit(1);
		}

		/* Since we now know that we are dealing with PCM, we know
		   that the frame rate is the same as the sample rate.
		*/
		float		fTargetFrameRate = fTargetSampleRate;

		/* Here, we are constructing the desired format of the
		   audio data (as the result of the conversion should be).
		   We take over all values besides the sample/frame rate.
		*/

		AudioFormat	targetFormat = new AudioFormat(
			sourceFormat.getEncoding(),
			fTargetSampleRate,
			sourceFormat.getSampleSizeInBits(),
			sourceFormat.getChannels(),
			sourceFormat.getFrameSize(),
			fTargetFrameRate,
			sourceFormat.isBigEndian());

		if (DEBUG)  { out("desired target format: " + targetFormat); }

		/* Now, the conversion takes place.
		 */
		AudioInputStream	targetStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
		if (DEBUG) { out("targetStream: " + targetStream); }

		/* And finally, we are trying to write the converted audio
		   data to a new file.
		*/
		int	nWrittenBytes = 0;
		nWrittenBytes = AudioSystem.write(targetStream, targetFileType, targetFile);
		if (DEBUG) { out("Written bytes: " + nWrittenBytes); }
		return targetFile;
	}
	
	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}


}
