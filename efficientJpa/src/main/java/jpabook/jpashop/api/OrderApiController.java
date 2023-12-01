package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderApiController {

	private final OrderRepository orderRepository;

	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());

		// Lazy 초기화 작업
		for (Order order : all) {
			order.getMember().getName();
			order.getDelivery().getAddress();

			List<OrderItem> orderItems = order.getOrderItems();
			orderItems.stream().forEach(o -> o.getItem().getName());
		}

		return all;

		// N + 1 문제 발생
		// Order + Member + Delivery + OrderItem + (OrderItem * Item)
		// 1 + 2 + 2 + 2 + (2 * 2)
	}

	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());

		// Order 뿐만 아니라, OrderItem도 DTO로 변환해야한다.
		List<OrderDto> result = orders.stream()
				.map(OrderDto::new)
				.collect(Collectors.toList());

		return result;

		// N + 1 문제 발생
		// Order + Member + Delivery + OrderItem + (OrderItem * Item)
		// 1 + 2 + 2 + 2 + (2 * 2)
	}

	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		// fetch join
		List<Order> orders = orderRepository.findAllWithItem();

		// @OneToMany에 대해 fetch join할 경우, 동일한 엔티티 객체(Order)가 Many(OrderItem)의 개수 만큼 생성된다.
			// DB에서 join으로 인해 'Order(id=4) : OrderItem(id=5)', 'Order(id=4) : OrderItem(id=6)'이 조회된다.
			// Java 어플리케이션에서는 'Order(id=4, List.of(OrderItem(id=5), OrderItem(id=6))'이 두개 생성된다.
		// 이 문제를 해결하기 위해 JPQL의 ditinct 키워드를 사용할 수 있다.
		for (Order order : orders) {
			System.out.println("order ref =" + order + " id=" + order.getId());
		}
		// No distinct
			// order ref =jpabook.jpashop.domain.Order@60a16af3 id=4
			// order ref =jpabook.jpashop.domain.Order@60a16af3 id=4
			// order ref =jpabook.jpashop.domain.Order@5b1298eb id=11
			// order ref =jpabook.jpashop.domain.Order@5b1298eb id=11
		// Yes distinct
			// order ref =jpabook.jpashop.domain.Order@62c89ff0 id=4
			// order ref =jpabook.jpashop.domain.Order@1fb18d53 id=11

		List<OrderDto> result = orders.stream()
				.map(OrderDto::new)
				.collect(Collectors.toList());

		return result;

		// but 컬렉션 페치조인 시 페이징이 불가능하다는 단점이 존재한다.
	}

	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
										@RequestParam(value = "limit", defaultValue = "100") int limit) {
		// fetch join for xToOne + No distinct + hibernate.default_batch_fetch_size
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

		for (Order order : orders) {
			System.out.println("order ref =" + order + " id=" + order.getId());
		}
		// No distinct
			// order ref =jpabook.jpashop.domain.Order@467c66ca id=4
			//order ref =jpabook.jpashop.domain.Order@1a8678b6 id=11

		List<OrderDto> result = orders.stream()
				.map(OrderDto::new)
				.collect(Collectors.toList());

		return result;
	}

	@Data
	static class OrderDto {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems; // OrderItem -> OrderItemDto

		public OrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName(); // Lazy 초기화
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress(); // Lazy 초기화
			orderItems = order.getOrderItems().stream() // Lazy 초기화
					.map(OrderItemDto::new)
					.collect(Collectors.toList());
		}
	}

	@Data
	static class OrderItemDto {

		private String itemName;
		private int orderPrice;
		private int count;

		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName(); // Lazy 초기화
			orderPrice = orderItem.getOrderPrice();
			count = orderItem.getCount();
		}
	}
}
