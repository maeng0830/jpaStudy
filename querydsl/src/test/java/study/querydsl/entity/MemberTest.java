package study.querydsl.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberTest {

	@Autowired
	EntityManager entityManager;

	@Test
	public void testEntity() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		entityManager.persist(teamA);
		entityManager.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member1", 20, teamA);
		Member member3 = new Member("member1", 10, teamB);
		Member member4 = new Member("member1", 20, teamB);
		entityManager.persist(member1);
		entityManager.persist(member2);
		entityManager.persist(member3);
		entityManager.persist(member4);

		//초기화
		entityManager.flush();
		entityManager.clear();

		//확인
		List<Member> members = entityManager.createQuery("select m from Member m",
						Member.class)
				.getResultList();

		for (Member member : members) {
			System.out.println("member = " + member);
			System.out.println("-> member.getTeam() = " + member.getTeam());
		}

	}
}