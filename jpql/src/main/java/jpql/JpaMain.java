package jpql;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		try {
			Member member = new Member();
			member.setUsername("member1");
			member.setAge(10);
			em.persist(member);

			// TypeQuery: 리턴 타입이 명확할 때
			TypedQuery<Member> typeQuery = em.createQuery("select m from Member m",
					Member.class);

			// Query: 리턴 타입이 명확하지 않을 때
			Query query = em.createQuery("select m.username, m.age from Member m");

			// 결과가 여러개 일 때 - 결과가 없으면 빈 리스트 반환
			List<Member> typeQueryResultList = typeQuery.getResultList();
			typeQueryResultList.stream()
					.forEach(s -> System.out.println(s.getUsername()));

			// 결과가 정확히 하나일 때 - 결과가 없거나, 둘 이상이면 예외 발생
			Member typeQuerySingleResult = typeQuery.getSingleResult();

			// 파라미터 바인딩 + 체이닝 활용
			Member result = em.createQuery(
							"select m from Member m where m.username = :username", Member.class)
					.setParameter("username", "member1")
					.getSingleResult();

			System.out.println(
					"parameterQuerySingleResult = " + result.getUsername());

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
