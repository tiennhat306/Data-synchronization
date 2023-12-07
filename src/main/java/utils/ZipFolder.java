package utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFolder {
	List<String> fileList;
//	private static final String OUTPUT_ZIP_FILE = "D:\\User\\Desktop\\foldertest.zip";
//	private static final String SOURCE_FOLDER = "D:\\User\\Desktop\\foldertest";
	private final String OUTPUT_ZIP_FILE;
	private final String SOURCE_FOLDER;

	public ZipFolder(String folderName, String sourceFolder){
		String downloadFolder = System.getProperty("user.home") + File.separator + "Downloads";
		OUTPUT_ZIP_FILE = downloadFolder + File.separator + folderName + ".zip";
		SOURCE_FOLDER = sourceFolder;
		fileList = new ArrayList<>();
	}

	public String zip(){
		generateFileList(new File(SOURCE_FOLDER));
		zipIt(OUTPUT_ZIP_FILE);

		return OUTPUT_ZIP_FILE;
	}

	public long size() {
		File file = new File(OUTPUT_ZIP_FILE);
		return file.length();
	}

	/**
	 * Zip it
	 * @param zipFile output ZIP file location
	 */
	public void zipIt(String zipFile){
		byte[] buffer = new byte[1024];
		try{
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for(String file : this.fileList){
				ZipEntry ze= new ZipEntry(file);
				zos.putNextEntry(ze);
				FileInputStream in =
						new FileInputStream(SOURCE_FOLDER + File.separator + file);
				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				in.close();
			}
			zos.closeEntry();
			//remember close it
			zos.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	/**
	 * Traverse a directory and get all files,
	 * and add the file into fileList
	 * @param node file or directory
	 */
	public void generateFileList(File node){
		//add file only
		if(node.isFile()){
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}
		if(node.isDirectory()){
			String[] subNote = node.list();
			assert subNote != null;
			for(String filename : subNote){
				generateFileList(new File(node, filename));
			}
		}
	}
	/**
	 * Format the file path for zip
	 * @param file file path
	 * @return Formatted file path
	 */
	private String generateZipEntry(String file){
		return file.substring(SOURCE_FOLDER.length()+1);
	}

	public void deleteOutputZipFile() {
		File file = new File(OUTPUT_ZIP_FILE);
		if(file.delete()) {
			System.out.println("Deleted output zip file");
		}
	}
}