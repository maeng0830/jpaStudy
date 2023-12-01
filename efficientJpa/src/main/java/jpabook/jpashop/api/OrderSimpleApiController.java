package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RequiredArgsConstructor
@RestController
public class OrderSimpleApiController {

	private final OrderRepository orderRepository;
	private final OrderSimpleQueryRepository orderSimpleQueryRepository;

	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());

		return orders;
	}

	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2() {
		// Order 2개 조회
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());

		// 각 Order 당 member, delivery를 개별적으로 조회
		return orders.stream()
				.map(SimpleOrderDto::new)
				.collect(Collectors.toList());

		// N + 1 = 1 + member N + delivery N
		// N은 order의 개수이다. order가 2개라면, 1 + 2 + 2 = 5개의 쿼리가 실행된다. <- 최악의 경우
	}

	@GetMapping("/api/v3/simple-orders")
	public List<SimpleOrderDto> ordersV3() {
		// fetch join을 통해 order에 member, delivery의 실제 객체를 포함하여 조회한다.
		// N + 1 문제 해결
		List<Order> orders = orderRepository.findAllWithMemberDelivery();

		return orders.stream()
				.map(SimpleOrderDto::new)
				.collect(Collectors.toList());
	}

	@GetMapping("/api/v4/simple-orders")
	public List<OrderSimpleQueryDto> ordersV4() {
		// DTO 타입으로 필요한 데이터만 선택 조회하여, 성능을 개선할 수 있다.
		return orderSimpleQueryRepository.findOrderDtos();

		// 하지만 OrderSimpleQueryDto라는 특정 DTO를 조회할 때만 사용할 수 있기 때문에 재사용성이 비교적 낮다.
	}

	@Data
	static class SimpleOrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;

		public SimpleOrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName(); // LAZY 초기화
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress(); // LAZY 초기화
		}
	}
}
