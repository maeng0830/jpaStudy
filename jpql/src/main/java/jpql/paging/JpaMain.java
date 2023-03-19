package jpql.paging;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import jpql.Member;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		// 페이징 setFirstResult() + setMaxResults() - 벤더에 따라 알맞은 쿼리를 알아서 생성해준다.
		try {

			for (int i = 0; i < 100; i++) {
				Member member = new Member();
				member.setUsername("member" + i);
				member.setAge(i);
				em.persist(member);
			}

			em.flush();
			em.clear();

			List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
					.setFirstResult(0) //시작 row
					.setMaxResults(10) // 개수
					.getResultList();

			System.out.println("result.size() = " + result.size());
			for (Member member1 : result) {
				System.out.println("member1 = " + member1);
			}

			// update 쿼리 실행
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
