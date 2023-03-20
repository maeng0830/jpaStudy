package jpql.projection;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import jpql.Address;
import jpql.Member;
import jpql.MemberDTO;
import jpql.Order;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		// 엔티티 프로젝션
		try {
			Member member = new Member();
			member.setUsername("member1");
			member.setAge(10);
			em.persist(member);

			em.flush();
			em.clear();

			// 엔티티 프로젝션으로 가져온 엔티티들은 모두 영속성 컨텍스트로 관리된다.
			List<Member> result = em.createQuery("select m from Member m", Member.class)
					.getResultList();

			Member findMember = result.get(0);
			findMember.setAge(20);

			// update 쿼리 실행
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		EntityManager em2 = emf.createEntityManager();
		EntityTransaction tx2 = em2.getTransaction();
		tx2.begin();

		// 임베디드 프로젝션
		try {
			Order order = new Order();
			order.setAddress(new Address("city", "street", "zipcode"));
			em2.persist(order);

			em2.flush();
			em2.clear();

			// 소속된 엔티티로부터 조회
			List<Address> result = em2.createQuery("select o.address from Order o",
							Address.class)
					.getResultList();

			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		} finally {
			em2.close();
		}

		EntityManager em3 = emf.createEntityManager();
		EntityTransaction tx3 = em3.getTransaction();
		tx3.begin();

		// 스칼라 프로젝션
		try {
			// 방법 1
			List resultList = em3.createQuery("select m.username, m.age from Member m")
					.getResultList();

			Object o = resultList.get(0); // 리스트의 첫번째 객체
			Object[] result = (Object[]) o; // 객체의 배열화
			System.out.println("result[0] = " + result[0]); //배열의 첫번째 값 username
			System.out.println("result[1] = " + result[1]); //배열의 두번째 값 age

			// 방법 2, 제네릭을 사용하여 타입 캐스팅 과정을 생략할 수 있다.
			List<Object[]> resultList2 = em3.createQuery("select m.username, m.age from Member m")
					.getResultList();

			Object[] result2 = resultList2.get(0); // 리스트의 첫번째 배열
			System.out.println("result[0] = " + result2[0]); //배열의 첫번째 값 username
			System.out.println("result[1] = " + result2[1]); //배열의 두번째 값 age

			// 방법 3, 생성자 + DTO 사용
			List<MemberDTO> resultList3 = em3.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
					.getResultList();

			MemberDTO memberDTO = resultList3.get(0);
			System.out.println("memberDTO.getUsername() = " + memberDTO.getUsername());
			System.out.println("memberDTO.getAge() = " + memberDTO.getAge());

			tx3.commit();
		} catch (Exception e) {
			tx3.rollback();
		} finally {
			em3.close();
		}

		emf.close();
	}
}
