package com.mucommander.file.impl.rar.provider.de.innosystec.unrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception.RarException;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader;

public class MVTest {

	/**
	 * @param args
	 */
	/* arik
	public static void main(String[] args) {
		String filename="/home/Avenger/testdata/test2.part01.rar";
		File f = new File(filename);
		Archive a=null;
		try {
			a = new Archive(f);
		} catch (RarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(a!=null){
			a.getMainHeader().print();
			FileHeader fh = a.nextFileHeader();
			while(fh!=null){	
				try {
					File out = new File("/home/Avenger/testdata/"+fh.getFileNameString().trim());
					System.out.println(out.getAbsolutePath());
					FileOutputStream os = new FileOutputStream(out);
					a.extractFile(fh, os);
					os.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RarException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fh=a.nextFileHeader();
			}
		}
	}*/
}



