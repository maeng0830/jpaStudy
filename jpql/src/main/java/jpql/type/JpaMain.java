package jpql.type;

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

		try {
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);

			Member member = new Member();
			member.setUsername("member1");
			member.setAge(10);
			member.setMemberType(MemberType.ADMIN);
			member.changeTeam(team);
			em.persist(member);

			em.flush();
			em.clear();

			String query = "select m.username, 'HELLO', TRUE, m.memberType from Member m where m.memberType = 'ADMIN'";
			List<Object[]> resultList = em.createQuery(query)
					.getResultList();

			for (Object[] objects : resultList) {
				System.out.println("objects[0] = " + objects[0]); // member1
				System.out.println("objects[1] = " + objects[1]); // HELLO
				System.out.println("objects[2] = " + objects[2]); // true
				System.out.println("objects[3] = " + objects[3]); // ADMIN
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
