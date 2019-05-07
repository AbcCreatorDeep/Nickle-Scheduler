create table nickle_scheduler_executor(
    executor_id int unsigned auto_increment primary key,
    executor_ip varchar(30) not null,
    executor_port int not null,
    key(executor_ip)
);
create table nickle_scheduler_executor_job(
    id int unsigned auto_increment primary key,
    job_id  int unsigned not null,
    executor_id int unsigned not null,
    key(job_id,executor_id)
);
create table nickle_scheduler_job(
    id int unsigned auto_increment primary key,
    job_name varchar(100) not null,
    job_class_name varchar(100) not null,
    job_author varchar(20) not null,
    job_trigger_name varchar(100) not null,
    job_description varchar(255) not null,
    key(job_name),
    key(job_trigger_name)
);
create table nickle_scheduler_trigger(
    trigger_id int unsigned auto_increment primary key,
    trigger_name varchar(100) not null,
    trigger_cron varchar(100) not null,
    trigger_next_time bigint not null,
    key(trigger_name)
);
create table nickle_scheduler_run_job(
    run_job_id int unsigned auto_increment primary key,
    job_name varchar(100) not null,
    trigger_name varchar(100) not null,
    executor_id int unsigned not null,
    schedule_time bigint not null,
    update_time bigint not null,
    key(update_time),
    key(trigger_name,job_name)
);
create table nickle_scheduler_lock(
    lock_id int unsigned auto_increment primary key,
    lock_name varchar(100) not null,
    key(lock_name)
);