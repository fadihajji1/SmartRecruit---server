package com.smartrecruit.recruitment.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartrecruit.recruitment.dto.ApplicationResponse;
import com.smartrecruit.recruitment.dto.BusinessResponse;
import com.smartrecruit.recruitment.dto.CreateApplicationRequest;
import com.smartrecruit.recruitment.dto.CreateJobRequest;
import com.smartrecruit.recruitment.dto.JobResponse;
import com.smartrecruit.recruitment.dto.UpdateApplicationStatusRequest;
import com.smartrecruit.recruitment.dto.UpdateBusinessRequest;
import com.smartrecruit.recruitment.dto.UpdateJobRequest;
import com.smartrecruit.recruitment.model.Application;
import com.smartrecruit.recruitment.model.ApplicationStatus;
import com.smartrecruit.recruitment.model.Business;
import com.smartrecruit.recruitment.model.BusinessMember;
import com.smartrecruit.recruitment.model.BusinessStatus;
import com.smartrecruit.recruitment.model.JobOffer;
import com.smartrecruit.recruitment.model.JobStatus;
import com.smartrecruit.recruitment.repository.ApplicationRepository;
import com.smartrecruit.recruitment.repository.BusinessMemberRepository;
import com.smartrecruit.recruitment.repository.BusinessRepository;
import com.smartrecruit.recruitment.repository.JobOfferRepository;

@Service
@Transactional(readOnly = true)
public class RecruitmentService {
	private final BusinessRepository businessRepository;
	private final BusinessMemberRepository businessMemberRepository;
	private final JobOfferRepository jobOfferRepository;
	private final ApplicationRepository applicationRepository;

	public RecruitmentService(
			BusinessRepository businessRepository,
			BusinessMemberRepository businessMemberRepository,
			JobOfferRepository jobOfferRepository,
			ApplicationRepository applicationRepository) {
		this.businessRepository = businessRepository;
		this.businessMemberRepository = businessMemberRepository;
		this.jobOfferRepository = jobOfferRepository;
		this.applicationRepository = applicationRepository;
	}

	public Page<JobResponse> searchPublicJobs(
			String query, String location, String contractType, Pageable pageable) {
		return jobOfferRepository.searchPublicJobs(
				JobStatus.OPEN, blankToNull(query), blankToNull(location), blankToNull(contractType), pageable)
				.map(this::toJobResponse);
	}

	public JobResponse getPublicJob(UUID jobId) {
		return jobOfferRepository.findByIdAndStatus(jobId, JobStatus.OPEN)
				.map(this::toJobResponse)
				.orElseThrow(() -> notFound("Open job not found"));
	}

	public List<BusinessResponse> getRecruiterBusinesses(UUID recruiterId) {
		return businessMemberRepository.findAllByUserId(recruiterId).stream()
				.map(BusinessMember::getBusiness)
				.distinct()
				.map(this::toBusinessResponse)
				.toList();
	}

	public BusinessResponse getRecruiterBusiness(UUID businessId, UUID recruiterId) {
		return toBusinessResponse(requireBusinessMembership(businessId, recruiterId));
	}

	@Transactional
	public BusinessResponse updateRecruiterBusiness(
			UUID businessId, UUID recruiterId, UpdateBusinessRequest request) {
		Business business = requireBusinessMembership(businessId, recruiterId);
		business.update(request.name(), request.description(), request.address());
		return toBusinessResponse(businessRepository.save(business));
	}

	public Page<JobResponse> getRecruiterJobs(UUID recruiterId, Pageable pageable) {
		List<UUID> businessIds = businessIdsFor(recruiterId);
		if (businessIds.isEmpty()) return Page.empty(pageable);
		return jobOfferRepository.findAllByBusiness_IdIn(businessIds, pageable).map(this::toJobResponse);
	}

	@Transactional
	public JobResponse createRecruiterJob(UUID recruiterId, CreateJobRequest request) {
		Business business = requireBusinessMembership(request.businessId(), recruiterId);
		if (business.getStatus() != BusinessStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive businesses cannot publish jobs");
		}
		JobOffer job = new JobOffer(business, recruiterId, request.title(), request.description(),
				request.location(), request.contractType());
		return toJobResponse(jobOfferRepository.save(job));
	}

	public JobResponse getRecruiterJob(UUID jobId, UUID recruiterId) {
		return toJobResponse(requireRecruiterJob(jobId, recruiterId));
	}

