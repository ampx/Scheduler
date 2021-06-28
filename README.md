# Scheduler

## Overview 



![Architecture Overview](docs/img/scheduler_overview.PNG)

* Grafana datasource for making sync or async calls to trigger ETL jobs.
    * Make sync call from Grafana to trigger a job and return a results
    * Make sync call from Grafana to schedule a longer running job. Poll the scheduler server for results
    * Can also be used to simply trigger resources 

* Scheduler is a java backend uses Executors to generate data to Grafana
    * Executors are isolated modules that only cares how to generate and produce data
    * There are hooks in place for implementation of custom executors (Java Development)
    * Request Manager Service handles queuing user requests

Scheduler current implementation includes two executors
* Process executor - trigger external script (for example python ETL script) and provide arguments from Grafana variables
* Uri executor - fetch data from external web api using GET calls
Scheduler is design to be extended with custom Java executors 

![Runner Demo](docs/img/runner_animation.gif)

![Scheduler Demo](docs/img/scheduler_animation.gif)

![Labeled Scheduler Demo](docs/img/labeled_animation.gif)


##Configuration:

provide file name

provide individual configurations

```properties
cacheTTLMins=60

cacheJdbcConfig.driver=com.mysql.jdbc.Driver
cacheJdbcConfig.url=jdbc:mysql://localhost:3306/databaseName?characterEncoding=latin1
cacheJdbcConfig.user=user
cacheJdbcConfig.password=password
cacheJdbcConfig.databaseName=databaseName

jobsConfigList[0].type=process
jobsConfigList[0].name=process1
jobsConfigList[0].config.process=/usr/bin/python
```

executor configurations are specific to each executor, see documentation for individual executors for available options

## Executors

Process

* About

* Security

* Configurations

UriExecutor

* About

* How output parsed

* Configurations

Creating Executor

* Which class to implement

* Adding factory bean configuration


