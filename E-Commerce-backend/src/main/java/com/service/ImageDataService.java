package com.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.repository.ImageDataDAO;

import net.coobird.thumbnailator.Thumbnails;

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

	public String saveImageToStorage(String type, UUID holderId, MultipartFile imageFile) throws IOException {
		String uniqueFileName = UUID.randomUUID().toString() + "_" + holderId + "_" +  LocalDateTime.now().toLocalDate().toString() + "_" +  imageFile.getOriginalFilename();
		Path directory;
		directory = Paths.get(uploadDirectory, holderId.toString(), type);

		Path filePath = directory.resolve(uniqueFileName);

		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}

		int[] dimensions = getImageDimensions(type);

		Thumbnails.of(imageFile.getInputStream()).size(dimensions[0], dimensions[1]) // Set your desired size
				.outputQuality(1).outputFormat("jpeg").toFile(filePath.toFile());
		
		return filePath.toString(); // Return the absolute path to the saved image
	}

	private int[] getImageDimensions(String type) {
		int width, height;
		switch (type) {
		case "profile":
			width = 100; // Set profile picture width
			height = 100; // Set profile picture height
			break;
		case "thumbnail":
			width = 400; // Set thumbnail width
			height = 200; // Set thumbnail height
			break;
		case "gallery":
			width = 800; // Set gallery image width
			height = 600; // Set gallery image height
			break;
		default:
			throw new IllegalArgumentException("Invalid image type: " + type);
		}
		return new int[] { width, height };
	}

	/*
	 * public byte[] getProductImages(String holderId, String type) throws
	 * IOException { Path directory = Paths.get(uploadDirectory, holderId, type,
	 * "f47dddc4-bc40-4d17-93ea-cb9d2d0cb77a_2_SharedScreenshot.jpg.png");
	 * 
	 * byte[] imageBytes = Files.readAllBytes(directory); return imageBytes;
	 * 
	 * 
	 * // Check if the directory exists if (!Files.exists(directory)) { throw new
	 * IOException("Directory does not exist: " + directory); }
	 * 
	 * 
	 * List<byte[]> imageBytesList = new ArrayList<>();
	 * 
	 * // List all files in the directory try (DirectoryStream<Path> stream =
	 * Files.newDirectoryStream(directory)) { for (Path imagePath : stream) { //
	 * Check if the file is a regular file (not a directory) if
	 * (Files.isRegularFile(imagePath)) { // Read the image file into byte array
	 * byte[] imageBytes = Files.readAllBytes(imagePath);
	 * imageBytesList.add(imageBytes); } } }
	 * 
	 * return imageBytesList; }
	 * 
	 */

//	public byte[] getImage(UUID holderId, String type) throws IOException {
//	    Path directory = Paths.get(uploadDirectory, holderId.toString(), type);
//	    
//	    try (Stream<Path> paths = Files.list(directory)) {
//	        Optional<Path> firstFile = paths
//	                .filter(path -> path.getFileName().toString().contains(holderId.toString()))
//	                .findFirst(); // Find the first file whose name contains the holderId
//
//	        if (firstFile.isPresent()) {
//	            // Read bytes from the first file found
//	            return Files.readAllBytes(firstFile.get());
//	        } else {
//	            // Handle case where no file matching the criteria is found
//	            throw new FileNotFoundException("No file found containing the holderId: " + holderId);
//	        }
//	    }
//	}

	public String deleteImage(String imageType, String imageName) throws IOException {
		// Split the string by underscores ('_')
        String[] parts = imageName.split("_");
        String holderId = parts[1];
		Path imagePath = Path.of(uploadDirectory, holderId, imageType, imageName);
		if (Files.exists(imagePath)) {
			Files.delete(imagePath);
			return "Success";
		} else {
			return "Failed"; 
		}
	}

	public List<Map<String, Object>> getImages(String holderId, String type) throws IOException {
	    Path directory = Paths.get(uploadDirectory, holderId, type);

	    if (!Files.exists(directory)) {
	        throw new IOException("Directory does not exist: " + directory);
	    }

	    List<Map<String, Object>> imagesList = new ArrayList<>();

	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
	        for (Path imagePath : stream) {
	            if (Files.isRegularFile(imagePath) && isImageFile(imagePath)) {
	                Map<String, Object> imageData = new HashMap<>();
	                byte[] imageBytes = Files.readAllBytes(imagePath);
	                String filename = imagePath.getFileName().toString();
	                
	                imageData.put("filename", filename);
	                imageData.put("imageBytes", imageBytes);
	                
	                imagesList.add(imageData);
	            }
	        }
	    }

	    return imagesList;
	}

	private boolean isImageFile(Path filePath) {
		String fileName = filePath.getFileName().toString().toLowerCase();
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
				|| fileName.endsWith(".gif");
	}

}
