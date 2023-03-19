package jpql.condition;

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

			String query = "select "
					+ "case when m.age < 20 then '학생요금' when m.age >= 60 then '노인 우대' else '일반 요금' end from Member m";
			List<String> resultList = em.createQuery(query, String.class)
					.getResultList();

			for (String s : resultList) {
				System.out.println("s = " + s);
			}

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		EntityManager em2 = emf.createEntityManager();
		EntityTransaction tx2 = em2.getTransaction();

		tx2.begin();

		//coalesce
		try {
			Team team = new Team();
			team.setName("teamA");
			em2.persist(team);

			Member member = new Member();
			member.setUsername("관리자");
			member.setAge(10);
			member.setMemberType(MemberType.ADMIN);
			member.changeTeam(team);
			em2.persist(member);

			Member member2 = new Member();
			member.setAge(10);
			member2.setMemberType(MemberType.ADMIN);
			member2.changeTeam(team);
			em2.persist(member2);

			em2.flush();
			em2.clear();

			// 값이 있으면 m.username, null이면 이름없는회원 반환
			String query = "select coalesce(m.username, '이름 없는 회원') from Member m";
			List<String> resultList = em2.createQuery(query, String.class)
					.getResultList();

			for (String s : resultList) {
				System.out.println("s = " + s);
			}

			// m.username이 관리자면 null 반환, 아니면 그대로 반환
			String query2 = "select nullif(m.username, '관리자') from Member m";
			List<String> resultList2 = em2.createQuery(query2, String.class)
					.getResultList();

			for (String s : resultList2) {
				System.out.println("s = " + s);
			}

			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		} finally {
			em2.close();
		}

		emf.close();
	}
}
