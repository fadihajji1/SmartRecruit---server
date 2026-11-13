package com.smartrecruit.identity.storage;

import org.springframework.web.multipart.MultipartFile;

public interface EmploymentProofStorage {
	String store(MultipartFile file);

	void delete(String objectKey);

	/** Short-lived download link for an administrator. */
	String temporaryUrl(String objectKey);
}
