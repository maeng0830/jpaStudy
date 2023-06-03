package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;

	@Test
	void basicTest() {
		// given
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		// when
		Member findMember = memberRepository.findById(member.getId()).get();
		List<Member> result1 = memberRepository.findAll();
		List<Member> result2 = memberRepository.findByUsername("member1");

		// then
		assertThat(findMember).isEqualTo(member);
		assertThat(result1).containsExactly(member);
		assertThat(result2).containsExactly(member);
	}

	@Test
	public void searchTest() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setAgeGoe(35);
		condition.setAgeLoe(40);
		condition.setTeamName("teamB");

//		List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
		List<MemberTeamDto> result = memberRepository.search(condition);

		assertThat(result).extracting("username")
				.containsExactly("member4");
	}

	@Test
	public void searchPageSimple() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

		MemberSearchCondition condition = new MemberSearchCondition();
		PageRequest pageRequest = PageRequest.of(0, 3);

//		List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
		Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result.getContent())
				.extracting("username")
				.containsExactly("member1", "member2", "member3");
	}

	// QuerydslPredicateExecutor는 실무 환경에서 한계가 명확하다.
	// left join이 불가능하다.
	// 서비스 계층이 querydsl이라는 구현 기술에 의존해야한다.
	@Test
	void querydslPredicateExecutorTest() {
		// given
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

		QMember member = QMember.member;
		Iterable<Member> result = memberRepository.findAll(
				member.age.between(10, 40).and(member.username.eq("member1")));

		for (Member m : result) {
			System.out.println("m = " + m);
		}

		// when

		// then
	}
}