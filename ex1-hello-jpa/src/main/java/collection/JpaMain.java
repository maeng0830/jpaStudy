package collection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		try {
			Member member = new Member();
			member.setName("member1");
			member.setHomeAddress(new Address("춘천시", "서부대성로", "12345"));

			member.getFavoriteFoods().add("치킨");
			member.getFavoriteFoods().add("족발");
			member.getFavoriteFoods().add("피자");

			member.getAddressHistory().add(new Address("중랑구", "면목동", "12345"));
			member.getAddressHistory().add(new Address("파주시", "운정", "12345"));

			// 컬렉션 테이블에 대한 데이터 저장이 한번에 된다.
			// 값 타입 컬렉션은 영속성 전이 + 고아 객체 기능을 필수로 가진다고 볼 수 있다. -> 라이프 사이클이 Member에 의존한다.
			em.persist(member);

			em.flush();
			em.clear();

			// 값 타입 컬렉션은 기본적으로 지연 로딩이다.
			Member findMember = em.find(Member.class, member.getId());

			List<Address> addressHistory = findMember.getAddressHistory();
			// 별도의 select 쿼리가 실행된다.
			addressHistory.get(0);

			// 컬렉션 또한 객체이므로, 데이터 변경 시 참조를 주의해야한다.
			// 치킨 -> 한식
			findMember.getFavoriteFoods().remove("치킨");
			findMember.getFavoriteFoods().add("한식");

			// 중랑구 -> 도봉구
			Optional<Address> optionalOldAddress = findMember.getAddressHistory().stream()
					.filter(h -> h.getCity().equals("중랑구"))
					.findFirst();

			// 해당 row delete
			findMember.getAddressHistory().remove(optionalOldAddress.get());
			// 컬렉션에 남아있는 모든 데이터들(도봉구, 파주시)에 대한 insert 쿼리가 다시 나간다.
			findMember.getAddressHistory()
					.add(new Address("도봉구", optionalOldAddress.get().getStreet(),
							optionalOldAddress.get().getZipcode()));

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
