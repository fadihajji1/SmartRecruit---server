CREATE TABLE analytics_metrics(id UUID PRIMARY KEY,recruiter_id UUID NOT NULL,metric_key VARCHAR(100) NOT NULL,metric_value BIGINT NOT NULL,measured_at TIMESTAMPTZ);
