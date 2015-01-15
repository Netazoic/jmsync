NetaMTP

Quick and Dirty utility project for copying a set of files to a named MTP directory (i.e., a directory on an Android phone).

This project was set up for moving over wav files in an encoded format, so it first runs the selected files through a FLAC encoder, then moves the encoded files to a specified "directory" on the phone.

Reason for this project: MTP is a hassle, actually kind of a b*atch.  So after wrestling with various mechanims for syncing the phone with local (workstation) files, I finally built my own.

It is not perfect:

	o	Windows (7) mtp will not recognize new files on the phone until the phone is actually rebooted. 87\
	o	Uses ADB to effect file transfer. Believe me, I tried other things first!


