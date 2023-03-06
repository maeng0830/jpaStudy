package hellojpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

	public static void main(String[] args) {
		/**
		 * META-INF/persistence.xml의 정보를 읽어 EntityManagerFactory를 생성한다.
		 * 어플리케이션 구동 시점에 딱 1개의 emf만 생성
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

		/**
		 * EntityManagerFactory에서 entityManager를 생성한다.
		 * 트랜잭션 단위의 작업이 실행 될 때마다, em을 생성
		 */
		EntityManager em = emf.createEntityManager();

		// 트랜잭션 획득 및 시작
		// JPA의 모든 데이터 변경은 트랜잭션 안에서 실행되어야 한다.
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		// 저장
		/*try {
			Member member = new Member();
			member.setId(2L);
			member.setName("HelloA");

			// member 저장
			em.persist(member);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			// 리소스 정리
			em.close();
		}*/

		// 조회
		try {
//			Member findMember = em.find(Member.class, 1L);

			// JPQL - 객체 지향 쿼리: 테이블이 아닌 Entity 객체를 대상으로한 쿼리를 작성
			// 특정 DB에 종속적이지 않게 된다.
			List<Member> result = em.createQuery("select m from Member as m", Member.class)
					.getResultList();

			for (Member member : result) {
				System.out.println("member = " + member.getName());
			}

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();

	}
}
