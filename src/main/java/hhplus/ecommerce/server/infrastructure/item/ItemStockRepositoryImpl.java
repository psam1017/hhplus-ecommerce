package hhplus.ecommerce.server.infrastructure.item;

import hhplus.ecommerce.server.domain.item.service.ItemStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Repository
public class ItemStockRepositoryImpl implements ItemStockRepository {

    private final ItemStockJpaRepository itemStockJpaRepository;
}
