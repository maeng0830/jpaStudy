package jpql.fetch_join;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import jpql.Member;
import jpql.MemberType;
import jpql.Team;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		/**
		 * fetch join
		 * JPQL에서 성능 최적화를 위해 제공하는 기능, SQL 조인 종류 X
		 * 연관된 엔티티 또는 컬렉션을 SQL에서 한번에 함께 조회하는 기능
		 * n + 1 문제의 해결 방법
		 */
		try {
			Team teamA = new Team();
			teamA.setName("teamA");
			em.persist(teamA);

			Team teamB = new Team();
			teamB.setName("teamB");
			em.persist(teamB);

			Member member1 = new Member();
			member1.setUsername("member1");
			member1.setMemberType(MemberType.ADMIN);
			member1.changeTeam(teamA);
			em.persist(member1);

			Member member2 = new Member();
			member2.setUsername("member2");
			member2.setMemberType(MemberType.USER);
			member2.changeTeam(teamA);
			em.persist(member2);

			Member member3 = new Member();
			member3.setUsername("member3");
			member3.setMemberType(MemberType.USER);
			member3.changeTeam(teamB);
			em.persist(member3);

			em.flush();
			em.clear();

			// 다대일 페치 조인
			String query = "select m From Member m join fetch m.team";
			// EAGER 일반 조인: 회원 목록(프록시 team) select 쿼리 -> 즉시 각 team에 대한 select 쿼리
			// EAGER 페치 조인: 회원 목록(실제 team) select join 쿼리
			// LAZY 일반 조인: 회원 목록(프록시 team) select 쿼리 -> 각 team의 데이터에 접근할 때 각각 select 쿼리
			// LAZY 페치 조인: 회원 목록(실제 team) select join 쿼리
			System.out.println("====== List<Member> members = em.createQuery(query, Member.class).getResultList(); ======");
			List<Member> members = em.createQuery(query, Member.class).getResultList();

			System.out.println("====== member.getUsername() = \" + member.getUsername(); ======");
			for (Member member : members) {
				System.out.print("member.getUsername() = " + member.getUsername() + ", " +
						"member.getTeam().getName()" + member.getTeam().getName());
				System.out.println();
			}

			em.clear();

			// 일대다, 컬렉션 페치 조인
			// 일대다, 컬렉션 조인 시 결과가 부풀려진다는 것을 주의하자(teamA - member1, teamA - member2)
			String query2 = "select t from Team t join fetch t.members";
			List<Team> teams = em.createQuery(query2, Team.class)
					.getResultList();

			for (Team team : teams) {
				System.out.println("team.getName() = " + team.getName() + ":" + team.getMembers().size() + "명");
				for (Member m: team.getMembers()) {
					System.out.println(" -m.getUsername() = " + m.getUsername());
				}
			}
			/*team.getName() = teamA:2명
					-m.getUsername() = member1
					-m.getUsername() = member2
			team.getName() = teamA:2명
					-m.getUsername() = member1
					-m.getUsername() = member2
			team.getName() = teamB:1명
					-m.getUsername() = member3*/

			em.clear();

			// SQL의 distinct는 로우의 모든 데이터가 동일할 때 중복으로 판별하여 중복 제거한다.
			// JPQL의 distinct는 그것과 더불어 어플리케이션 차원에서 결과 컬렉션 내부의 같은 식별자를 가진 엔티티를 중복으로 판별하여 제거한다!
			String query3 = "select distinct t from Team t join fetch t.members";
			List<Team> teams2 = em.createQuery(query3, Team.class)
					.getResultList();

			for (Team team : teams2) {
				System.out.println("team.getName() = " + team.getName() + ":" + team.getMembers().size() + "명");
				for (Member m: team.getMembers()) {
					System.out.println(" -m.getUsername() = " + m.getUsername());
				}
			}
			/*team.getName() = teamA:2명
					-m.getUsername() = member1
					-m.getUsername() = member2
			team.getName() = teamB:1명
					-m.getUsername() = member3*/

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
