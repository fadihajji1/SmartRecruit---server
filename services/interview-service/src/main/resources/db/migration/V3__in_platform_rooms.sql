-- Interviews now take place in SmartRecruit's own video room; an external meeting link becomes optional.
ALTER TABLE interviews ALTER COLUMN external_meeting_url DROP NOT NULL;
