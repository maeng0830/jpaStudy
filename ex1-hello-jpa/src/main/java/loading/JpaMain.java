package loading;

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

		// LAZY
		try {
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);

			Member member = new Member();
			member.setName("memberA");
			member.setTeam(team);
			em.persist(member);

			em.flush();
			em.clear();

			// Team에 지연 로딩을 설정해주었다.
			// Member만 DB에서 조회하고, Team은 프록시 객체로 생성한다.
			Member m = em.find(Member.class, member.getId());
			System.out.println("m.getTeam().getClass() = " + m.getTeam().getClass()); // 프록시 Team

			// 실제 Team의 값을 조회할 때 Team을 조회하는 쿼리가 따로 실행된다. 즉 초기화된다.
			m.getTeam().getName();

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		EntityManager em2 = emf.createEntityManager();
		EntityTransaction tx2 = em2.getTransaction();

		tx2.begin();

		// EAGER
		try {
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);

			Member member = new Member();
			member.setName("memberA");
			member.setTeam(team);
			em.persist(member);

			em.flush();
			em.clear();

			// Team에 즉시 로딩을 설정해주었다.
			// join을 통해 Member와 Team을 DB에서 함께 조회한다.
			Member m = em.find(Member.class, member.getId());
			System.out.println("m.getTeam().getClass() = " + m.getTeam().getClass()); // Team

			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		} finally {
			em2.close();
		}

		emf.close();
	}
}
