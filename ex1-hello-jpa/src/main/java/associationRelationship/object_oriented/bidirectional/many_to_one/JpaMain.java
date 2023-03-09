package associationRelationship.object_oriented.bidirectional.many_to_one;


import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 객체 지향 설계(연관 관계 설정)
 * 객체 참조 <-> DB 외래키를 매핑
 * 양방향 연관관계
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

			// 양방향 연관관계
			List<Member> members = findMember.getTeam().getMembers();
			for (Member m : members) {
				System.out.println("m = " + m.getName());
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
		// 연관관계의 주인에게 join될 변수(컬럼)의 데이터 변경을 적용해야한다!
		// 연관관계의 주인이 아닌 경우, join 변수(컬럼)의 데이터 변경이 sql로 DB에게 전달되지 않는다.
		try {
//			Member member = new Member();
//			member.setName("member2");
//			em2.persist(member);
//
//			Team team = new Team();
//			team.setName("teamB");
//			// 연관관계의 주인이 아니므로, join될 변수(컬럼)의 데이터 변경이 적용되지 않는다. 조회만 가능하다.
//			team.getMembers().add(member);
//			em2.persist(team);

			Team team = new Team();
			team.setName("teamB");
			em2.persist(team);

			Member member = new Member();
			member.setName("member2");
			// 연관관계의 주인이므로, join될 변수(컬럼)의 데이터 변경이 적용된다!
			member.setTeam(team);
			em2.persist(member);

			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		} finally {
			em2.close();
		}

		EntityManager em3 = emf.createEntityManager();
		EntityTransaction tx3 = em3.getTransaction();
		tx3.begin();
		// DB에 변경사항이 전달되지 않더라도, 연관관계 주인이 아닌 join 변수(컬럼)에 데이터 변경을 똑같이 해주는 것이 좋다!
		// 연관관계 주인의 join 변수(컬럼) 데이터 변경사항이 1차 캐시에 곧바로 적용되지 않아 문제가 발생할 수 있기 때문이다!
		// 또한 테스트 케이스 작성 시에는 JPA 없는 순수한 자바 코드로 작성해야할 경우가 있다. 이때도 문제가 발생할 수 있다.
		try {
			Team team = new Team();
			team.setName("teamC");
			em3.persist(team);

			Member member = new Member();
			member.setName("member3");
			// 연관관계의 주인이므로, join될 변수(컬럼)의 데이터 변경이 적용된다!
			member.setTeam(team);
			em3.persist(member);

			Team memberTeam = member.getTeam();
			System.out.println("memberTeam.getName() = " + memberTeam.getName());

			System.out.println("===== DB 전달 및 1차 캐시 갱신 이전 ======");
			List<Member> members = team.getMembers(); // 1차 캐시 갱신 X
			if (!members.isEmpty()) {
				for (Member m : members) {
					System.out.println("m.getName() = " + m.getName());
				}
			}

			em3.flush(); // DB 전달
			em3.clear();

			System.out.println("===== DB 전달 및 1차 캐시 갱신 이후 ======");
			List<Member> members2 = em3.find(Team.class, team.getId()).getMembers(); // 1차 캐시 갱신
			if (!members2.isEmpty()) {
				for (Member m : members2) {
					System.out.println("m.getName() = " + m.getName());
				}
			}

			tx3.commit();
		} catch (Exception e) {
			tx3.rollback();
		} finally {
			em3.close();
		}

		emf.close();
	}
}
