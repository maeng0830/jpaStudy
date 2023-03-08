package associationRelationship.database_oriented;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 데이터베이스(테이블) 지향 설계
 * 외래키를 그대로 사용
 */
public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		try {
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);

			Member member = new Member();
			member.setName("member1");
			// 객체가 아닌 외래키를 사용
			member.setTeamId(team.getId());
			em.persist(member);

			em.flush();
			em.clear();

			// 멤버의 팀 이름를 가져오는 과정.. 마치 외래키를 사용하는 것 같다..
			// 객체 참조를 사용해서 findMember.getTeam.getName()이면 얼마나 편할까..
			Member findMember = em.find(Member.class, member.getId());
			Long findMemberTeamId = findMember.getTeamId();
			Team findMemberTeam = em.find(Team.class, findMemberTeamId);
			String findMemberTeamName = findMemberTeam.getName();
			System.out.println("findMemberTeamName = " + findMemberTeamName);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
