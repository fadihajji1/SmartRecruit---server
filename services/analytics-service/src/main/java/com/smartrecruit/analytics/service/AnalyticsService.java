package com.smartrecruit.analytics.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartrecruit.analytics.domain.AnalyticsEvent;
import com.smartrecruit.analytics.dto.AnalyticsOverviewResponse;
import com.smartrecruit.analytics.dto.ApplicationMetricResponse;
import com.smartrecruit.analytics.dto.OfferMetricResponse;
import com.smartrecruit.analytics.repo.AnalyticsEventRepository;

@Service
public class AnalyticsService {
    private final AnalyticsEventRepository events;
    private final PdfReportWriter pdf;
    public AnalyticsService(AnalyticsEventRepository events,PdfReportWriter pdf){this.events=events;this.pdf=pdf;}

    @Transactional
    public void ingest(UUID eventId,String eventType,UUID aggregateId,JsonNode payload,Instant occurredAt) {
        if(eventId==null||eventType==null||eventType.isBlank()||events.existsByEventId(eventId)) return;
        JsonNode data=payload==null?com.fasterxml.jackson.databind.node.MissingNode.getInstance():payload;
        String normalized=eventType.replaceAll("([a-z])([A-Z])","$1_$2").toUpperCase(Locale.ROOT);
        UUID jobId=uuid(data,"jobId","job_id");
        UUID applicationId=uuid(data,"applicationId","application_id");
        UUID interviewId=uuid(data,"interviewId","interview_id");
        if(jobId==null&&normalized.contains("JOB")) jobId=uuid(data,"id");
        if(applicationId==null&&normalized.contains("APPLICATION")) applicationId=uuid(data,"id");
        if(interviewId==null&&normalized.contains("INTERVIEW")) interviewId=uuid(data,"id");
        if(jobId==null&&normalized.contains("JOB")) jobId=aggregateId;
        if(applicationId==null&&normalized.contains("APPLICATION")) applicationId=aggregateId;
        if(interviewId==null&&normalized.contains("INTERVIEW")) interviewId=aggregateId;
        UUID recruiterId=uuid(data,"recruiterId","recruiter_id","createdByUserId","ownerUserId");
        if(recruiterId==null&&jobId!=null) recruiterId=events.findFirstByJobIdOrderByOccurredAtDesc(jobId).map(AnalyticsEvent::getRecruiterId).orElse(null);
        UUID businessId=uuid(data,"businessId","companyId","business_id");
        String status=text(data,"status","newStatus","applicationStatus");
        if(status==null&&normalized.contains("APPLICATION_SUBMITTED")) status="PENDING";
        if(status==null&&normalized.contains("JOB_PUBLISH")) status="PUBLISHED";
        if(status==null&&normalized.contains("JOB_CLOSE")) status="CLOSED";
        String title=text(data,"jobTitle","title","name");
        events.save(new AnalyticsEvent(eventId,eventType,aggregateId,businessId,recruiterId,
            uuid(data,"candidateId","candidate_id"),jobId,applicationId,interviewId,status,title,
            occurredAt==null?Instant.now():occurredAt));
    }

    @Transactional(readOnly=true)
    public AnalyticsOverviewResponse overview(UUID recruiterId) {
        List<AnalyticsEvent> values=forRecruiter(recruiterId);
        Map<UUID,AnalyticsEvent> jobs=latestByType(values,"JOB",AnalyticsEvent::getJobId);
        Map<UUID,AnalyticsEvent> applications=latestByType(values,"APPLICATION",AnalyticsEvent::getApplicationId);
        long published=jobs.values().stream().filter(e->isStatus(e,"PUBLISHED","OPEN","ACTIVE")).count();
        long closed=jobs.values().stream().filter(e->isStatus(e,"CLOSED","EXPIRED","DELETED")).count();
        long interviews=latestByType(values,"INTERVIEW",AnalyticsEvent::getInterviewId).size();
        Map<String,Long> byStatus=new LinkedHashMap<>();
        applications.values().forEach(e->byStatus.merge(status(e,"PENDING"),1L,Long::sum));
        long hires=applications.values().stream().filter(e->"HIRED".equalsIgnoreCase(status(e,""))).count();
        return new AnalyticsOverviewResponse(published,closed,applications.size(),interviews,hires,Map.copyOf(byStatus),Instant.now());
    }

