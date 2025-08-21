DROP TABLE IF EXISTS WAREHOUSES;

CREATE TABLE WAREHOUSES (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            warehouse_id VARCHAR(36),
                            LOCATION_NAME VARCHAR(100) NOT NULL,
                            ADDRESS VARCHAR(255) NOT NULL,
                            CAPACITY INT
);
