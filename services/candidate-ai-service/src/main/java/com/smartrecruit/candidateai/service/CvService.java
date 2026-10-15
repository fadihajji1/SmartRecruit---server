package com.smartrecruit.candidateai.service;

import com.smartrecruit.candidateai.domain.CandidateCv;
import com.smartrecruit.candidateai.repo.CandidateCvRepository;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service public class CvService {
 private final CandidateCvRepository repo;
 public CvService(CandidateCvRepository repo){
this.repo=repo;
}
 public List<CandidateCv> list(UUID owner){
return repo.findByCandidateIdAndDeletedAtIsNullOrderByCreatedAtDesc(owner);
}
 @Transactional public CandidateCv upload(UUID owner,MultipartFile file){
if(file==null||file.isEmpty()||file.getSize()>10*1024*1024)throw new IllegalArgumentException("CV required, maximum 10 MB");
var cv=new CandidateCv();
cv.candidateId=owner;
cv.originalFilename=file.getOriginalFilename();
cv.objectKey="cvs/"+owner+"/"+UUID.randomUUID();
cv.contentType=file.getContentType();
cv.sizeBytes=file.getSize();
return repo.save(cv);
}
 @Transactional public void delete(UUID id,UUID owner){
var cv=repo.findByIdAndCandidateIdAndDeletedAtIsNull(id,owner).orElseThrow();
cv.deletedAt=Instant.now();
}
 }

