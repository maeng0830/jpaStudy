package associationRelationship.object_oriented;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 객체 지향 설계(연관 관계 설정)
 * 객체 참조 <-> DB 외래키를 매핑
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
			// 외래키가 아닌 객체를 그대로 사용
			member.setTeam(team);
			em.persist(member);

			em.flush();
			em.clear();

			// 객체 참조 사용
			Member findMember = em.find(Member.class, member.getId()); // fetchType.eager = join해서 한번에 가져온다.
			Team findMemberTeam = findMember.getTeam();
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
