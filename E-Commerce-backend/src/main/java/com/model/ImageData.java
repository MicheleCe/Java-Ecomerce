package com.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_image")
public class ImageData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String holderId;

    private String type;

    @Column(name = "url", nullable = false)
    private String url;

	public String getName() {
		return holderId;
	}

	public void setName(String name) {
		this.holderId = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


}