# jmssqlcli
MSSQL [very] Simple client application

Just the JDBC-powered console MSSQL client with a few options.

```
usage: (c)2023 MSSQL [very] Simple CLI: java -jar jmssqlcli
 -d,--database <arg>    Database name
 -h,--help              This help screen
 -i,--inputfile <arg>   Input file with SQL statement(s)
 -l,--login <arg>       Database login or full login like user@servername
 -m,--mode <arg>        Worker mode [select | update]
 -p,--password <arg>    Database password
 -s,--server <arg>      Database server address, e.g. db.example.com:1433
```

Pass the SQL file, define the working mode (update or select) and run.

**update_script.sql** file:
```
UPDATE MY_SUPER_TABLE SET DISPLAY_NAME='updated'
```

"Update" command:
```
java -jar target\jmssqlcli.jar -s mssql.example.com:1433 -d mydatabase -l iamtheuser --password SeCuRePaSsWord -m update -i c:\path\to\update_script.sql
### (c)2023 MSSQL [very] Simple CLI -=- Start time: Wed Jan 13 19:39:43 EET 2023 -=- Work mode: update
### Loaded 54 bytes from c:\path\to\update_script.sql
### Update executed
### End time: Wed Jan 13 19:39:44 EET 2023
```

...and now - select data:

**select_script.sql** file:
```
SELECT TOP 3 * from MY_SUPER_TABLE;
```     

"Select" command:
```
g:\Projects\sqlclient>java -jar target\jmssqlcli.jar -s mssql.example.com:1433 -d mydatabase -l iamtheuser --password SeCuRePaSsWord -m select -i c:\path\to\select_script.sql
### (c)2023 MSSQL [very] Simple CLI -=- Start time: Wed Jan 13 19:53:24 EET 2023 -=- Work mode: select
### Loaded 41 bytes from c:\path\to\select_script.sql


+--------------+----------------------+----------------+----------------+----------------------------+
|  ID_LOOKUP   |     FIELD_NAME       |  FIELD_VALUE   |  DISPLAY_NAME  |       CREATION_DATE        |
+--------------+----------------------+----------------+----------------+----------------------------+
|      1       |     Economic Data    |    aaaaa       |    updated     |  2023-01-13 08:49:04.773   |
|      2       |     Economic Data    |  bbbbbbbbbb    |    updated     |  2023-01-13 08:49:04.777   |
|      3       |    Economic Test1    |    vvccc       |    updated     |  2023-01-13 08:49:04.783   |
+--------------+----------------------+----------------+----------------+----------------------------+


### End time: Wed Jan 13 19:53:26 EET 2023
```

### Docker

Using *jmssqlcli* in Docker is simple. 
```
docker run -v /opt:/r emergn/jmssqlcli:1.4 -s mssql.example.com:1433 -d mydatabase -l iamtheuser --password SeCuRePaSsWord -m update -i /r/update_script.sql
```

Just use `docker run -v /opt:/r emergn/jmssqlcli:1.4 ...` prefix. (mapped `/opt` dir as `/r` for SQL files)

### Docker: Log files 

By default, *jmssqlcli* is using `/opt/jmssqlcli.log` file for logs. Default log level is `DEBUG`.
So, to get the logs out of the container, just map the internal `/opt/jmssqlcli.log` file to the host, e.g.:

```
docker run -v /opt:/r -v /mypath/jmssqlcli.log:/opt/jmssqlcli.log emergn/jmssqlcli:1.4 ...
```



eof