package hhplus.ecommerce.server.interfaces.controller.item;

import hhplus.ecommerce.server.application.ItemFacade;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/items")
@RestController
public class ItemController {

    private final ItemFacade itemFacade;

    @GetMapping("/top")
    public ApiResponse<List<ItemDto.ItemResponse>> findTopItems() {
        return ApiResponse.ok(
                itemFacade.findTopItems().stream()
                        .map(item -> ItemDto.ItemResponse.builder()
                                .id(item.id())
                                .name(item.name())
                                .price(item.price())
                                .amount(item.amount())
                                .build()
                        )
                        .toList()
        );
    }

    @GetMapping("")
    public ApiResponse<List<ItemDto.ItemResponse>> findItems() {
        return ApiResponse.ok(
                itemFacade.findItems().stream()
                        .map(item -> ItemDto.ItemResponse.builder()
                                .id(item.id())
                                .name(item.name())
                                .price(item.price())
                                .amount(item.amount())
                                .build()
                        )
                        .toList()
        );
    }
}
