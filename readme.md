# Aid - Another imageboard downloader

## Features
* Download entire boards
* Filter out threads based on image names and post content
* Threads meeting filter criteria will be added to a list for review
* Discards exact duplicates
* Checks boards for new images at regular intervals

## Requirements
* Currently needs a MySQL database to run.
* Java 7 (may work with earlier versions) 

## Dependencies
* The code requires the MySQL JDBC Driver.
* Tests additionally require JUnit4, DbUnit and SLF4J (API and JDK14).

http://www.mysql.com/downloads/connector/j/

http://www.junit.org/
http://www.dbunit.org/

http://slf4j.org/
