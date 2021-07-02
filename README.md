# Scheduler

## Overview 

Scheduler is a Grafana datasource that serves as a proxy for calling external processes or APIs.
  As the name implies, you can make async calls from Grafana to schedule a
  long-running process and then monitor the progress or
  fetch cached output at later time.

Scheduler is a Java backend that instantiates Executors.  Executors are modules callable from Grafana to
  produce data.  Other parts of the Scheduler handle queuing of the requests from Grafana and caching executors output.
  Currently, there are two build in executors:
  * Process executor - trigger external script (for example python ETL script) and provide arguments from Grafana variables
  * Uri executor - fetch data from external web api using GET calls
  
You can implement your own executor by extending Executor parent class and providing a factory bean to instantiate
  your custom Executor.

![Architecture Overview](docs/img/scheduler_overview.PNG)

## Demo

**Run Requests**

![Runner Demo](docs/img/runner_animation.gif)

Scheduler can be used to generate data instantly as a regular datasource using Run requests. 
Fill out available options, and your request will be instantly passed down to targeted executor. 
Configured panel will instantly display the data produced by the executor.  
For continuously updated data, you can turn on auto-refresh to continuously target an executor.  
This is a good use case for simple computations or querying external API.
See details on the request form for more details.

**Submit Request**

![Labeled Scheduler Demo](docs/img/labeled_animation.gif)

Submit requests changes the way Grafana gets data from a data source. By adding Button Plugin to Grafana user can 
submit processing request asynchronously.
([My implementation of Grafana Button](https://github.com/ampx/grafana-json-button)) 
Once submitted, user can close out the page completely and fetch the results later.
Scheduler will cache the results for a configured amount of time.  
User can poll for completed data or progress status manually using data refresh button.
Alternatively user can enable auto-refresh to see progress change or to make data available immediately 
(Be cautious if you expect large data to be returned).

You can assign a label to a submitted job, 
and then fetch the results for that job by selecting assigned label from a drop down selection.

*Alternative way to submit a job:*

![Scheduler Demo](docs/img/scheduler_animation.gif)

Alternatively you can leave the label field blank and rely on arguments to fetch the results for previously submitted job.  
If a user submits a job and later another user tries to create a job with the same arguments, they will 
automatically see the results of a previous run.  
This is especially useful if you have a report that have default arguments, but you don't want triggered automatically.
Note that labeled and unlabeled requests are kept in a separate que.  
Meaning you would not be able to fetch results using arguments from previously submitted job with a label assigned to it.


##Configuration:

*  Available configurations:

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

* To load your custom configurations, provide a path to your config file using command line:

```
--spring.config.location=file:///Users/home/config/application.properties
```

* Configurations can be passed in as command line arguments.  This is useful for testing or when you need to edit 
  few configurations:

```
--cacheTTLMins=5
```

Each Executor implementation has specific configurations, see documentation for individual executors for available options

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


