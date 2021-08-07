# Configurations:

See [configuration template](src/main/resources/application.properties) to see available configurations

Setting configurations:

* To load your custom configurations, provide a path to your config file using command line:

```
--spring.config.location=file://Users/home/config/application.properties
```

* Configurations can be passed in as command line arguments.  This is useful for testing or when you need to edit
  few configurations:

```
--cacheTTLMins=5
```
