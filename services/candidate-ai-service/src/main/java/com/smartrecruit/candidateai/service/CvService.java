package com.smartrecruit.candidateai.service;

import com.smartrecruit.candidateai.domain.CandidateCv;
import com.smartrecruit.candidateai.repo.CandidateCvRepository;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

@Service public class CvService {
 private final CandidateCvRepository repo;
 private final MinioClient minio;
 @Value("${minio.bucket:smartrecruit-cvs}") private String bucket;
 public CvService(CandidateCvRepository repo,MinioClient minio){
this.repo=repo;this.minio=minio;
}
 public List<CandidateCv> list(UUID owner){
return repo.findByCandidateIdAndDeletedAtIsNullOrderByCreatedAtDesc(owner);
}
public CandidateCv get(UUID id,UUID owner) { return repo.findByIdAndCandidateIdAndDeletedAtIsNull(id,owner).orElseThrow(); }
 @Transactional public CandidateCv upload(UUID owner,MultipartFile file){
if(file==null||file.isEmpty()||file.getSize()>10*1024*1024)throw new IllegalArgumentException("CV required, maximum 10 MB");
String filename=org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename()==null?"cv":file.getOriginalFilename());
if(filename.contains("..")||filename.contains("/")||filename.contains("\\"))throw new IllegalArgumentException("Invalid filename");
String ext=filename.contains(".")?filename.substring(filename.lastIndexOf('.')+1).toLowerCase(Locale.ROOT):"";
if(!Set.of("pdf","doc","docx").contains(ext))throw new IllegalArgumentException("CV must be PDF, DOC or DOCX");
if(file.getContentType()!=null&&!Set.of("application/pdf","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/octet-stream").contains(file.getContentType()))throw new IllegalArgumentException("Unsupported CV content type");
try {
 byte[] header=file.getBytes();
 boolean valid=ext.equals("pdf")?startsWith(header,new byte[]{'%', 'P', 'D', 'F', '-'})
  :ext.equals("doc")?startsWith(header,new byte[]{(byte)0xD0,(byte)0xCF,0x11,(byte)0xE0,(byte)0xA1,(byte)0xB1,0x1A,(byte)0xE1})
  :startsWith(header,new byte[]{'P','K',3,4});
 if(!valid)throw new IllegalArgumentException("CV content does not match its file type");
} catch(java.io.IOException e) { throw new IllegalArgumentException("Could not read CV",e); }
var cv=new CandidateCv();
cv.candidateId=owner;
cv.originalFilename=filename;
cv.objectKey="cvs/"+owner+"/"+UUID.randomUUID();
cv.contentType=switch(ext) { case "pdf" -> "application/pdf"; case "doc" -> "application/msword"; default -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; };
cv.sizeBytes=file.getSize();
try {
 if(!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
 try(InputStream data=file.getInputStream()) {
  minio.putObject(PutObjectArgs.builder().bucket(bucket).object(cv.objectKey).stream(data,file.getSize(),-1)
   .contentType(cv.contentType).build());
 }
} catch(Exception e) { throw new IllegalStateException("Could not store CV",e); }
return repo.save(cv);
}
private boolean startsWith(byte[] value,byte[] prefix) { if(value.length<prefix.length)return false;for(int i=0;i<prefix.length;i++)if(value[i]!=prefix[i])return false;return true; }
public String downloadUrl(UUID id,UUID owner) {
var cv=repo.findByIdAndCandidateIdAndDeletedAtIsNull(id,owner).orElseThrow();
try { return minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucket).object(cv.objectKey).expiry(300).build()); }
catch(Exception e) { throw new IllegalStateException("Could not create CV download link",e); }
}
 @Transactional public void delete(UUID id,UUID owner){
var cv=repo.findByIdAndCandidateIdAndDeletedAtIsNull(id,owner).orElseThrow();
try { minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(cv.objectKey).build()); }
catch(Exception e) { throw new IllegalStateException("Could not delete CV file",e); }
cv.deletedAt=Instant.now();
}
 }