	@Transactional
	public JobResponse updateRecruiterJob(UUID jobId, UUID recruiterId, UpdateJobRequest request) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Deleted jobs cannot be updated");
		}
		job.update(request.title(), request.description(), request.location(), request.contractType());
		return toJobResponse(jobOfferRepository.save(job));
	}

	@Transactional
	public void closeRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Deleted jobs cannot be closed");
		}
		job.close();
		jobOfferRepository.save(job);
	}

	@Transactional
	public void reopenRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Deleted jobs cannot be reopened");
		}
		job.reopen();
		jobOfferRepository.save(job);
	}

	@Transactional
	public void deleteRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) return;
		job.markDeleted();
		jobOfferRepository.save(job);
		applicationRepository.findAllByJob_Id(jobId).forEach(Application::markUnavailable);
	}

	@Transactional
	public ApplicationResponse createApplication(UUID candidateId, CreateApplicationRequest request) {
		JobOffer job = jobOfferRepository.findByIdAndStatus(request.jobId(), JobStatus.OPEN)
				.orElseThrow(() -> notFound("Open job not found"));
		if (applicationRepository.existsByJob_IdAndCandidateId(job.getId(), candidateId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Candidate has already applied to this job");
		}
		Application application = new Application(job, candidateId, request.candidateCvId());
		return toApplicationResponse(applicationRepository.save(application));
	}

	public Page<ApplicationResponse> getCandidateApplications(UUID candidateId, Pageable pageable) {
		return applicationRepository.findAllByCandidateId(candidateId, pageable)
				.map(this::toApplicationResponse);
	}

	public ApplicationResponse getCandidateApplication(UUID applicationId, UUID candidateId) {
		return applicationRepository.findByIdAndCandidateId(applicationId, candidateId)
				.map(this::toApplicationResponse)
				.orElseThrow(() -> notFound("Application not found"));
	}

	public Page<ApplicationResponse> getRecruiterApplications(UUID recruiterId, Pageable pageable) {
		List<UUID> businessIds = businessIdsFor(recruiterId);
		if (businessIds.isEmpty()) return Page.empty(pageable);
		return applicationRepository.findAllByJob_Business_IdIn(businessIds, pageable)
				.map(this::toApplicationResponse);
	}

	public ApplicationResponse getRecruiterApplication(UUID applicationId, UUID recruiterId) {
		List<UUID> businessIds = businessIdsFor(recruiterId);
		if (businessIds.isEmpty()) throw notFound("Application not found");
		return applicationRepository.findByIdAndJob_Business_IdIn(applicationId, businessIds)
				.map(this::toApplicationResponse)
				.orElseThrow(() -> notFound("Application not found"));
	}

	@Transactional
	public ApplicationResponse updateApplicationStatus(
			UUID applicationId, UUID recruiterId, UpdateApplicationStatusRequest request) {
		List<UUID> businessIds = businessIdsFor(recruiterId);
		if (businessIds.isEmpty()) throw notFound("Application not found");
		Application application = applicationRepository.findByIdAndJob_Business_IdIn(applicationId, businessIds)
				.orElseThrow(() -> notFound("Application not found"));
		if (application.getStatus() == ApplicationStatus.UNAVAILABLE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Unavailable applications cannot be updated");
		}
		ApplicationStatus status;
		try {
			status = ApplicationStatus.valueOf(request.status().trim().toUpperCase());
		} catch (RuntimeException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Status must be PENDING, HIRED, or REJECTED");
		}
		if (status == ApplicationStatus.UNAVAILABLE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UNAVAILABLE is set by job deletion");
		}
		application.changeStatus(status);
		return toApplicationResponse(applicationRepository.save(application));
	}

	private Business requireBusinessMembership(UUID businessId, UUID recruiterId) {
		return businessMemberRepository.findByBusiness_IdAndUserId(businessId, recruiterId)
				.map(BusinessMember::getBusiness)
				.orElseThrow(() -> notFound("Business not found"));
	}

	private JobOffer requireRecruiterJob(UUID jobId, UUID recruiterId) {
		List<UUID> businessIds = businessIdsFor(recruiterId);
		if (businessIds.isEmpty()) throw notFound("Job not found");
		return jobOfferRepository.findByIdAndBusiness_IdIn(jobId, businessIds)
				.orElseThrow(() -> notFound("Job not found"));
	}

	private List<UUID> businessIdsFor(UUID recruiterId) {
		return businessMemberRepository.findAllByUserId(recruiterId).stream()
				.map(member -> member.getBusiness().getId())
				.distinct()
				.toList();
	}

	private JobResponse toJobResponse(JobOffer job) {
		return new JobResponse(job.getId(), job.getBusiness().getId(), job.getTitle(), job.getDescription(),
				job.getLocation(), job.getContractType(), job.getStatus().name(), job.getCreatedAt(), job.getUpdatedAt());
	}

	private BusinessResponse toBusinessResponse(Business business) {
		return new BusinessResponse(business.getId(), business.getName(), business.getDescription(),
				business.getAddress(), business.getStatus().name(), business.getCreatedAt(), business.getUpdatedAt());
	}

	private ApplicationResponse toApplicationResponse(Application application) {
		return new ApplicationResponse(application.getId(), application.getJob().getId(),
				application.getCandidateId(), application.getCandidateCvId(), application.getStatus().name(),
				application.getCreatedAt(), application.getUpdatedAt());
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private ResponseStatusException notFound(String message) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
	}
}
