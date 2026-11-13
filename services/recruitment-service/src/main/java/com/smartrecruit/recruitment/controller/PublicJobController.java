package com.smartrecruit.recruitment.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartrecruit.recruitment.dto.JobResponse;
import com.smartrecruit.recruitment.service.RecruitmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Public jobs", description = "Public job search and details")
public class PublicJobController {
	private final RecruitmentService recruitmentService;

	public PublicJobController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	@GetMapping
	@Operation(summary = "Search published jobs")
	public Page<JobResponse> searchJobs(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) String location,
			@RequestParam(required = false) String contractType,
			@ParameterObject Pageable pageable) {
		return recruitmentService.searchPublicJobs(query, location, contractType, pageable);
	}

	@GetMapping("/{jobId}")
	@Operation(summary = "Get a published job")
	public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
		return ResponseEntity.ok(recruitmentService.getPublicJob(jobId));
	}
}
