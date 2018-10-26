package net.ratcash.sqlite.zipvfs.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class NdsDecompressor {

	public static void main(String[] args) {
		System.out.println("specs: http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki\n");

		try {
			new NdsDecompressor().decodeNDS(args[0]);
		} catch (IOException | DataFormatException ex) {
			Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void decodeNDS(String filepath) throws FileNotFoundException, IOException, DataFormatException {
		File dbFile = new File(filepath);
		String convertedFile = dbFile.getAbsolutePath() + ".sqlite";
		
		try (FileChannel fc = FileChannel.open(dbFile.toPath(), StandardOpenOption.READ)) {
			ZipVfsFile zipvfs = new ZipVfsFile();

			zipvfs.parse(fc);

			//System.out.println(zipvfs);

			if (!zipvfs.isReadable()) {
				try {
					// fixed black size: Blowfish AES RSA DES
					// to validate: RC2
					// candidate: RC4 ARCFOUR
					zipvfs.findCipherKey("RC4", 16);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
				}
			}
			
			this.convert(zipvfs, convertedFile);
		}
	}
	
	public void convert(ZipVfsFile zipvfs, String filepath) throws DataFormatException, IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(filepath));
		
		zipvfs.pipe(outputStream);
		outputStream.close();
		
		System.out.println("Conversion done.\nOpen '" + filepath + "' in your faviroute Sqlite Front-End.");
	}

}
