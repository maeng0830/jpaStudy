package jpabook.jpashop.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import jpabook.jpashop.domain.Order;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class OrderRepository {

	private final EntityManager em;

	public OrderRepository(EntityManager em) {
		this.em = em;
	}

	public void save(Order order) {
		em.persist(order);
	}

	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}

	public List<Order> findAll() {
		return em.createQuery("select o from Order o", Order.class)
				.getResultList();
	}

	public List<Order> findAllByString(OrderSearch orderSearch) {

		String jpql = "select o from Order o join o.member m";
		boolean isFirstCondition = true;

		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " o.status = :status";
		}

		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " m.name like :name";
		}

		TypedQuery<Order> query = em.createQuery(jpql, Order.class)
				.setMaxResults(1000);

		if (orderSearch.getOrderStatus() != null) {
			query = query.setParameter("status", orderSearch.getOrderStatus());
		}
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			query = query.setParameter("name", orderSearch.getMemberName());
		}

		return query.getResultList();
	}

	// fetch join
	// fetch join은 엔티티 타입으로 조회할 때만 사용할 수 있는 JPA 문법이다.
	// fetch join은 select 절에 한번에 함께 조회하고자하는 데이터를 추가하는 것일 뿐이다.
	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery(
						"select o from Order o" +
								" join fetch o.member m" +
								" join fetch o.delivery d", Order.class)
				.getResultList();
	}

	// fetch join
	// distinct는 'DB의 distinct' + '데이터 조회 결과 컬렉션 내부에서 같은 식별자를 가진 엔티티 객체를 중복으로 판별해 제거' 기능을 제공한다.
	public List<Order> findAllWithItem() {
		return em.createQuery("select distinct o from Order o"
						+ " join fetch o.member m"
						+ " join fetch o.delivery d"
						+ " join fetch o.orderItems oi"
						+ " join fetch oi.item i", Order.class)
				.getResultList();
	}

	// 컬렉션 fetch join의 페이징 불가능 문제 해결
	// 우선 xToOne(member, delivery)에 대해서는 페치 조인을 사용해도 문제가 없다.
	// xToMany에 대해서는 default_batch_fetch_size를 사용하자.
	public List<Order> findAllWithMemberDelivery(int offset, int limit) {
		return em.createQuery(
						"select o from Order o" +
								" join fetch o.member m" +
								" join fetch o.delivery d", Order.class)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();
	}
}

