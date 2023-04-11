package study.datajpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

	List<Member> findTop3By();

	// 메소드명 간략화, 리포지토리 메소드에 바로 JPQL 작성 가능
	// 쿼리에 문법 오류가 있을 경우, 컴파일 예외 발생
	@Query("select m from Member m where m.username = :username and m.age = :age")
	List<Member> findUser(@Param("username") String username, @Param("age") int age);

	// String 타입으로 조회
	@Query("select m.username from Member m")
	List<String> findUsernameList();

	// DTO 타입으로 조회
	// DTO에 해당하는 생성자가 필요하다. MemberDto(m.id, m.username, t.name)
	@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();

	// Collection을 통한 in절 처리
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") Collection<String> names);

	// 컬렉션 조회
	// 없으면 빈 컬렉션 반환
	List<Member> findListByUsername(String username);

	// 단건 조회
	// 값이 없을 경우, 순수 JPA에서는 예외 발생, 스프링 데이타 JPA에서는 null
	// 여러건인 경우, 예외발생
	Member findMemberByUsername(String username);

	// Optional 단건 조회
	// 여러건인 경우, 예외 발생
	Optional<Member> findOptionalByUsername(String username);
}