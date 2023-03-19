package jpql.path_expression;

import java.util.Collection;
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

		// 명시적 조인을 사용해야한다. 묵시적 조인(단일값 연관 경로, 컬렉션 값 연관 경로)은 사용하지 말자!
		// 명시적 조인: join 키워드를 직접 사용
		// 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생(내부 조인만 가능)
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

			Member member2 = new Member();
			member2.setUsername("member2");
			member2.setAge(20);
			member2.setMemberType(MemberType.USER);
			member2.changeTeam(team);
			em.persist(member2);

			em.flush();
			em.clear();

			// 상태필드(m.username): 경로 탐색의 끝, 탐색 불가능
			String query = "select m.username From Member m";
			em.createQuery(query, String.class)
					.getResultList();

			//단일 값 연관 경로(m.team): 묵시적 내부 조인 발생, 탐색 가능(m.team.name O)
			//지연 로딩이 안된다.
			String query2 = "select m.team From Member m";
			em.createQuery(query2, Team.class)
					.getResultList();

			//컬렉션 값 연관 경로(): 묵시적 내부 조인 발생, 탐색 불가능(t.members.** X)
			//From절 명시적 조인을 통해 별칭을 얻으면 해당 별칭을 통해 탐색 가능 "select m.username From Team t join t.members m"
			String query3 = "select t.members from Team t";
			List<Collection> result = em.createQuery(query3, Collection.class)
					.getResultList();

			for (Object o : result) {
				System.out.println("o = " + o);
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
