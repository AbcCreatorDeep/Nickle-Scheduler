drop DATABASE if exists nickle_scheduler;
CREATE DATABASE nickle_scheduler;
USE nickle_scheduler;

create table nickle_scheduler_executor(
    executor_id int unsigned auto_increment primary key,
    executor_name varchar(100) not null,
    executor_ip varchar(30) not null,
    executor_port int not null,
    update_time bigint not null,
    unique key(executor_ip),
    unique key(executor_port)
);
create table nickle_scheduler_executor_job(
    id int unsigned auto_increment primary key,
    job_name varchar(100) not null,
    executor_name varchar(100) not null,
    key(job_name,executor_name)
);
create table nickle_scheduler_job(
    id int unsigned auto_increment primary key,
    job_name varchar(100) not null,
    job_class_name varchar(100) not null,
    job_author varchar(20) not null,
    job_trigger_name varchar(100) not null,
    job_description varchar(255) not null,
    unique key(job_name),
    key(job_trigger_name)
);
create table nickle_scheduler_trigger(
    trigger_id int unsigned auto_increment primary key,
    trigger_name varchar(100) not null,
    trigger_cron varchar(100) not null,
    trigger_next_time bigint not null,
    trigger_status int not null,
    trigger_update_time bigint not null,
    unique key(trigger_name)
);
create table nickle_scheduler_run_job(
    id int unsigned auto_increment primary key,
    job_id int unsigned not null,
    job_name varchar(100) not null,
    trigger_name varchar(100) not null,
    executor_id int unsigned not null,
    schedule_time bigint not null,
    update_time bigint not null,
    key(update_time),
    key(trigger_name,job_name)
);
create table nickle_scheduler_fail_job(
    id int unsigned auto_increment primary key,
    job_id int unsigned not null,
    job_name varchar(100) not null,
    trigger_name varchar(100) not null,
    executor_id int unsigned not null,
    failed_time bigint not null,
    fail_reason tinyint not null,
    key(failed_time),
    key(trigger_name,job_name)
);
create table nickle_scheduler_success_job(
    id int unsigned auto_increment primary key,
    job_id int unsigned not null,
    job_name varchar(100) not null,
    trigger_name varchar(100) not null,
    executor_id int unsigned not null,
    success_time bigint not null,
    key(success_time),
    key(trigger_name,job_name)
);
create table nickle_scheduler_lock(
    lock_id int unsigned auto_increment primary key,
    lock_name varchar(100) not null,
    unique key(lock_name)
);