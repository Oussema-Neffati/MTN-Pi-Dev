# MTE-Pi-Dev

## Database Setup

To create the reservation table in your database:

1. Make sure your MySQL server is running
2. Connect to the MySQL server
3. Create the database if it doesn't exist: `CREATE DATABASE IF NOT EXISTS base_commune;`
4. Use the database: `USE base_commune;`
5. Run the SQL script located at: `src/main/resources/scripts/create_reservation_table.sql`

You can execute the script using one of these methods:
- MySQL command line: `mysql -u root -p base_commune < src/main/resources/scripts/create_reservation_table.sql`
- MySQL Workbench: Open the script file and execute it
- PhpMyAdmin: Import the SQL file

## Troubleshooting

If you encounter database errors:
- Verify database connection settings in `src/main/java/tn/esprit/utils/MyDataBase.java`
- Ensure table structure matches the column names used in the application

