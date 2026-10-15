package com.smartrecruit.identity.storage;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartrecruit.identity.config.MinioProperties;
import com.smartrecruit.identity.exception.StorageException;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;

@Service
public class MinioEmploymentProofStorage implements EmploymentProofStorage {
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

	private final MinioClient minio;
	private final MinioProperties properties;

	public MinioEmploymentProofStorage(MinioClient minio, MinioProperties properties) {
		this.minio = minio;
		this.properties = properties;
	}

	@Override
	public String store(MultipartFile file) {
		String contentType = file.getContentType();
		if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE
				|| !("application/pdf".equals(contentType) || "image/jpeg".equals(contentType)
						|| "image/png".equals(contentType))) {
			throw new IllegalArgumentException("Employment proof must be a PDF, JPEG, or PNG file up to 10 MB");
		}
		validateFileSignature(file, contentType);

		String extension = switch (contentType) {
			case "application/pdf" -> ".pdf";
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			default -> throw new IllegalArgumentException("Unsupported employment proof file type");
		};
		String objectKey = "employment-proof/" + UUID.randomUUID() + extension;
		try {
			if (!minio.bucketExists(BucketExistsArgs.builder().bucket(properties.bucket()).build())) {
				try {
					minio.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
				} catch (ErrorResponseException exception) {
					String code = exception.errorResponse().code();
					if (!"BucketAlreadyOwnedByYou".equals(code) && !"BucketAlreadyExists".equals(code)) {
						throw exception;
					}
				}
			}
			minio.putObject(PutObjectArgs.builder()
					.bucket(properties.bucket())
					.object(objectKey)
					.contentType(contentType)
					.stream(file.getInputStream(), file.getSize(), -1)
					.build());
			return objectKey;
		} catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException exception) {
			throw new StorageException("Unable to store employment proof", exception);
		}
	}

	private static void validateFileSignature(MultipartFile file, String contentType) {
		try (var input = file.getInputStream()) {
			byte[] signature = input.readNBytes(8);
			boolean valid = switch (contentType) {
				case "application/pdf" -> startsWith(signature, new byte[] { '%', 'P', 'D', 'F', '-' });
				case "image/jpeg" -> startsWith(signature, new byte[] { (byte) 0xff, (byte) 0xd8, (byte) 0xff });
				case "image/png" -> Arrays.equals(signature,
						new byte[] { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 });
				default -> false;
			};
			if (!valid) {
				throw new IllegalArgumentException("Employment proof content does not match its file type");
			}
		} catch (IOException exception) {
			throw new StorageException("Unable to read employment proof", exception);
		}
	}

	private static boolean startsWith(byte[] value, byte[] prefix) {
		if (value.length < prefix.length) {
			return false;
		}
		for (int index = 0; index < prefix.length; index++) {
			if (value[index] != prefix[index]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void delete(String objectKey) {
		try {
			minio.removeObject(RemoveObjectArgs.builder()
					.bucket(properties.bucket())
					.object(objectKey)
					.build());
		} catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException exception) {
			throw new StorageException("Unable to remove employment proof", exception);
		}
	}
}
