package com.footballstore.apigateway.presentationlayer.warehouses;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.footballstore.apigateway.businesslayer.warehouses.WarehousesService;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.InvalidWarehouseCapacityException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class WarehousesControllerUnitTest {

    @Autowired
    private WarehousesController controller;

    @MockitoBean
    private WarehousesService service;

    private final String VALID_ID    = "11111111-2222-3333-4444-555555555555";
    private final String NOTFOUND_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private final String BAD_ID      = "bad-uuid";

    @Test
    void getAllWarehouses_whenServiceReturnsList_thenOkAndBody() {
        var w = WarehouseResponseModel.builder()
                .warehouseId(VALID_ID)
                .locationName("LocA")
                .address("AddrA")
                .capacity(1000)
                .build();

        when(service.getAllWarehouses()).thenReturn(List.of(w));

        ResponseEntity<List<WarehouseResponseModel>> resp =
                controller.getAllWarehouses();

        assertAll("getAll",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(1, resp.getBody().size()),
                () -> assertEquals(VALID_ID, resp.getBody().get(0).getWarehouseId())
        );
        verify(service).getAllWarehouses();
    }

    @Test
    void getWarehouseById_whenValid_thenOkAndBody() {
        var w = WarehouseResponseModel.builder()
                .warehouseId(VALID_ID)
                .locationName("LocA")
                .address("AddrA")
                .capacity(1000)
                .build();

        when(service.getWarehouseById(VALID_ID)).thenReturn(w);

        ResponseEntity<WarehouseResponseModel> resp =
                controller.getWarehouseById(VALID_ID);

        assertAll("getById",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(VALID_ID, resp.getBody().getWarehouseId())
        );
        verify(service).getWarehouseById(VALID_ID);
    }

    @Test
    void getWarehouseById_whenInvalid_thenThrows() {
        doThrow(new InvalidInputException("bad id"))
                .when(service).getWarehouseById(BAD_ID);

        var ex = assertThrows(InvalidInputException.class, () ->
                controller.getWarehouseById(BAD_ID)
        );
        assertTrue(ex.getMessage().contains("bad id"));
        verify(service).getWarehouseById(BAD_ID);
    }

    @Test
    void getWarehouseById_whenNotFound_thenThrows() {
        doThrow(new NotFoundException("nf"))
                .when(service).getWarehouseById(NOTFOUND_ID);

        var ex = assertThrows(NotFoundException.class, () ->
                controller.getWarehouseById(NOTFOUND_ID)
        );
        assertEquals("nf", ex.getMessage());
        verify(service).getWarehouseById(NOTFOUND_ID);
    }

    @Test
    void createWarehouse_whenValid_thenCreatedAndBody() {
        var req = WarehouseRequestModel.builder()
                .locationName("LocB")
                .address("AddrB")
                .capacity(200)
                .build();
        var w = WarehouseResponseModel.builder()
                .warehouseId(VALID_ID)
                .locationName("LocB")
                .address("AddrB")
                .capacity(200)
                .build();

        when(service.createWarehouse(req)).thenReturn(w);

        ResponseEntity<WarehouseResponseModel> resp =
                controller.createWarehouse(req);

        assertAll("create",
                () -> assertEquals(HttpStatus.CREATED, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(VALID_ID, resp.getBody().getWarehouseId())
        );
        verify(service).createWarehouse(req);
    }

    @Test
    void createWarehouse_whenInvalidCapacity_thenThrows() {
        var req = WarehouseRequestModel.builder()
                .locationName("Tiny")
                .address("Addr")
                .capacity(50)
                .build();
        doThrow(new InvalidWarehouseCapacityException("too small"))
                .when(service).createWarehouse(req);

        var ex = assertThrows(InvalidWarehouseCapacityException.class, () ->
                controller.createWarehouse(req)
        );
        assertEquals("too small", ex.getMessage());
        verify(service).createWarehouse(req);
    }

    @Test
    void updateWarehouse_whenValid_thenOkAndBody() {
        var req = WarehouseRequestModel.builder()
                .locationName("LocC")
                .address("AddrC")
                .capacity(300)
                .build();
        var w = WarehouseResponseModel.builder()
                .warehouseId(VALID_ID)
                .locationName("LocC")
                .address("AddrC")
                .capacity(300)
                .build();

        when(service.updateWarehouse(VALID_ID, req)).thenReturn(w);

        ResponseEntity<WarehouseResponseModel> resp =
                controller.updateWarehouse(VALID_ID, req);

        assertAll("update",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(VALID_ID, resp.getBody().getWarehouseId())
        );
        verify(service).updateWarehouse(VALID_ID, req);
    }

    @Test
    void updateWarehouse_whenNotFound_thenThrows() {
        var req = WarehouseRequestModel.builder().build();
        doThrow(new NotFoundException("nf"))
                .when(service).updateWarehouse(NOTFOUND_ID, req);

        var ex = assertThrows(NotFoundException.class, () ->
                controller.updateWarehouse(NOTFOUND_ID, req)
        );
        assertEquals("nf", ex.getMessage());
        verify(service).updateWarehouse(NOTFOUND_ID, req);
    }

    @Test
    void deleteWarehouse_whenValid_thenNoContent() {
        doNothing().when(service).deleteWarehouse(VALID_ID);

        ResponseEntity<Void> resp =
                controller.deleteWarehouse(VALID_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(service).deleteWarehouse(VALID_ID);
    }

    @Test
    void deleteWarehouse_whenInvalid_thenThrows() {
        doThrow(new InvalidInputException("bad"))
                .when(service).deleteWarehouse(BAD_ID);

        var ex = assertThrows(InvalidInputException.class, () ->
                controller.deleteWarehouse(BAD_ID)
        );
        assertEquals("bad", ex.getMessage());
        verify(service).deleteWarehouse(BAD_ID);
    }

    @Test
    void deleteWarehouse_whenNotFound_thenThrows() {
        doThrow(new NotFoundException("nf"))
                .when(service).deleteWarehouse(NOTFOUND_ID);

        var ex = assertThrows(NotFoundException.class, () ->
                controller.deleteWarehouse(NOTFOUND_ID)
        );
        assertEquals("nf", ex.getMessage());
        verify(service).deleteWarehouse(NOTFOUND_ID);
    }
}
