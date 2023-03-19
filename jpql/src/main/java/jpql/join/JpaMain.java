package jpql.join;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import jpql.Member;
import jpql.Team;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		// 조인
		try {
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);

			Member member = new Member();
			member.setUsername("member1");
			member.setAge(10);
			member.changeTeam(team);
			em.persist(member);

			em.flush();
			em.clear();

			// 내부 조인: inner 생략 가능
			String query = "select m from Member m inner join m.team t";
			List<Member> resultList = em.createQuery(query, Member.class)
					.getResultList();

			em.flush();
			em.clear();

			// 외부 조인: outer 생략 가능
			String query2 = "select m from Member m left outer join m.team t";
			List<Member> resultList2 = em.createQuery(query2, Member.class)
					.getResultList();

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		EntityManager em2 = emf.createEntityManager();
		EntityTransaction tx2 = em2.getTransaction();

		tx2.begin();

		// on절 활용
		try {
			Team team = new Team();
			team.setName("teamA");
			em2.persist(team);

			Member member = new Member();
			member.setUsername("teamA");
			member.setAge(10);
			member.changeTeam(team);
			em2.persist(member);

			em2.flush();
			em2.clear();

			// 조인 대상 필터링(내부, 외부 조인 둘 다 가능)
			String query = "select m from Member m left join m.team t on t.name = 'teamA'";
			List<Member> resultList = em2.createQuery(query, Member.class)
					.getResultList();

			em2.flush();
			em2.clear();

			// 연관관계 없는 엔티티 조인(member와 team은 연관관계가 없는 상태이다.) - fk로 조인하는 것이 아니다.
			// 내부, 외부 조인 둘 다 가능
			String query2 = "select m from Member m inner join Team t on m.username = t.name";
			List<Member> resultList2 = em2.createQuery(query2, Member.class)
					.getResultList();

			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		} finally {
			em2.close();
		}

		emf.close();
	}
}
