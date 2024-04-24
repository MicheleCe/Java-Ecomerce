package com.api.controller.image;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.model.LocalUser;

import com.service.ImageDataService;

import com.service.UserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
@RequestMapping("/image")
public class ImageController {

    private final ImageDataService imageDataService;


    public ImageController(ImageDataService imageDataService, UserService userService) {
        this.imageDataService = imageDataService;
    }

    @PostMapping("/{holderId}/{imageTpe}")
    public ResponseEntity<String> uploadImage(@AuthenticationPrincipal LocalUser user, @PathVariable UUID holderId, @PathVariable String imageTpe,
                                                     @RequestParam("file") List<MultipartFile> file) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            String imageUrl = imageDataService.saveImagesToStorage(imageTpe, holderId, file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    /*
    @GetMapping("/{holderId}/{imageType}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String holderId, @PathVariable String imageType) {
        try {
            // Assuming you have a method in ImageDataService to retrieve the image byte array by productId
            byte[] imageBytes = imageDataService.getProductImage(holderId, imageType);
            // You may also need to determine the appropriate content type based on the image format
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG); // Assuming JPEG format for simplicity
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    */
    
//    @GetMapping("/{holderId}/{imageType}")
//    public ResponseEntity<List<byte[]>> getImage(@PathVariable String holderId, @PathVariable String imageType) {
//        try {
//            // Retrieve the list of image byte arrays from the service
//            List<byte[]> imageBytesList = imageDataService.getImages(holderId, imageType);
//
//            // Return the list of byte arrays as ResponseEntity
//            return new ResponseEntity<>(imageBytesList, HttpStatus.OK);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    

    @GetMapping("/{holderId}/{imageType}")
    public ResponseEntity<FileSystemResource> getProductImages(@PathVariable String holderId, @PathVariable String imageType) {
        try {

            String zipFilePath = imageDataService.getZipImages(holderId, imageType );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "images.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(zipFilePath));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @CrossOrigin
    @DeleteMapping("/{holderId}/{imageName}")
    public ResponseEntity<String> deleteImageFromZip(
            @PathVariable UUID holderId,
            @PathVariable String imageName) {
        try {
        	imageDataService.deleteImageFromZip("thumbnail", holderId, imageName);
        	imageDataService.deleteImageFromZip("gallery", holderId, imageName);
            return ResponseEntity.ok("Image '" + imageName + "' deleted successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete image '" + imageName + "'.");
        }
    }
    
    
    
//    @DeleteMapping("/{imageType}/{imageName}")
//    public String deleteImage(@PathVariable String imageType, @PathVariable String imageName) {
//        try {
//            String result = imageDataService.deleteImageFromZip(imageType, imageName);
//            if (result.equals("Success")) {
//                return "Image deleted successfully";
//            } else {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
//            }
//        } catch (IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image", e);
//        }
//    }
    

    
//    @GetMapping("/{holderId}")
//    public ResponseEntity<byte[]> getProductThumbnail(@PathVariable String holderId) {
//        try {
//            List<Map<String, Object>> imageBytesList = imageDataService.getImages(holderId, "thumbnail");
//
//            if (imageBytesList.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", "images.zip"); 
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
//                Map<String, Object> imageData = imageBytesList.get(0); 
//                byte[] imageBytes = (byte[]) imageData.get("imageBytes");
//                String imageName = (String) imageData.get("filename");
//
//                ZipEntry zipEntry = new ZipEntry(imageName);
//                zipOutputStream.putNextEntry(zipEntry);
//                zipOutputStream.write(imageBytes);
//                zipOutputStream.closeEntry();
//            }
//            byte[] zipBytes = outputStream.toByteArray();
//
//            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    
}