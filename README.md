# Poller API

## Objective
Creating REST Spring Boot API

### Maintainer 
<table>
    <thead>
      <tr>
        <th>Name</th>
        <th>NUID</th>
      </tr>
    </thead>
    <tbody>
        <tr>
            <td>Naresh Agrawal</td>
            <td>001054600</td>
        </tr>
    </tbody>
</table>

## Technology Stack
* Spring Boot
* MYSQL 
* Helm

## Prerequisites
* JAVA(JDK)
* Docker
* Helm

## Running application locally
- Run following commands in the root directory
```
$ mvn package
$ cd target
$ java -jar poller-latest.jar
```
* Shutdown application locally
```
$ ctrl+c
```

## Build Docker image
```
$ docker build -t <image-name> .
```

## Run application by Docker image
```
$ docker run --rm -p 9091:9091 <image-name>
```
The application should be running and listening for HTTP requests on port 9091 on localhost<br/>
http://localhost:9091/

## Deploy application on Kubernetes Cluster
```
$ helm install poller ./helm/
```

## Uninstall application on Kubernetes Cluster
```
$ helm delete poller
```
