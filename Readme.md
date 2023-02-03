# Using AKS workload identity to connect to azure sql db with Java/SpringBoot

This repository shows an example of using AKS workload identity to connect to azure sql db with Java/SpringBoot

## Update AKS to use Workload Identity and Create Workload Identity

This [documentation](https://learn.microsoft.com/en-us/azure/aks/learn/tutorial-kubernetes-workload-identity) shows how to Update the azure environement and create a workload identity 

**Note:** If you are performing the changes on an existing aks cluster, update your cluster to enable oidc-issuer and workload identity as following :
```bash
az aks update -g "<RG NAME>" --name "<aks cluster name>"  --enable-workload-identity --enable-oidc-issuer
```
## Use the latests libraries for [azure-identity](https://mvnrepository.com/artifact/com.azure/azure-identity),[msal4j](https://mvnrepository.com/artifact/com.microsoft.azure/msal4j),[mssql-jdbc](https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc)

```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-identity</artifactId>
        <version>1.7.3</version>
    </dependency>
    <dependency>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>msal4j</artifactId>
        <version>1.13.4</version>
    </dependency>
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
        <version>12.2.0.jre11</version>
    </dependency>
```

## Specify in the connection the authentication mode as ActiveDirectoryMSI and the msiClientId
Update your connection string with **authentication=ActiveDirectoryMSI** and **msiClientId=${MSI_CLIENTID}**

**Note** : the msiClientId is the client id of the managed identity you have created
```
spring.datasource.url=jdbc:sqlserver://${DATABASE_HOST}:1433;database=${DATABASE_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;authentication=ActiveDirectoryMSI;msiClientId=${MSI_CLIENTID};
```

## In the deployement manifest specify the use of the workload identity
In the deployemnt manifest, you should add the annotation **azure.workload.identity/use: "true"** and specify the **serviceAccountName: <service-account-name>** which is the name of the service account  you created for the Namespace you are using, In this case we are using the default Namespace

```yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-azsql-aks
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-azsql-aks
  template:
    metadata:
      labels:
        app: springboot-azsql-aks
      annotations:
        azure.workload.identity/use: "true" 
    spec:
      serviceAccountName: <service-account-name>
      containers:
      ...
```

## Create a user for your managed identity in Azure Sql DB
To allow your managed identity access to the Azure SQL database, you need to create a user in the database for your managed identity. The following T-SQL code allows you to do this

```sql
create user [Managed-Identity-Name] from external provider;
alter role db_datareader add member [Managed-Identity-Name];
alter role db_datawriter add member [Managed-Identity-Name];
```

**Note**: In this T-SQL script you should youse your managed identity name, the brackets are also a part of the script


## Popular errors

### 1- MSI Token failure
```
com.microsoft.sqlserver.jdbc.SQLServerException: MSI Token failure: Failed to acquire access token from IMDS
```
This error can occure due to workload Identity misconfiguration, or if you are not using the latest versions of [azure-identity](https://mvnrepository.com/artifact/com.azure/azure-identity),[msal4j](https://mvnrepository.com/artifact/com.microsoft.azure/msal4j),[mssql-jdbc](https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc)

### 2- Login Failed

```
Login failed for user '<token-identified principal>
```
This Error means that the managed identity you are using as workload identity does not have access the the database, to give access to your managed identity for the DB reffer to the section : **Create a user for your managed identity in Azure Sql DB**

## Acknowledgments
Thank you To Mike Lapierre and Adil Touati, your support was greatly appreciated