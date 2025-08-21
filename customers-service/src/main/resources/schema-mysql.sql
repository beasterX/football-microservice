USE `customers-db`;
DROP TABLE IF EXISTS CUSTOMERS;

CREATE TABLE CUSTOMERS (
                           ID INT AUTO_INCREMENT PRIMARY KEY,
                           CUSTOMER_ID VARCHAR(36),
                           FIRST_NAME VARCHAR(100) NOT NULL,
                           LAST_NAME VARCHAR(100) NOT NULL,
                           EMAIL VARCHAR(150) UNIQUE NOT NULL,
                           PHONE VARCHAR(50),
                           REGISTRATION_DATE DATE,
                           PREFERRED_CONTACT VARCHAR(20),
                           STREET VARCHAR(150),
                           CITY VARCHAR(100),
                           STATE VARCHAR(100),
                           POSTAL_CODE VARCHAR(20),
                           COUNTRY VARCHAR(100)
);
