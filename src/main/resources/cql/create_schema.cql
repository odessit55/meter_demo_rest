CREATE KEYSPACE if not exists metrics WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

use metrics;

CREATE TABLE if not exists metrics.daily_rollups (
    device_id text,
    metric_day timestamp,
    metric_avg decimal,
    metric_max decimal,
    metric_min decimal,
    metric_name text,
    PRIMARY KEY (device_id, metric_day)
) WITH CLUSTERING ORDER BY (metric_day DESC)
;

CREATE TABLE if not exists metrics.raw_metrics (
    device_id text,
    metric_time timestamp,
    metric_name text,
    metric_value decimal,
    PRIMARY KEY (device_id, metric_time)
) WITH CLUSTERING ORDER BY (metric_time DESC)
;
