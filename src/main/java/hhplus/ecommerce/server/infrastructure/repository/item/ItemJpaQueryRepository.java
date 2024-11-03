package hhplus.ecommerce.server.infrastructure.repository.item;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.order.QOrder;
import hhplus.ecommerce.server.domain.order.QOrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hhplus.ecommerce.server.domain.item.QItem.item;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Repository
public class ItemJpaQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(em.find(Item.class, itemId));
    }

    public List<Item> findAllById(Collection<Long> itemIds) {
        return query
                .selectFrom(item)
                .where(item.id.in(itemIds))
                .fetch();
    }

    public List<Item> findAllBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return query
                .selectFrom(item)
                .where(itemNameContains(searchCond.keyword()))
                .orderBy(specifyOrder(searchCond.prop(), searchCond.dir()))
                .limit(searchCond.getLimit())
                .offset(searchCond.getOffset())
                .fetch();
    }

    public long countAllBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        Long count = query.select(item.countDistinct())
                .from(item)
                .where(itemNameContains(searchCond.keyword()))
                .fetchOne();
        return count != null ? count : 0;
    }

    public List<Item> findTopItemsOrderDateTimeBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QOrder qOrder = QOrder.order;
        return query
                .select(item)
                .from(item)
                .leftJoin(qOrderItem).on(item.id.eq(qOrderItem.item.id))
                .leftJoin(qOrder).on(qOrderItem.order.id.eq(qOrder.id))
                .where(
                        qOrder.status.eq(OrderStatus.ORDERED),
                        qOrder.orderDateTime.between(startDateTime, endDateTime)
                )
                .groupBy(item)
                .orderBy(qOrderItem.quantity.multiply(qOrderItem.price).sum().desc())
                .limit(5)
                .fetch();
    }

    private BooleanExpression itemNameContains(String keyword) {
        return StringUtils.hasText(keyword) ? item.name.contains(keyword) : null;
    }

    /**
     * @param prop 정렬 속성은 ID, name 2가지를 사용
     * @param dir 정렬 방향은 ASC, DESC 두 가지만 사용
     * @return OrderSpecifier
     */
    private OrderSpecifier<?> specifyOrder(String prop, String dir) {
        Order order = Objects.equals(dir, "asc") ? Order.ASC : Order.DESC;
        prop = prop.toLowerCase();

        return switch (prop) {
            case "id" -> new OrderSpecifier<>(order, item.id);
            case "name" -> new OrderSpecifier<>(order, item.name);
            default -> null;
        };
    }
}
