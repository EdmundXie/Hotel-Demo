# hotel-demo

## Step 1. Create Mysql database

Create database `heima` in Mysql. Choose charset `utf8mb4` and Sequencing rules `utf8mv4_general_ci`

Use tb_hotel.sql to create table `tb_hotel`

## Step 2. Change configurations

In `resources/application.yaml`
change datasource username and password

In `HotelDemoApplication`
change hostname and port of `RestHighLevelClient`

## Step 3. Run hotel-demo

### Run with HotelDemoApplication
Run the `main` method in  HotelDemoApplication

### Use maven
Use `clean` and `package` in Maven lifecycle
then run with` javac -java yourpackage.jar`

