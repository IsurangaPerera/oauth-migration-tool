# OAuth Migration Tool

## How to Build

```
mvn clean install
mvn package
```

## How to run
```
cd components/org.wso2.carbon.oauth.migration.tool
cd target/dist/bin
./token-migrate
```

## How to debug the tool

To debug the tool remotely do the following.

####Linux:

Execute following commands in the shell that the tool in running. 

 * ```JAVA_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y"```
 * ```export JAVA_OPTS```
 * ```./token-migrate.sh <arguments>```

Use IDEs remote debugging feature to connect to port 5005. 

####Windows