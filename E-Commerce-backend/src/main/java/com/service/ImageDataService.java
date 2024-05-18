package com.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.repository.ImageDataDAO;

@Service
public class ImageDataService {

	private ImageDataDAO imageDataDAO;

	@Value("${app.uploadDirectory}")
	private String uploadDirectory;

	/**
	 * Constructor for spring injection.
	 * 
	 * @param productDAO
	 */
	public ImageDataService(ImageDataDAO imageDataDAO) {
		this.imageDataDAO = imageDataDAO;
	}

	public String saveImagesToStorage(String type, UUID holderId, List<MultipartFile> imageFiles) throws IOException {
		Path directory = Paths.get(uploadDirectory, holderId.toString(), type);
		Files.createDirectories(directory);

		String zipFileName = type + "_images.zip";
		Path zipFilePath = directory.resolve(zipFileName);

		if (Files.exists(zipFilePath)) {
			updateZipFile(zipFilePath, imageFiles);
		} else {
			createZipFile(zipFilePath, imageFiles);
		}

		return zipFileName;
	}
	    

	private void createZipFile(Path zipFilePath, List<MultipartFile> imageFiles) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

			for (MultipartFile imageFile : imageFiles) {
				String uniqueFileName = imageFile.getOriginalFilename();
				ZipEntry zipEntry = new ZipEntry(uniqueFileName);
				zipOutputStream.putNextEntry(zipEntry);

				byte[] buffer = new byte[1024];
				int len;
				try (InputStream inputStream = imageFile.getInputStream()) {
					while ((len = inputStream.read(buffer)) > 0) {
						zipOutputStream.write(buffer, 0, len);
					}
				}
			}

			zipOutputStream.closeEntry();
			zipOutputStream.finish();

			Files.write(zipFilePath, baos.toByteArray());
		}
	}


	private void updateZipFile(Path zipFilePath, List<MultipartFile> imageFiles) throws IOException {
		Map<String, String> env = new HashMap<>();
		env.put("create", "false");

		try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFilePath, env)) {
			for (MultipartFile imageFile : imageFiles) {
				String uniqueFileName =  imageFile.getOriginalFilename();
				Path zipEntryPath = zipFileSystem.getPath(uniqueFileName);

				if (!Files.exists(zipEntryPath)) {
					try (InputStream inputStream = imageFile.getInputStream()) {
						Files.copy(inputStream, zipEntryPath);
					}
				} else {
					try (InputStream inputStream = imageFile.getInputStream()) {
						Files.delete(zipEntryPath);
						Files.copy(inputStream, zipEntryPath);
					}
				}
			}
		}
	}



	public void deleteImageFromZip(String type, UUID holderId, String imageName) throws IOException {

		Path zipFilePath = Paths.get(uploadDirectory, holderId.toString(), type, type + "_images.zip");
		Map<String, String> env = new HashMap<>();
		env.put("create", "false");
		try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFilePath, env)) {
			Path imagePath = zipFileSystem.getPath(imageName);
			if (Files.exists(imagePath)) {
				Files.delete(imagePath);
			} else {
				System.out.println("Image '" + imageName + "' does not exist in the ZIP file.");
			}
		}
	}

	
	public String getZipImages(String holderId, String type) throws IOException {
	    String directoryPath = Paths.get(uploadDirectory, holderId.toString(), type).toString();

	    Path directory = Paths.get(directoryPath);
	    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
	        throw new IOException("Directory does not exist or is not a directory: " + directoryPath);
	    }

	    String zipFilePath = Paths.get(directoryPath, type + "_images.zip").toString();
	    return zipFilePath;
	}
	
	public String deleteProductFolder(UUID productId) {
		try {

			String directoryPath = Paths.get(uploadDirectory, productId.toString()).toString();

			FileUtils.deleteDirectory(new File(directoryPath));

			return "Images deleted successfully for holderId: " + productId;
		} catch (IOException e) {
			e.printStackTrace();
			return "Error deleting images for holderId: " + productId;
		}
	}
	
	public String deleteInventoryFolder(UUID holderId, UUID inventoryId,  String type) {
		try {

			String directoryPath = Paths.get(uploadDirectory, holderId.toString(), inventoryId + "_" + type).toString();

			FileUtils.deleteDirectory(new File(directoryPath));

			return "Images deleted successfully for holderId: " + holderId;
		} catch (IOException e) {
			e.printStackTrace();
			return "Error deleting images for holderId: " + holderId;
		}
	}

	
}
