package jpql.studyCode;

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
			Team team1 = new Team();
			team1.setName("teamA");
			em.persist(team1);

			Team team2 = new Team();
			team2.setName("teamB");
			em.persist(team2);

			Member member1 = new Member();
			member1.setUsername("member1");
			member1.setAge(10);
			member1.changeTeam(team1);
			em.persist(member1);

			Member member2 = new Member();
			member2.setUsername("member2");
			member2.setAge(10);
			member2.changeTeam(team2);
			em.persist(member2);

			em.flush();
			em.clear();

			// 내부 조인: inner 생략 가능
			String query = "select m from Member m";
			System.out.println("=== select m from Member m 실행 ===");
			List<Member> resultList = em.createQuery(query, Member.class)
					.getResultList();

			for (Member m : resultList) {
				System.out.println("===team 호출====");
				System.out.println("m.getTeam().getName() = " + m.getTeam().getName());
			}

			em.flush();
			em.clear();

			// 외부 조인: outer 생략 가능
			String query2 = "select m from Member m left outer join m.team t";
			System.out.println("=== select m from Member m left outer join m.team t ===");
			List<Member> resultList2 = em.createQuery(query2, Member.class)
					.getResultList();

			for (Member m : resultList2) {
				System.out.println("===team 호출====");
				System.out.println("m.getTeam().getName() = " + m.getTeam().getName());
			}

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}
//
//		EntityManager em2 = emf.createEntityManager();
//		EntityTransaction tx2 = em2.getTransaction();
//
//		tx2.begin();
//
////		 on절 활용
//		try {
//			Team team1 = new Team();
//			team1.setName("teamA");
//			em2.persist(team1);
//
//			Team team2 = new Team();
//			team2.setName("teamB");
//			em2.persist(team2);
//
//			Member member1 = new Member();
//			member1.setUsername("member1");
//			member1.setAge(10);
//			member1.changeTeam(team1);
//			em2.persist(member1);
//
//			Member member2 = new Member();
//			member2.setUsername("teamB");
//			member2.setAge(10);
//			member2.changeTeam(team2);
//			em2.persist(member2);
//
//			em2.flush();
//			em2.clear();
//
//			// 조인 대상 필터링(내부, 외부 조인 둘 다 가능)
//			// 팀이름이 teamA인 멤버만 조회한다
//			String query = "select m from Member m join m.team t on t.name = 'teamA'";
//			List<Member> resultList = em2.createQuery(query, Member.class)
//					.getResultList();
//
//			System.out.println("resultList.size() = " + resultList.size()); // print: 1
//
//			em2.flush();
//			em2.clear();
//
//			// 연관관계 없는 엔티티 조인(member와 team은 연관관계가 없는 상태이다.) - PK와 FK로 조인하는 것이 아니다.
//			// 멤버이름과 팀이름이 같은 멤버만 조회한다.
//			String query2 = "select m from Member m join Team t on m.username = t.name";
//			List<Member> resultList2 = em2.createQuery(query2, Member.class)
//					.getResultList();
//
//			System.out.println("resultList2.get(0).getUsername() = " + resultList2.get(0).getUsername()); // print: teamB
//
//			tx2.commit();
//		} catch (Exception e) {
//			tx2.rollback();
//		} finally {
//			em2.close();
//		}

		emf.close();
	}
}
