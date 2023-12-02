package com.jpa.practice.jpaTest;

import com.jpa.practice.member.domain.Member;
import com.jpa.practice.member.repository.MemberRepository;
import com.jpa.practice.team.domain.TeamForLazy;
import com.jpa.practice.team.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class FetchJoinTest {

	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private TeamRepository teamRepository;

	@PersistenceContext
	private EntityManager em;

	@Test
	void membersWithFetchJoin() {
		TeamForLazy teamA = TeamForLazy.builder()
				.name("teamA")
				.build();
		TeamForLazy teamB = TeamForLazy.builder()
				.name("teamB")
				.build();
		teamRepository.saveTL(teamA);
		teamRepository.saveTL(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForLazy(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForLazy(teamA)
				.build();
		Member memberC = Member.builder()
				.username("userC")
				.age(10)
				.teamForLazy(teamB)
				.build();
		Member memberD = Member.builder()
				.username("userD")
				.age(20)
				.teamForLazy(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);
		memberRepository.save(memberC);
		memberRepository.save(memberD);

		em.flush();
		em.clear();

		List<Member> members = memberRepository.membersWithFetchJoin();
		for (Member member : members) {
			System.out.println("member.getTeamForLazy().getName() = " + member.getTeamForLazy().getName());
		}
		/*
		 * join 절이 적용되고, fetch join이 적용된 엔티티를 select 절에 포함하여 한번에 함께 조회한다.
		 *
		 *   SELECT m1_0.id, ... , t1_0.id, ... FROM member m1_0 JOIN team_for_lazy t1_0 ON t1_0.id=m1_0.team_for_lazy_id
		 *   member.getTeamForLazy().getName() = teamA
		 *   member.getTeamForLazy().getName() = teamA
		 *   member.getTeamForLazy().getName() = teamB
		 *   member.getTeamForLazy().getName() = teamB
		 */
	}

	@Test
	void teamsWithFetchJoin() {
		TeamForLazy teamA = TeamForLazy.builder()
				.name("teamA")
				.build();
		TeamForLazy teamB = TeamForLazy.builder()
				.name("teamB")
				.build();
		teamRepository.saveTL(teamA);
		teamRepository.saveTL(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForLazy(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForLazy(teamA)
				.build();
		Member memberC = Member.builder()
				.username("userC")
				.age(10)
				.teamForLazy(teamB)
				.build();
		Member memberD = Member.builder()
				.username("userD")
				.age(20)
				.teamForLazy(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);
		memberRepository.save(memberC);
		memberRepository.save(memberD);

		em.flush();
		em.clear();

		List<TeamForLazy> teams = teamRepository.teamsWithFetchJoin();
		for (TeamForLazy team : teams) {
			System.out.println("team.getName() = " + team.getName() + ":" + team.getMembers().size() + "명");
			for (Member m: team.getMembers()) {
				System.out.println(" -m.getUsername() = " + m.getUsername());
			}
		}
		/*
		 * join 절이 적용되고, fetch join이 적용된 엔티티를 select 절에 포함하여 한번에 함께 조회한다.
		 * 하지만 컬렉션(일대다) fetch join 시에는 조회 결과가 부풀려진다는 것을 주의하자.
		 *
		 *   SELECT t1_0.id, ... , m1_0.id, ... FROM team_for_lazy t1_0 JOIN member m1_0 ON t1_0.id=m1_0.team_for_lazy_id
		 *   team.getName() = teamA:2명
		 *     -m.getUsername() = userA
		 *     -m.getUsername() = userB
		 *   team.getName() = teamA:2명
		 *     -m.getUsername() = userA
		 *     -m.getUsername() = userB
		 *   team.getName() = teamB:2명
		 *     -m.getUsername() = userC
		 *     -m.getUsername() = userD
		 *   team.getName() = teamB:2명
		 *     -m.getUsername() = userC
		 *     -m.getUsername() = userD
		 */
	}

	@Test
	void teamsWithFetchJoinDistinct() {
		TeamForLazy teamA = TeamForLazy.builder()
				.name("teamA")
				.build();
		TeamForLazy teamB = TeamForLazy.builder()
				.name("teamB")
				.build();
		teamRepository.saveTL(teamA);
		teamRepository.saveTL(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForLazy(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForLazy(teamA)
				.build();
		Member memberC = Member.builder()
				.username("userC")
				.age(10)
				.teamForLazy(teamB)
				.build();
		Member memberD = Member.builder()
				.username("userD")
				.age(20)
				.teamForLazy(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);
		memberRepository.save(memberC);
		memberRepository.save(memberD);

		em.flush();
		em.clear();

		List<TeamForLazy> teams = teamRepository.teamsWithFetchJoinDistinct();
		for (TeamForLazy team : teams) {
			System.out.println("team.getName() = " + team.getName() + ":" + team.getMembers().size() + "명");
			for (Member m: team.getMembers()) {
				System.out.println(" -m.getUsername() = " + m.getUsername());
			}
		}
		/*
		 * join 절이 적용되고, fetch join이 적용된 엔티티를 select 절에 포함하여 한번에 함께 조회한다.
		 * JPQL의 distinct 키워드를 사용하였다.
		 *   SQL의 distinct는 로우의 모든 데이터가 동일할 때 중복으로 판별하여 중복 제거한다.
		 *   JPQL의 distinct는 그것과 더불어 어플리케이션 차원에서 결과 컬렉션 내부의 같은 식별자를 가진 엔티티를 중복으로 판별하여 제거한다!
		 *
		 *   SELECT t1_0.id, ... , m1_0.id, ... FROM team_for_lazy t1_0 JOIN member m1_0 ON t1_0.id=m1_0.team_for_lazy_id
		 *   team.getName() = teamA:2명
		 *     -m.getUsername() = userA
		 *     -m.getUsername() = userB
		 *   team.getName() = teamB:2명
		 *     -m.getUsername() = userC
		 *     -m.getUsername() = userD
		 */
	}
}
