package com.smartrecruit.recruitment.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartrecruit.recruitment.dto.AdminBusinessRequest;
import com.smartrecruit.recruitment.dto.ApplicationResponse;
import com.smartrecruit.recruitment.dto.BusinessResponse;
import com.smartrecruit.recruitment.dto.CreateApplicationRequest;
import com.smartrecruit.recruitment.dto.CreateBusinessRequest;
import com.smartrecruit.recruitment.dto.CreateJobRequest;
import com.smartrecruit.recruitment.dto.JobResponse;
import com.smartrecruit.recruitment.dto.UpdateApplicationStatusRequest;
import com.smartrecruit.recruitment.dto.UpdateBusinessRequest;
import com.smartrecruit.recruitment.dto.UpdateJobRequest;
import com.smartrecruit.recruitment.events.DomainEventPublisher;
import com.smartrecruit.recruitment.model.Application;
import com.smartrecruit.recruitment.model.ApplicationStatus;
import com.smartrecruit.recruitment.model.Business;
import com.smartrecruit.recruitment.model.BusinessMember;
import com.smartrecruit.recruitment.model.BusinessMemberRole;
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
	private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

	private final BusinessRepository businessRepository;
	private final BusinessMemberRepository businessMemberRepository;
	private final JobOfferRepository jobOfferRepository;
	private final ApplicationRepository applicationRepository;
	private final DomainEventPublisher events;

	public RecruitmentService(
			BusinessRepository businessRepository,
			BusinessMemberRepository businessMemberRepository,
			JobOfferRepository jobOfferRepository,
			ApplicationRepository applicationRepository,
			DomainEventPublisher events) {
		this.businessRepository = businessRepository;
		this.businessMemberRepository = businessMemberRepository;
		this.jobOfferRepository = jobOfferRepository;
		this.applicationRepository = applicationRepository;
		this.events = events;
	}

	// ------------------------------------------------------------------ public jobs

	public Page<JobResponse> searchPublicJobs(
			String query, String location, String contractType, Pageable pageable) {
		return jobOfferRepository.searchPublicJobs(
				JobStatus.OPEN, blankToNull(query), blankToNull(location), blankToNull(contractType),
				withDefaultSort(pageable))
				.map(job -> toJobResponse(job, null));
	}

	public JobResponse getPublicJob(UUID jobId) {
		return jobOfferRepository.findByIdAndStatus(jobId, JobStatus.OPEN)
				.map(job -> toJobResponse(job, null))
				.orElseThrow(() -> notFound("Open job not found"));
	}

	// ------------------------------------------------------------------ recruiter businesses

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
	public BusinessResponse createRecruiterBusiness(UUID recruiterId, CreateBusinessRequest request) {
		Business business = businessRepository.save(new Business(request.name().trim(), request.description(),
				request.address(), request.website(), request.industry(), request.city(), request.country()));
		businessMemberRepository.save(new BusinessMember(business, recruiterId, BusinessMemberRole.OWNER));
		return toBusinessResponse(business, recruiterId);
	}

	@Transactional
	public BusinessResponse updateRecruiterBusiness(
			UUID businessId, UUID recruiterId, UpdateBusinessRequest request) {
		Business business = requireBusinessMembership(businessId, recruiterId);
		business.update(request.name(), request.description(), request.address());
		business.updateProfile(request.website(), request.industry(), request.city(), request.country());
		return toBusinessResponse(businessRepository.save(business));
	}

	// ------------------------------------------------------------------ recruiter jobs

	public Page<JobResponse> getRecruiterJobs(UUID recruiterId, Pageable pageable) {
		List<UUID> businessIds = businessIdsFor(recruiterId);
		if (businessIds.isEmpty()) return Page.empty(pageable);
		return jobOfferRepository.findAllByBusiness_IdIn(businessIds, withDefaultSort(pageable))
				.map(job -> toJobResponse(job, countApplications(job)));
	}

	@Transactional
	public JobResponse createRecruiterJob(UUID recruiterId, CreateJobRequest request) {
		if (request.businessId() == null || isBlank(request.title()) || isBlank(request.description())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "businessId, title and description are required");
		}
		Business business = requireBusinessMembership(request.businessId(), recruiterId);
		if (business.getStatus() != BusinessStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive businesses cannot publish jobs");
		}
		JobOffer job = jobOfferRepository.save(new JobOffer(business, recruiterId, request.title().trim(),
				request.description(), request.location(), request.contractType()));
		publishJobEvent("JOB_PUBLISHED", job);
		return toJobResponse(job, 0L);
	}

	public JobResponse getRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		return toJobResponse(job, countApplications(job));
	}

	@Transactional
	public JobResponse updateRecruiterJob(UUID jobId, UUID recruiterId, UpdateJobRequest request) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Deleted jobs cannot be updated");
		}
		job.update(request.title(), request.description(), request.location(), request.contractType());
		job = jobOfferRepository.save(job);
		publishJobEvent("JOB_UPDATED", job);
		return toJobResponse(job, countApplications(job));
	}

	@Transactional
	public JobResponse closeRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Deleted jobs cannot be closed");
		}
		return close(job);
	}

	@Transactional
	public JobResponse reopenRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Deleted jobs cannot be reopened");
		}
		if (job.getBusiness().getStatus() != BusinessStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Jobs of inactive businesses cannot be reopened");
		}
		if (job.getStatus() == JobStatus.OPEN) return toJobResponse(job, countApplications(job));
		job.reopen();
		job = jobOfferRepository.save(job);
		publishJobEvent("JOB_PUBLISHED", job);
		return toJobResponse(job, countApplications(job));
	}

	@Transactional
	public void deleteRecruiterJob(UUID jobId, UUID recruiterId) {
		JobOffer job = requireRecruiterJob(jobId, recruiterId);
		if (job.getStatus() == JobStatus.DELETED) return;
		job.markDeleted();
		jobOfferRepository.save(job);
		applicationRepository.findAllByJob_Id(jobId).forEach(Application::markUnavailable);
		publishJobEvent("JOB_DELETED", job);
	}

	// ------------------------------------------------------------------ applications

	@Transactional
	public ApplicationResponse createApplication(UUID candidateId, String candidateName, String candidateEmail,
			CreateApplicationRequest request) {
		if (request.jobId() == null || request.candidateCvId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobId and candidateCvId are required");
		}
		JobOffer job = jobOfferRepository.findByIdAndStatus(request.jobId(), JobStatus.OPEN)
				.orElseThrow(() -> notFound("Open job not found"));
		if (applicationRepository.existsByJob_IdAndCandidateId(job.getId(), candidateId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Candidate has already applied to this job");
		}
		Application application = applicationRepository.save(new Application(job, candidateId,
				request.candidateCvId(), blankToNull(request.message()), candidateName, candidateEmail));
		publishApplicationEvent("APPLICATION_SUBMITTED", application);
		return toApplicationResponse(application);
	}

	public Page<ApplicationResponse> getCandidateApplications(UUID candidateId, Pageable pageable) {
		return applicationRepository.findAllByCandidateId(candidateId, withDefaultSort(pageable))
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
		return applicationRepository.findAllByJob_Business_IdIn(businessIds, withDefaultSort(pageable))
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
					"Status must be PENDING, REVIEWING, INTERVIEW, HIRED or REJECTED");
		}
		if (status == ApplicationStatus.UNAVAILABLE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UNAVAILABLE is set by job deletion");
		}
		if (status == application.getStatus()) return toApplicationResponse(application);
		application.changeStatus(status);
		application = applicationRepository.save(application);
		publishApplicationEvent("APPLICATION_STATUS_CHANGED", application);
		return toApplicationResponse(application);
	}

	// ------------------------------------------------------------------ platform administrator

	public List<BusinessResponse> getAllBusinesses() {
		return businessRepository.findAll(NEWEST_FIRST).stream().map(this::toBusinessResponse).toList();
	}

	public BusinessResponse getBusinessForAdmin(UUID businessId) {
		return toBusinessResponse(requireBusiness(businessId));
	}

	@Transactional
	public BusinessResponse adminCreateBusiness(AdminBusinessRequest request) {
		if (request.ownerUserId() == null || isBlank(request.name())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerUserId and name are required");
		}
		Business business = new Business(request.name().trim(), request.description(), request.address(),
				request.website(), request.industry(), request.city(), request.country());
		if (request.status() != null) business.setStatus(parseBusinessStatus(request.status()));
		business = businessRepository.save(business);
		businessMemberRepository.save(new BusinessMember(business, request.ownerUserId(), BusinessMemberRole.OWNER));
		return toBusinessResponse(business, request.ownerUserId());
	}

	@Transactional
	public BusinessResponse adminUpdateBusiness(UUID businessId, AdminBusinessRequest request) {
		Business business = requireBusiness(businessId);
		business.update(request.name(), request.description(), request.address());
		business.updateProfile(request.website(), request.industry(), request.city(), request.country());
		if (request.status() != null) {
			BusinessStatus status = parseBusinessStatus(request.status());
			if (status == BusinessStatus.INACTIVE && business.getStatus() == BusinessStatus.ACTIVE) {
				closeOpenJobs(business);
			}
			business.setStatus(status);
		}
		return toBusinessResponse(businessRepository.save(business));
	}

	/** Deactivates a business and closes its open jobs; history is kept. */
	@Transactional
	public void adminDeactivateBusiness(UUID businessId) {
		Business business = requireBusiness(businessId);
		if (business.getStatus() == BusinessStatus.INACTIVE) return;
		closeOpenJobs(business);
		business.setStatus(BusinessStatus.INACTIVE);
		businessRepository.save(business);
	}

	public List<JobResponse> getAllJobs() {
		return jobOfferRepository.findAll(NEWEST_FIRST).stream()
				.map(job -> toJobResponse(job, countApplications(job)))
				.toList();
	}

	@Transactional
	public JobResponse adminCloseJob(UUID jobId) {
		JobOffer job = jobOfferRepository.findById(jobId).orElseThrow(() -> notFound("Job not found"));
		if (job.getStatus() != JobStatus.OPEN) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only open jobs can be closed");
		}
		return close(job);
	}

	public Map<String, Long> getAdminStats() {
		Map<String, Long> stats = new LinkedHashMap<>();
		for (JobStatus status : JobStatus.values()) {
			stats.put("jobs" + status.name(), jobOfferRepository.countByStatus(status));
		}
		stats.put("jobs", jobOfferRepository.count());
		stats.put("applications", applicationRepository.count());
		for (ApplicationStatus status : ApplicationStatus.values()) {
			stats.put("applications" + status.name(), applicationRepository.countByStatus(status));
		}
		stats.put("businesses", businessRepository.count());
		return stats;
	}

	// ------------------------------------------------------------------ helpers

	private JobResponse close(JobOffer job) {
		if (job.getStatus() == JobStatus.CLOSED) return toJobResponse(job, countApplications(job));
		job.close();
		job = jobOfferRepository.save(job);
		publishJobEvent("JOB_CLOSED", job);
		return toJobResponse(job, countApplications(job));
	}

	private void closeOpenJobs(Business business) {
		jobOfferRepository.findAllByBusiness_IdIn(List.of(business.getId()), Pageable.unpaged()).stream()
				.filter(job -> job.getStatus() == JobStatus.OPEN)
				.forEach(this::close);
	}

	private Business requireBusiness(UUID businessId) {
		return businessRepository.findById(businessId).orElseThrow(() -> notFound("Business not found"));
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

	private long countApplications(JobOffer job) {
		return applicationRepository.findAllByJob_Id(job.getId()).size();
	}

	private UUID ownerOf(Business business) {
		return businessMemberRepository.findFirstByBusiness_IdAndRole(business.getId(), BusinessMemberRole.OWNER)
				.map(BusinessMember::getUserId)
				.orElse(null);
	}

	private void publishJobEvent(String type, JobOffer job) {
		Business business = job.getBusiness();
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("jobId", str(job.getId()));
		payload.put("businessId", str(business.getId()));
		payload.put("businessName", business.getName());
		payload.put("recruiterId", str(job.getCreatedByUserId()));
		payload.put("title", job.getTitle());
		payload.put("description", job.getDescription());
		payload.put("location", job.getLocation());
		payload.put("contractType", job.getContractType());
		payload.put("status", job.getStatus().name());
		events.enqueue(type, job.getId(), payload);
	}

	private void publishApplicationEvent(String type, Application application) {
		JobOffer job = application.getJob();
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("applicationId", str(application.getId()));
		payload.put("jobId", str(job.getId()));
		payload.put("jobTitle", job.getTitle());
		payload.put("businessId", str(job.getBusiness().getId()));
		payload.put("candidateId", str(application.getCandidateId()));
		payload.put("candidateName", application.getCandidateName());
		payload.put("recruiterId", str(job.getCreatedByUserId()));
		payload.put("status", application.getStatus().name());
		if ("APPLICATION_STATUS_CHANGED".equals(type)) {
			payload.put("recipientUserId", str(application.getCandidateId()));
		} else {
			payload.put("recipientUserId", str(job.getCreatedByUserId()));
		}
		events.enqueue(type, application.getId(), payload);
	}

	private JobResponse toJobResponse(JobOffer job, Long applicationCount) {
		Business business = job.getBusiness();
		return new JobResponse(job.getId(), business.getId(), job.getCreatedByUserId(), job.getTitle(),
				job.getDescription(), job.getLocation(), job.getContractType(), job.getStatus().name(),
				job.getCreatedAt(), job.getUpdatedAt(), business.getName(), business.getIndustry(),
				business.getCity(), business.getCountry(), business.getWebsite(), business.getDescription(),
				applicationCount);
	}

	private BusinessResponse toBusinessResponse(Business business) {
		return toBusinessResponse(business, ownerOf(business));
	}

	private BusinessResponse toBusinessResponse(Business business, UUID ownerUserId) {
		return new BusinessResponse(business.getId(), ownerUserId, business.getName(), business.getDescription(),
				business.getWebsite(), business.getIndustry(), business.getAddress(), business.getCity(),
				business.getCountry(), business.getStatus().name(), business.getCreatedAt(), business.getUpdatedAt());
	}

	private ApplicationResponse toApplicationResponse(Application application) {
		JobOffer job = application.getJob();
		return new ApplicationResponse(application.getId(), job.getId(), application.getCandidateId(),
				application.getCandidateCvId(), application.getMessage(), job.getTitle(), job.getBusiness().getName(),
				application.getCandidateName(), application.getCandidateEmail(), application.getStatus().name(),
				application.getCreatedAt(), application.getUpdatedAt());
	}

	private BusinessStatus parseBusinessStatus(String value) {
		try {
			return BusinessStatus.valueOf(value.trim().toUpperCase());
		} catch (RuntimeException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be ACTIVE or INACTIVE");
		}
	}

	private static Pageable withDefaultSort(Pageable pageable) {
		if (pageable == null || pageable.isUnpaged()) return pageable;
		return pageable.getSort().isSorted() ? pageable
				: org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST);
	}

	private static String str(UUID id) {
		return id == null ? null : id.toString();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private ResponseStatusException notFound(String message) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
	}

}
