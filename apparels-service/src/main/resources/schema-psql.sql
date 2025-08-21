DROP TABLE IF EXISTS APPARELS;

CREATE TABLE APPARELS (
                          id SERIAL PRIMARY KEY,
                          apparel_id VARCHAR(36),
                          item_name VARCHAR(100) NOT NULL,
                          description VARCHAR(255),
                          brand VARCHAR(100),
                          price DECIMAL(10,2),
                          cost DECIMAL(10,2),
                          stock INT,
                          apparel_type VARCHAR(20),
                          size_option VARCHAR(20)
);
