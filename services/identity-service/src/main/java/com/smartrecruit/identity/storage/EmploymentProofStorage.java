package com.smartrecruit.identity.storage;

import org.springframework.web.multipart.MultipartFile;

public interface EmploymentProofStorage {
	String store(MultipartFile file);

	StoredEmploymentProof retrieve(String objectKey);

	void delete(String objectKey);

	record StoredEmploymentProof(byte[] content, String contentType, String fileName) {
	}
}
