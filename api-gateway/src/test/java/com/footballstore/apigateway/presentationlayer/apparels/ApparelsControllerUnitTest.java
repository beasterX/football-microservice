package com.footballstore.apigateway.presentationlayer.apparels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.footballstore.apigateway.businesslayer.apparels.ApparelsService;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class ApparelsControllerUnitTest {

    @Autowired
    private ApparelsController controller;

    @MockitoBean
    private ApparelsService service;

    private final String VALID_ID  = "aaa11111-bbbb-cccc-dddd-eeeeeeeeeeee";
    private final String NOT_FOUND = "bbb22222-bbbb-cccc-dddd-eeeeeeeeeeee";

    @Test
    void getAllApparels_whenServiceReturnsList_thenOkAndBody() {
        var a1 = ApparelResponseModel.builder()
                .apparelId("id1")
                .itemName("Item1")
                .description("Desc1")
                .brand("Brand1")
                .price(new BigDecimal("10.00"))
                .cost(new BigDecimal("5.00"))
                .stock(10)
                .apparelType("JERSEY")
                .sizeOption("M")
                .build();
        var a2 = ApparelResponseModel.builder()
                .apparelId("id2")
                .itemName("Item2")
                .description("Desc2")
                .brand("Brand2")
                .price(new BigDecimal("20.00"))
                .cost(new BigDecimal("7.00"))
                .stock(20)
                .apparelType("SHORTS")
                .sizeOption("S")
                .build();

        when(service.getAllApparels()).thenReturn(List.of(a1, a2));

        ResponseEntity<List<ApparelResponseModel>> resp = controller.getAllApparels();

        assertAll("getAll",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody(), "Body must not be null"),
                () -> assertEquals(2, resp.getBody().size(), "Should return two items"),
                () -> assertEquals("id1", resp.getBody().get(0).getApparelId(), "First ID matches")
        );
        verify(service, times(1)).getAllApparels();
    }

    @Test
    void getApparelById_whenValid_thenOkAndBody() {
        var model = ApparelResponseModel.builder()
                .apparelId(VALID_ID)
                .itemName("ItemX")
                .description("DescX")
                .brand("BrandX")
                .price(new BigDecimal("100.00"))
                .cost(new BigDecimal("50.00"))
                .stock(100)
                .apparelType("JERSEY")
                .sizeOption("L")
                .build();

        when(service.getApparelById(VALID_ID)).thenReturn(model);

        ResponseEntity<ApparelResponseModel> resp = controller.getApparelById(VALID_ID);

        assertAll("getById",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody(), "Body must not be null"),
                () -> assertEquals("ItemX", resp.getBody().getItemName()),
                () -> assertEquals(100, resp.getBody().getStock())
        );
        verify(service).getApparelById(VALID_ID);
    }

    @Test
    void getApparelById_whenServiceThrowsNotFound_thenPropagates() {
        doThrow(new NotFoundException("not found"))
                .when(service).getApparelById(NOT_FOUND);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.getApparelById(NOT_FOUND)
        );
        assertEquals("not found", ex.getMessage());
        verify(service).getApparelById(NOT_FOUND);
    }

    @Test
    void createApparel_whenValid_thenCreatedAndBody() {
        var req = ApparelRequestModel.builder()
                .itemName("NewItem")
                .description("NewDesc")
                .brand("NewBrand")
                .price(new BigDecimal("60.00"))
                .cost(new BigDecimal("30.00"))
                .stock(50)
                .apparelType("JERSEY")
                .sizeOption("M")
                .build();

        var respModel = ApparelResponseModel.builder()
                .apparelId("new-id")
                .itemName("NewItem")
                .description("NewDesc")
                .brand("NewBrand")
                .price(new BigDecimal("60.00"))
                .cost(new BigDecimal("30.00"))
                .stock(50)
                .apparelType("JERSEY")
                .sizeOption("M")
                .build();

        when(service.createApparel(req)).thenReturn(respModel);

        ResponseEntity<ApparelResponseModel> resp = controller.createApparel(req);

        assertAll("create",
                () -> assertEquals(HttpStatus.CREATED, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody(), "Body must not be null"),
                () -> assertEquals("new-id", resp.getBody().getApparelId()),
                () -> assertEquals(50, resp.getBody().getStock())
        );
        verify(service).createApparel(req);
    }

    @Test
    void createApparel_whenServiceThrowsInvalidInput_thenPropagates() {
        var req = ApparelRequestModel.builder().build();
        when(service.createApparel(req)).thenThrow(new InvalidInputException("bad input"));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.createApparel(req)
        );
        assertEquals("bad input", ex.getMessage());
        verify(service).createApparel(req);
    }

    @Test
    void updateApparel_whenValid_thenOkAndBody() {
        var req = ApparelRequestModel.builder()
                .itemName("Upd")
                .description("UpdDesc")
                .brand("UpdBrand")
                .price(new BigDecimal("70.00"))
                .cost(new BigDecimal("35.00"))
                .stock(80)
                .apparelType("JERSEY")
                .sizeOption("L")
                .build();

        var respModel = ApparelResponseModel.builder()
                .apparelId(VALID_ID)
                .itemName("Upd")
                .description("UpdDesc")
                .brand("UpdBrand")
                .price(new BigDecimal("70.00"))
                .cost(new BigDecimal("35.00"))
                .stock(80)
                .apparelType("JERSEY")
                .sizeOption("L")
                .build();

        when(service.updateApparel(VALID_ID, req)).thenReturn(respModel);

        ResponseEntity<ApparelResponseModel> resp = controller.updateApparel(VALID_ID, req);

        assertAll("update",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody(), "Body must not be null"),
                () -> assertEquals("Upd", resp.getBody().getItemName()),
                () -> assertEquals(80, resp.getBody().getStock())
        );
        verify(service).updateApparel(VALID_ID, req);
    }

    @Test
    void updateApparel_whenServiceThrowsNotFound_thenPropagates() {
        var req = ApparelRequestModel.builder().build();
        when(service.updateApparel(NOT_FOUND, req)).thenThrow(new NotFoundException("gone"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.updateApparel(NOT_FOUND, req)
        );
        assertEquals("gone", ex.getMessage());
        verify(service).updateApparel(NOT_FOUND, req);
    }

    @Test
    void deleteApparel_whenValid_thenNoContent() {
        when(service.deleteApparel(VALID_ID)).thenReturn(null);

        ResponseEntity<Void> resp = controller.deleteApparel(VALID_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode(), "Should return 204");
        verify(service).deleteApparel(VALID_ID);
    }

    @Test
    void deleteApparel_whenServiceThrowsNotFound_thenPropagates() {
        when(service.deleteApparel(NOT_FOUND)).thenThrow(new NotFoundException("not here"));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.deleteApparel(NOT_FOUND)
        );
        assertEquals("not here", ex.getMessage());
        verify(service).deleteApparel(NOT_FOUND);
    }
}