    @Transactional(readOnly=true)
    public List<OfferMetricResponse> offers(UUID recruiterId) {
        List<AnalyticsEvent> values=forRecruiter(recruiterId);
        Map<UUID,AnalyticsEvent> latestJobs=latestByType(values,"JOB",AnalyticsEvent::getJobId);
        Map<UUID,AnalyticsEvent> latestApplications=latestByType(values,"APPLICATION",AnalyticsEvent::getApplicationId);
        Map<UUID,Long> applicationsByJob=new LinkedHashMap<>();
        latestApplications.values().forEach(e->{if(e.getJobId()!=null)applicationsByJob.merge(e.getJobId(),1L,Long::sum);});
        return latestJobs.values().stream().map(e->new OfferMetricResponse(e.getJobId(),safeTitle(e),status(e,"UNKNOWN"),
            applicationsByJob.getOrDefault(e.getJobId(),0L),e.getOccurredAt())).sorted(Comparator.comparing(OfferMetricResponse::lastActivityAt).reversed()).toList();
    }

    @Transactional(readOnly=true)
    public List<ApplicationMetricResponse> applications(UUID recruiterId) {
        Map<String,List<AnalyticsEvent>> grouped=new LinkedHashMap<>();
        latestByType(forRecruiter(recruiterId),"APPLICATION",AnalyticsEvent::getApplicationId).values().forEach(e->grouped.computeIfAbsent(status(e,"PENDING"),ignored->new ArrayList<>()).add(e));
        return grouped.entrySet().stream().map(e->new ApplicationMetricResponse(e.getKey(),e.getValue().size(),
            e.getValue().stream().map(AnalyticsEvent::getOccurredAt).max(Comparator.naturalOrder()).orElse(Instant.EPOCH)))
            .sorted(Comparator.comparing(ApplicationMetricResponse::status)).toList();
    }

    @Transactional(readOnly=true)
    public byte[] exportPdf(UUID recruiterId) {
        AnalyticsOverviewResponse overview=overview(recruiterId);
        List<String> lines=new ArrayList<>(); lines.add("SmartRecruit recruitment report"); lines.add("Generated: "+overview.generatedAt());
        lines.add("Published jobs: "+overview.publishedJobs()); lines.add("Closed jobs: "+overview.closedJobs());
        lines.add("Applications: "+overview.applications()); lines.add("Interviews: "+overview.interviews()); lines.add("Hires: "+overview.hires());
        lines.add("Application status summary:"); overview.applicationsByStatus().forEach((key,value)->lines.add("  "+key+": "+value));
        lines.add("Offers:"); offers(recruiterId).stream().limit(18).forEach(item->lines.add("  "+safeTitle(item.title())+" | "+item.status()+" | applications: "+item.applications()));
        return pdf.create(lines);
    }

    private List<AnalyticsEvent> forRecruiter(UUID recruiterId) { return events.findAllByRecruiterIdOrderByOccurredAtDesc(recruiterId); }
    private static Map<UUID,AnalyticsEvent> latestByType(List<AnalyticsEvent> values,String type,java.util.function.Function<AnalyticsEvent,UUID> key) {
        Map<UUID,AnalyticsEvent> result=new LinkedHashMap<>(); for(AnalyticsEvent event:values){
            if(!event.getEventType().toUpperCase(Locale.ROOT).contains(type)) continue;
            UUID id=key.apply(event);if(id!=null)result.putIfAbsent(id,event);
        } return result;
    }
    private static boolean isStatus(AnalyticsEvent event,String... statuses){for(String status:statuses)if(status.equalsIgnoreCase(status(event,"")))return true;return false;}
    private static String status(AnalyticsEvent event,String fallback){return event.getStatus()==null||event.getStatus().isBlank()?fallback:event.getStatus().toUpperCase(Locale.ROOT);}
    private static String safeTitle(AnalyticsEvent event){return safeTitle(event.getTitle());}
    private static String safeTitle(String title){return title==null||title.isBlank()?"Untitled job":title.replaceAll("[\\r\\n]"," ");}
    private static String text(JsonNode node,String... names){for(String name:names)if(node.hasNonNull(name)&&!node.get(name).asText().isBlank())return node.get(name).asText();return null;}
    private static UUID uuid(JsonNode node,String... names){String value=text(node,names);try{return value==null?null:UUID.fromString(value);}catch(IllegalArgumentException ex){return null;}}
}
