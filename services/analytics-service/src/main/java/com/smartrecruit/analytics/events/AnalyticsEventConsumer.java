package com.smartrecruit.analytics.events;

import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.analytics.service.AnalyticsService;

@Component
public class AnalyticsEventConsumer {
    private final ObjectMapper mapper;private final AnalyticsService service;
    public AnalyticsEventConsumer(ObjectMapper mapper,AnalyticsService service){this.mapper=mapper;this.service=service;}
    @KafkaListener(topics="${analytics.events.topic}")
    public void consume(String raw)throws Exception{
        JsonNode root=mapper.readTree(raw);String eventId=text(root,"eventId","id");String type=text(root,"eventType","type","name");
        UUID id=uuid(eventId);UUID aggregateId=uuid(text(root,"aggregateId","aggregate_id"));Instant occurred=null;
        String timestamp=text(root,"occurredAt","timestamp");if(timestamp!=null)try{occurred=Instant.parse(timestamp);}catch(Exception ignored){}
        JsonNode payload=root.path("payload");if(payload.has("job")&&payload.get("job").isObject())payload=payload.get("job");
        service.ingest(id,type,aggregateId,payload,occurred);
    }
    private static String text(JsonNode node,String...names){for(String name:names)if(node.hasNonNull(name)&&!node.get(name).asText().isBlank())return node.get(name).asText();return null;}
    private static UUID uuid(String v){try{return v==null?null:UUID.fromString(v);}catch(IllegalArgumentException ex){return null;}}
}
