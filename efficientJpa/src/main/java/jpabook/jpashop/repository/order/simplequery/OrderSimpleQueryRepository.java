package jpabook.jpashop.repository.order.simplequery;

import java.util.List;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderSimpleQueryRepository {

	private final EntityManager em;

	// API에서 사용할 DTO 타입으로 데이터 조회 -> 조회할 데이터의 크기를 줄일 수 있다.
	// DTO 타입으로 조회할 경우, 필요한 데이터들만 한번에 조회하기 때문에 fetch type과 무관하게 쿼리가 한번만 나간다.
	// DTO 생성자를 통해 select 절에 한번에 함께 조회하고자하는 데이터가 추가되기 때문에 fetch join은 사용할 필요가 없다.
	public List<OrderSimpleQueryDto> findOrderDtos() {
		return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto("
						+ "o.id, m.name, o.orderDate, o.status, d.address) "
						+ "from Order o "
						+ "join o.member m "
						+ "join o.delivery d", OrderSimpleQueryDto.class)
				.getResultList();
	}
}
